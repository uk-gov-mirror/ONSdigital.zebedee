package com.github.onsdigital.zebedee.search.indexing;

import com.github.onsdigital.zebedee.reader.configuration.ReaderConfiguration;
import com.github.onsdigital.zebedee.util.URIUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.onsdigital.zebedee.search.indexing.SearchBoostTermsResolver.getSearchTermResolver;
import static com.github.onsdigital.zebedee.util.PathUtils.toRelativeUri;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * Searches the file system
 */
public class FileScanner {

    private Path root;

    public FileScanner() {
        root = Paths.get(ReaderConfiguration.get().getContentDir());
    }

    public List<Document> scan() throws IOException {
        return scan(null);
    }

    public List<Document> scan(String path) throws IOException {

        Path dir = root;
        if (isEmpty(path) == false) {
            dir = root.resolve(URIUtils.removeLeadingSlash(path));
        }

        List<Document> fileNames = new ArrayList<>();
        List<Document> allFileNames = getFileNames(fileNames, dir, null);

        /*
        TODO - SPIKE CODE

        I'm just hard coding a single cmd dataset url here. To do it properly we'll need the
        dataset api client to return a list of all published datasets and add the additional
        documents from that.

        Note 1 - we may need to do something clever for search terms but at the moment they don't appear to be
        being used.

        Note 2 - we're probably better off using 'datasets/{dataset}'and pulling the version url from that
        response (its needed later), shouldn't be too hard.
         */

        allFileNames.add(new Document("/datasets/suicides-in-the-uk/editions/time-series/versions/1", null));

        return allFileNames;
    }

    /**
     * Iterates through the file system from a specified root directory and
     * stores the file names
     *
     * @param fileNames a List to store results in
     * @param dir       the root directory to start searching from
     * @return the list with file names
     * @throws IOException if any file io operations fail
     */
    private List<Document> getFileNames(List<Document> fileNames, Path dir, Set<List<String>> searchTerms)
            throws IOException {

        if (fileNames == null || dir == null) {
            throw new IllegalArgumentException(
                    "List of fileNames and Path dir cannot be null");
        }

        if (searchTerms == null) {
            searchTerms = new HashSet<>();
        }

        // java 7 try-with-resources automatically closes streams after use
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                String uri = toRelativeUri(root, path.getParent()).toString();
                if (path.toFile().isDirectory()) {
                    if (isNotPreviousVersion(path.getFileName().toString())) {
                        List<String> termsForPrefix = getSearchTermResolver().getTermsForPrefix(uri);
                        searchTerms.add(termsForPrefix);
                        getFileNames(fileNames, path, searchTerms);
                        searchTerms.remove(termsForPrefix);
                    } else {
                        continue;//skip versions
                    }
                } else {
                    String fullPath = toUri(path);
                    if (isDataFile(fullPath)) {
                        List<String> terms = getSearchTermResolver().getTerms(uri);
                        searchTerms.add(terms);

                        //info().data("searchTerms", searchTerms).data("uri", uri).log("updating elastic search");

                        fileNames.add(new Document(uri, searchTerms));
                        searchTerms.remove(terms);
                    }
                }
            }
        }

        return fileNames;
    }

    private String toUri(Path path) {
        return path.toAbsolutePath().toString();
    }

    private static boolean isDataFile(String fileName) {
        return fileName.endsWith("data.json");
    }

    private static boolean isNotPreviousVersion(String fileName) {
        return !fileName.equals("previous");
    }
}