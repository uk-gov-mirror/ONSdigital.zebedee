package com.github.onsdigital.zebedee.api.cmd;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.zebedee.content.page.statistics.dataset.ApiDatasetLandingPage;
import com.github.onsdigital.zebedee.exceptions.ZebedeeException;
import com.github.onsdigital.zebedee.reader.util.DatasetAPIClientSupplier;
import com.github.onsdigital.zebedee.util.JsonUtils;
import dp.api.dataset.DatasetClient;
import dp.api.dataset.exception.DatasetAPIException;
import dp.api.dataset.exception.DatasetNotFoundException;
import dp.api.dataset.model.Dataset;
import dp.api.dataset.model.DatasetLinks;
import dp.api.dataset.model.Link;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import java.io.IOException;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static com.github.onsdigital.zebedee.logging.CMSLogEvent.info;

@Api
public class Datasets {

    static final String zebedeeFileSuffix = "/data.json";

    private DatasetClient datasetClient;

    public Datasets() {
        try {
            this.datasetClient = DatasetAPIClientSupplier.get();
        } catch (ZebedeeException e) {
            // TODO
            e.printStackTrace();
        }
    }

    @POST
    public void createDataset(HttpServletRequest request, HttpServletResponse response, ApiDatasetLandingPage page) throws IOException, DatasetAPIException {
        if (datasetExists(page.getapiDatasetId())) {
            info().log("dataset already exists");
            JsonUtils.writeResponseEntity(response, null, 400);
        } else {
            info().log("dataset does not exist doing a thing...");

            String uri = request.getParameter("uri");
            uri = trimZebedeeFileSuffix(uri);

            Dataset dataset = new Dataset();
            dataset.setId(page.getapiDatasetId());
            dataset.setTitle(page.getDescription().getTitle());

            Link taxonomyLink = new Link();
            taxonomyLink.setHref(uri);

            DatasetLinks datasetLinks = new DatasetLinks();
            datasetLinks.setTaxonomy(taxonomyLink);
            dataset.setLinks(datasetLinks);

            try {
                info().data("id", dataset.getId())
                        .data("title", dataset.getTitle())
                        .data("pageURI", uri)
                        .log("creating dataset in the dataset api");

                datasetClient.createDataset(page.getapiDatasetId(), dataset);
            } catch (DatasetAPIException e) {
                error().data("id", dataset.getId()).data("title", dataset.getTitle())
                        .data("pageURI", uri).logException(e, "failed to create dataset in the dataset api");
                throw new RuntimeException(e);
            }
        }
    }

    boolean datasetExists(String datasetID) throws IOException, DatasetAPIException {
        Dataset dataset = null;
        try {
            dataset = datasetClient.getDataset(datasetID);
        } catch (DatasetNotFoundException ex) {
            // this is fine.
        }
        return dataset != null;
    }

    String trimZebedeeFileSuffix(String uri) {
        if (uri.endsWith(zebedeeFileSuffix)) {
            uri = uri.substring(0, uri.length() - zebedeeFileSuffix.length());
        }
        return uri;
    }
}
