package com.github.onsdigital.zebedee.search.indexing;

import com.github.onsdigital.zebedee.content.page.base.Page;
import com.github.onsdigital.zebedee.content.page.base.PageDescription;
import com.github.onsdigital.zebedee.content.page.base.PageType;
import com.github.onsdigital.zebedee.content.partial.Link;
import com.github.onsdigital.zebedee.content.util.ContentUtil;
import com.github.onsdigital.zebedee.exceptions.NotFoundException;
import com.github.onsdigital.zebedee.exceptions.ZebedeeException;
import com.github.onsdigital.zebedee.reader.ZebedeeReader;
import com.github.onsdigital.zebedee.search.client.ElasticSearchClient;
import com.github.onsdigital.zebedee.search.model.SearchDocument;
import com.github.onsdigital.zebedee.util.URIUtils;
import dp.api.dataset.model.DatasetVersion;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.onsdigital.zebedee.content.util.ContentUtil.serialise;
import static com.github.onsdigital.zebedee.logging.ReaderLogger.info;
import static com.github.onsdigital.zebedee.logging.ReaderLogger.warn;
import static com.github.onsdigital.zebedee.search.configuration.SearchConfiguration.getSearchAlias;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.startsWith;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dp.api.dataset.DatasetAPIClient;
import dp.api.dataset.model.DatasetMetadata;
import dp.api.dataset.exception.DatasetAPIException;

import com.github.onsdigital.zebedee.content.partial.Contact;
import java.text.SimpleDateFormat;

public class Indexer {
    private final static String DEPARTMENTS_INDEX = "departments";
    private final static String DEPARTMENT_TYPE = "departments";
    private final static String DEPARTMENTS_PATH = "/search/departments/departments.txt";
    private static Indexer instance = new Indexer();
    private final Lock LOCK = new ReentrantLock();
    private final Client client = ElasticSearchClient.getClient();
    private ElasticSearchUtils searchUtils = new ElasticSearchUtils(client);
    private ZebedeeReader zebedeeReader = new ZebedeeReader();

    private static final String DATASET_API_URL = "http://localhost:22000";
    private static final String DATASET_API_AUTH_TOKEN = "FD0108EA-825D-411C-9B1D-41EF7727F465";
    private static final String SERVICE_AUTH_TOKEN = "15C0E4EE-777F-4C61-8CDB-2898CEB34657";

    // TODO - getting these variables from env
    public DatasetAPIClient getDatasetClient() throws URISyntaxException {
        return new DatasetAPIClient(
                DATASET_API_URL,
                DATASET_API_AUTH_TOKEN,
                SERVICE_AUTH_TOKEN);
    }

    private Indexer() {
    }

    public static Indexer getInstance() throws URISyntaxException {
        return instance;
    }

    public static void main(String[] args) throws URISyntaxException {
        try {
            Indexer.getInstance().reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes search index and aliases, it should be run on application start.
     */
    public void reload() throws IOException, URISyntaxException {
        if (LOCK.tryLock()) {
            try {
                lockGlobal();//lock in cluster
                String searchAlias = getSearchAlias();
                boolean aliasAvailable = isIndexAvailable(searchAlias);
                String oldIndex = searchUtils.getAliasIndex(searchAlias);
                String newIndex = generateIndexName();

                info().data("new_index", newIndex)
                        .log("reloading elastic search index");
                searchUtils.createIndex(newIndex, getSettings(), getDefaultMapping());

                if (aliasAvailable && oldIndex == null) {
                    //In this case it is an index rather than an alias. This normally is not possible with index structure set up.
                    //This is a transition code due to elastic search index structure change, making deployment to environments with old structure possible without down time
                    searchUtils.deleteIndex(searchAlias);
                    searchUtils.addAlias(newIndex, searchAlias);
                    doLoad(newIndex);
                } else if (oldIndex == null) {
                    searchUtils.addAlias(newIndex, searchAlias);
                    doLoad(newIndex);
                } else {
                    doLoad(newIndex);
                    searchUtils.swapIndex(oldIndex, newIndex, searchAlias);
                    info().data("old_index", oldIndex).log("deleting old elastic search index");
                    searchUtils.deleteIndex(oldIndex);
                }
            } finally {
                LOCK.unlock();
                unlockGlobal();
            }
        } else {
            throw new IndexInProgressException();
        }
    }

    public boolean isIndexAvailable(String indexName) throws URISyntaxException {
        return searchUtils.isIndexAvailable(indexName);
    }

    private void doLoad(String indexName) throws URISyntaxException, IOException {
        loadDepartments();
        loadContent(indexName);
    }

    private void loadContent(String indexName) throws IOException, URISyntaxException {
        long start = System.currentTimeMillis();
        info().data("index", indexName).log("triggering elastic search reindex");

        indexDocuments(indexName);

        info().data("index", indexName)
                .data("duration", (System.currentTimeMillis() - start))
                .log("elastic search reindex complete");
    }

    private void loadDepartments() throws IOException, URISyntaxException {

        if (isIndexAvailable(DEPARTMENTS_INDEX)) {
            searchUtils.deleteIndex(DEPARTMENTS_INDEX);
        }

        searchUtils.createIndex(DEPARTMENTS_INDEX, getDepartmentsSetting(), DEPARTMENT_TYPE, getDepartmentsMapping());

        info().log("elastic search: indexing departments");
        long start = System.currentTimeMillis();
        try (
                InputStream resourceStream = SearchBoostTermsResolver.class.getResourceAsStream(DEPARTMENTS_PATH);
                InputStreamReader inputStreamReader = new InputStreamReader(resourceStream);
                BufferedReader br = new BufferedReader(inputStreamReader)
        ) {
            for (String line; (line = br.readLine()) != null; ) {
                processDepartment(line);
            }
        }

        info().data("duration", (System.currentTimeMillis() - start))
                .log("elastic search: indexing departments complete");
    }

    private void processDepartment(String line) {
        if (isEmpty(line) || startsWith(line, "#")) {
            return; // skip comments
        }

        String[] split = line.split(" *=> *");
        if (split.length != 4) {
            warn().data("line", line).log("elastic search indexing departments: skipping invalid external line");
            return;
        }
        String[] terms = split[3].split(" *, *");
        if (terms == null || terms.length == 0) {
            return;
        }

        Department department = new Department(split[0], split[1], split[2], terms);
        searchUtils.createDocument(DEPARTMENTS_INDEX, DEPARTMENT_TYPE, split[0], ContentUtil.serialise(department));
    }

    /**
     * Reads content with given uri and indexes for search
     *
     * @param uri
     */

    public void reloadContent(String uri) throws IOException, URISyntaxException {
        try {
            info().data("uri", uri).log("elastic search: triggering reindex for uri");
            long start = System.currentTimeMillis();
            Page page = getPage(uri);
            if (page == null) {
                throw new NotFoundException("Content not found for re-indexing, uri: " + uri);
            }
            if (isPeriodic(page.getType())) {
                //TODO: optimize resolving latest flag, only update elastic search for existing releases rather than reindexing
                //Load old releases as well to get latest flag re-calculated
                index(getSearchAlias(), new FileScanner().scan(URIUtils.removeLastSegment(uri)));
            } else {
                indexSingleContent(getSearchAlias(), page);
            }
            long end = System.currentTimeMillis();
            info().data("uri", uri).data("duration", (start - end)).log("elastic search: reindex for uri complete");
        } catch (ZebedeeException e) {
            throw new IndexingException("Failed re-indexint content with uri: " + uri, e);
        } catch (NoSuchFileException e) {
            throw new IndexingException("Content not found for re-indexing, uri: " + uri);
        }
    }


    public void deleteContentIndex(String pageType, String uri) {
        info().data("uri", uri).log("elastic search: triggering delete index on publishing search index");
        long start = System.currentTimeMillis();
        searchUtils.deleteDocument(getSearchAlias(), pageType, uri);
        long end = System.currentTimeMillis();
        info().data("uri", uri).data("duration", (start - end)).log("elastic searchL delete index complete");
    }

    private String generateIndexName() {
        return getSearchAlias() + System.currentTimeMillis();
    }

    /**
     * Resolves search terms for a single document
     */
    private List<String> resolveSearchTerms(String uri) throws URISyntaxException, IOException {
        if (uri == null) {
            return null;
        }

        SearchBoostTermsResolver resolver = SearchBoostTermsResolver.getSearchTermResolver();
        List<String> terms = new ArrayList<>();
        addTerms(terms, resolver.getTerms(uri));

        String[] segments = uri.split("/");
        for (String segment : segments) {
            String documentUri = "/" + segment;
            addTerms(terms, resolver.getTermsForPrefix(documentUri));
        }
        return terms;
    }

    private void addTerms(List<String> termsList, List<String> terms) {
        if (terms == null) {
            return;
        }
        termsList.addAll(terms);
    }

    private void indexDocuments(String indexName) throws URISyntaxException, IOException {
        index(indexName, new FileScanner().scan());
    }

    /**
     * Recursively indexes contents and their child contents
     *
     * @param indexName
     * @param documents
     * @throws IOException
     */
    private void index(String indexName, List<Document> documents) throws IOException, URISyntaxException {

        /*
        TODO - SPIKE NOTES

        For each document we're checking if it's a cmd dataset (currently hard coded, but done properly its probably a regex
        against the url).

        The searchDocument (the object that generates the request to elasticSearch) is then either build via the existing method
        (renamed to the hefty `prepareJsonPageContentIndexRequest`) or via a mongo specific method currently called
        `prepareMongoDatasetContentIndexRequest`.

        Could do with a refactor to lose some repetition.
        */

        // TODO - handler the URISyntaxException
        DatasetAPIClient datasetApiClient = getDatasetClient();

        try (BulkProcessor bulkProcessor = getBulkProcessor()) {
            for (Document document : documents) {

                Boolean isCmd = false;
                // TODO - an actual check
                if (document.getUri() == "/datasets/suicides-in-the-uk/editions/time-series/versions/1") {
                    isCmd = true;
                }

                if (!isCmd) {
                    // index data.json based content from zebedee
                    try {
                        IndexRequestBuilder indexRequestBuilder = prepareJsonPageContentIndexRequest(indexName, document);
                        if (indexRequestBuilder == null) {
                            continue;
                        }
                        bulkProcessor.add(indexRequestBuilder.request());
                    } catch (Exception e) {
                        System.err.println("!!!!!!!!!Failed preparing index for " + document.getUri() + " skipping...");
                        e.printStackTrace();
                    }
                } else {

                    // index structured dataset content from mongo
                    info().log("attempting to index a cmd dataset");
                    try {
                        IndexRequestBuilder indexRequestBuilder = prepareMongoDatasetContentIndexRequest(indexName, document, datasetApiClient);
                        if (indexRequestBuilder == null) {
                            continue;
                        }
                        bulkProcessor.add(indexRequestBuilder.request());
                    } catch (Exception e) {
                        System.err.println("!!!!!!!!!Failed preparing index for " + document.getUri() + " skipping...");
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    private Page getPage(String uri) throws ZebedeeException, IOException {
        return zebedeeReader.getPublishedContent(uri);
    }

    private IndexRequestBuilder prepareJsonPageContentIndexRequest(String indexName, Document document) throws ZebedeeException, IOException {
        Page page = getPage(document.getUri());
        if (page != null && page.getType() != null) {
            IndexRequestBuilder indexRequestBuilder = searchUtils.prepareIndex(indexName, page.getType().name(), page.getUri().toString());
            indexRequestBuilder.setSource(serialise(toSearchDocument(page, document.getSearchTerms())));
            return indexRequestBuilder;
        }
        return null;
    }

    private IndexRequestBuilder prepareMongoDatasetContentIndexRequest(String indexName, Document document, DatasetAPIClient datasetApiClient)
            throws ZebedeeException, IOException, DatasetAPIException {

        /*
        /TODO - SPIKE CODE

        We're taking the content from the /metadata and /version endpoints on the dataset api and munging it to
        populate the searchDocument class - from there it'll slot in with the usual index requests.

        Its messy and needs work but I've left notes where I can.

         */

        // we're splitting the full version url, i.e "/datasets/*/editions/*/versions/*"
        String url = document.getUri();
        String[] SplitUrl = url.split("/");
        String datasetID = SplitUrl[2];
        String edition = SplitUrl[4];
        String version = SplitUrl[6];

        // TODO - catches
        DatasetMetadata metadata = datasetApiClient.getDatasetMetadata(datasetID, edition, version);
        DatasetVersion versionObj = datasetApiClient.getDatasetVersion(datasetID, edition, version);

        // Create a searchDocument object
        SearchDocument searchDocument = new SearchDocument();

        // Create a contacts object
        Contact contacts = new Contact();

        // TODO - get this working, the current temp client is wrong
        //contacts.setEmail(metadata.getContact().getEmail().toString());
        //contacts.setName(metadata.getContact().getName().toString());
        //contacts.setTelephone(metadata.getContact().getEmail().toString());

        // Create a pageDescription object
        PageDescription pageDescription = new PageDescription();

        // Set the release data
        try {
            String dateWithoutTime = versionObj.getRelease_date().split( " ")[0];
            SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-DD");
            Date date = (Date) formatter.parse(dateWithoutTime);
            pageDescription.setReleaseDate(date);
        } catch (ParseException e) {
            // TODO - log error, drop out
        }

        pageDescription.setTitle(metadata.getTitle().toString());
        pageDescription.setMetaDescription(metadata.getDescription().toString());

        // TODO - client should return type boolean
        String nationalStatistic = metadata.getNationalStatistic();
        if (nationalStatistic == "true") {
            pageDescription.setNationalStatistic(true);
        } else {
            pageDescription.setNationalStatistic(false);
        }
        pageDescription.setContact(contacts);

        // TODO - client should return type List<String> directly
        List<String> keywords = new ArrayList<String>();
        String[] keyWordsRaw = metadata.getKeywords();
        for(String keyword : keyWordsRaw){
            keywords.add(keyword);
        }

        pageDescription.setKeywords(keywords);
        searchDocument.setDescription(pageDescription);

        // Set the uri to the top level dataset
        String datasetUrl = "/datasets/" + datasetID;

        try {
            URI pageUri = new URI(datasetUrl);
            searchDocument.setUri(pageUri);
        } catch (URISyntaxException e) {
            // TODO - log error, drop out
        }

        // PageType.x here populates the 'type' field in index
        searchDocument.setType(PageType.dataset_landing_page);

        // "type" here populates the '_type' field in index
        // using "dataset_landing_page as it ranks better than dataset
        IndexRequestBuilder indexRequestBuilder = searchUtils.prepareIndex(indexName, "dataset_landing_page", searchDocument.getUri().toString());
        indexRequestBuilder.setSource(serialise(searchDocument));

        return indexRequestBuilder;
    }

    private void indexSingleContent(String indexName, Page page) throws URISyntaxException, IOException {
        List<String> terms = resolveSearchTerms(page.getUri().toString());
        searchUtils.createDocument(indexName, page.getType().toString(), page.getUri().toString(), serialise(toSearchDocument(page, terms)));
    }

    private SearchDocument toSearchDocument(Page page, List<String> searchTerms) {
        SearchDocument indexDocument = new SearchDocument();
        indexDocument.setUri(page.getUri());
        indexDocument.setDescription(page.getDescription());
        indexDocument.setTopics(getTopics(page.getTopics()));
        indexDocument.setType(page.getType());
        indexDocument.setSearchBoost(searchTerms);
        return indexDocument;
    }

    private ArrayList<URI> getTopics(List<Link> topics) {
        if (topics == null) {
            return null;
        }
        ArrayList<URI> uriList = new ArrayList<>();
        for (Link topic : topics) {
            uriList.add(topic.getUri());
        }

        return uriList;
    }

    private Settings getSettings() throws IOException {
        Settings.Builder settingsBuilder = Settings.builder().
                loadFromStream("index-config.yml", Indexer.class.getResourceAsStream("/search/index-config.yml"));

        info().data("settings", settingsBuilder.internalMap()).log("elastic search: index settings");
        return settingsBuilder.build();
    }

    private Settings getDepartmentsSetting() {
        Settings.Builder settingsBuilder = Settings.builder().
                loadFromStream("departments-index-config.yml", Indexer.class.getResourceAsStream("/search/departments/departments-index-config.yml"));
        return settingsBuilder.build();
    }

    private String getDefaultMapping() throws IOException {
        InputStream mappingSourceStream = Indexer.class.getResourceAsStream("/search/default-mapping.json");
        String mappingSource = IOUtils.toString(mappingSourceStream);
        info().data("mapping_source", mappingSource).log("elastic search: default mapping");
        return mappingSource;
    }

    private String getDepartmentsMapping() throws IOException {
        InputStream mappingSourceStream = Indexer.class.getResourceAsStream("/search/departments/departments-mapping.json");
        String mappingSource = IOUtils.toString(mappingSourceStream);
        info().data("mappingSource", mappingSource).log("elastic search: get departments mapping file");
        return mappingSource;
    }

    //acquires global lock
    private void lockGlobal() {
        IndexResponse lockResponse = searchUtils.createDocument("fs", "lock", "global", "{}");
        if (!lockResponse.isCreated()) {
            throw new IndexInProgressException();
        }
    }

    private void unlockGlobal() {
        searchUtils.deleteDocument("fs", "lock", "global");
    }

    private boolean isPeriodic(PageType type) {
        switch (type) {
            case bulletin:
            case article:
            case compendium_landing_page:
                return true;
            default:
                return false;
        }
    }

    private BulkProcessor getBulkProcessor() {
        BulkProcessor bulkProcessor = BulkProcessor.builder(
                client,
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(
                            long executionId, BulkRequest request) {
                        info().data("quantity", request.numberOfActions())
                                .log("elastic search bulk processor: bulk indexing  documents");
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                        if (response.hasFailures()) {
                            BulkItemResponse[] items = response.getItems();
                            for (BulkItemResponse item : items) {
                                if (item.isFailed()) {
                                    info().data("uri", item.getFailure().getId())
                                            .data("detailed_message", item.getFailureMessage())
                                            .log("elastic search bulk processor: bulk indexing failure");
                                }
                            }
                        }
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                        info().data("detailedMessagee", failure.getMessage())
                                .exception(failure)
                                .log("elastic search bulk processor: bulk indexing failure");
                    }
                })
                .setBulkActions(10000)
                .setBulkSize(new ByteSizeValue(100, ByteSizeUnit.MB))
                .setConcurrentRequests(4)
                .build();

        return bulkProcessor;
    }
}
