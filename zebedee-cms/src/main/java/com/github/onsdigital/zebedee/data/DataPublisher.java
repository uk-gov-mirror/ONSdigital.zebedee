package com.github.onsdigital.zebedee.data;

import com.github.onsdigital.zebedee.data.importing.TimeseriesUpdateCommand;
import com.github.onsdigital.zebedee.data.processing.DataIndex;
import com.github.onsdigital.zebedee.data.processing.DataPublication;
import com.github.onsdigital.zebedee.data.processing.DataPublicationFinder;
import com.github.onsdigital.zebedee.exceptions.ZebedeeException;
import com.github.onsdigital.zebedee.model.ContentWriter;
import com.github.onsdigital.zebedee.reader.CollectionReader;
import com.github.onsdigital.zebedee.reader.ContentReader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class DataPublisher {
    public boolean doNotCompress = false;

    public DataPublisher(boolean doNotCompress) {
        this.doNotCompress = doNotCompress;
    }

    public DataPublisher() {
        this(false);
    }

    /**
     * Run the preprocess routine that processes csdb uploads with the option of skipping timeseries filesaves
     *
     * @param publishedContentReader  reader for the master content
     * @param collectionContentWriter reader for this publications collection content
     * @param saveTimeSeries          the option to skip saving the individual timeseries
     * @param updateCommands
     * @throws IOException
     * @throws ZebedeeException
     * @throws URISyntaxException
     */
    public List<String> preprocessCollection(
            ContentReader publishedContentReader,
            CollectionReader collectionReader,
            ContentWriter collectionContentWriter,
            boolean saveTimeSeries,
            DataIndex dataIndex,
            List<TimeseriesUpdateCommand> updateCommands
    ) throws IOException, ZebedeeException, URISyntaxException {

        // Find all files that need data preprocessing
        List<DataPublication> dataPublications = new DataPublicationFinder().findPublications(publishedContentReader, collectionReader.getReviewed());

        // For each file in this collection
        for (DataPublication dataPublication : dataPublications) {
            // If a file upload exists
            if (dataPublication.hasUpload())
                dataPublication.process(publishedContentReader, collectionReader.getReviewed(), collectionContentWriter, saveTimeSeries, dataIndex, updateCommands);
        }

        for (TimeseriesUpdateCommand updateCommand : updateCommands) {

            // see if the timeseries is already in the reviewed section

            // if it is then ignore it, its already been updated.

            // if its not then update the title and write it
        }

        // Get the list of uris in reviewed
        List<String> uris = collectionReader.getReviewed().listUris();
        return uris;
    }
}
