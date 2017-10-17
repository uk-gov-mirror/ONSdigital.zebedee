package com.github.onsdigital.zebedee.service;

import com.github.onsdigital.zebedee.dataset.api.exception.BadRequestException;
import com.github.onsdigital.zebedee.dataset.api.exception.DatasetNotFoundException;
import com.github.onsdigital.zebedee.dataset.api.exception.UnexpectedResponseException;
import com.github.onsdigital.zebedee.exceptions.ZebedeeException;
import com.github.onsdigital.zebedee.json.CollectionDataset;
import com.github.onsdigital.zebedee.json.CollectionDatasetVersion;

import java.io.IOException;

/**
 * Provides high level dataset functionality
 */
public interface DatasetService {

    /**
     * Add a dataset for the given ID to the collection for the given collectionID.
     */
    CollectionDataset addDatasetToCollection(String collectionID, String datasetID) throws ZebedeeException, IOException, UnexpectedResponseException, DatasetNotFoundException, BadRequestException;

    /**
     * Add the dataset version to the collection for the collectionID.
     */
    CollectionDatasetVersion addDatasetVersionToCollection(String collectionID, String datasetID, String edition, String version) throws ZebedeeException, IOException, UnexpectedResponseException, DatasetNotFoundException, BadRequestException;

    /**
     * Remove the dataset for the given ID from the collection for the given collectionID.
     */
    void removeDatasetFromCollection(String collectionID, String datasetID) throws ZebedeeException, IOException;

    /**
     * Remove the instance for the given datasetID from the collection for the collectionID.
     */
    void removeDatasetVersionFromCollection(String collectionID, String datasetID, String edition, String version) throws ZebedeeException, IOException;
}
