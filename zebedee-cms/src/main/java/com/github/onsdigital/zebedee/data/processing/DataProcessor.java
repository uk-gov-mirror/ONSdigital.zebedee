package com.github.onsdigital.zebedee.data.processing;

import com.github.onsdigital.zebedee.content.page.base.PageDescription;
import com.github.onsdigital.zebedee.content.page.statistics.data.timeseries.TimeSeries;
import com.github.onsdigital.zebedee.content.page.statistics.data.timeseries.TimeSeriesValue;
import com.github.onsdigital.zebedee.content.page.statistics.dataset.DatasetLandingPage;
import com.github.onsdigital.zebedee.content.partial.Contact;
import com.github.onsdigital.zebedee.content.partial.Link;
import com.github.onsdigital.zebedee.content.util.ContentUtil;
import com.github.onsdigital.zebedee.exceptions.NotFoundException;
import com.github.onsdigital.zebedee.exceptions.ZebedeeException;
import com.github.onsdigital.zebedee.model.CollectionContentReader;
import com.github.onsdigital.zebedee.model.CollectionContentWriter;
import com.github.onsdigital.zebedee.reader.ContentReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by thomasridd on 1/16/16.
 */
public class DataProcessor {
    public int updates = 0;
    public int inserts = 0;
    public TimeSeries timeSeries = null;

    public DataProcessor() {

    }

    /**
     * Take a timeseries as produced by Brian from an upload and combine it with current content
     *
     * @param publishedContentReader
     * @param reviewedContentReader
     * @param reviewedContentWriter
     * @param details
     * @param newTimeSeries
     * @return
     */
    public TimeSeries processTimeseries(ContentReader publishedContentReader, CollectionContentReader reviewedContentReader, CollectionContentWriter reviewedContentWriter, DataPublicationDetails details, TimeSeries newTimeSeries) throws ZebedeeException, IOException, URISyntaxException {

        // Get current version of the time series
        this.timeSeries = initialTimeseries(newTimeSeries, publishedContentReader, details);

        // Add meta from the landing page and timeseries dataset page
        syncLandingPageMetadata(this.timeSeries, details);
        syncNewTimeSeriesMetadata(this.timeSeries, newTimeSeries);

        // Combine the time series values
        mergeTimeseries(this.timeSeries, newTimeSeries, details);

        return this.timeSeries;
    }

    /**
     *
     *
     * @param page
     * @param details
     * @return
     * @throws URISyntaxException
     */
    TimeSeries syncLandingPageMetadata(TimeSeries page, DataPublicationDetails details) throws URISyntaxException {
        PageDescription description = page.getDescription();
        if (description == null) {
            description = new PageDescription();
            page.setDescription(description);
        }
        description.setNextRelease(details.landingPage.getDescription().getNextRelease());
        description.setReleaseDate(details.landingPage.getDescription().getReleaseDate());

        // Set some contact details
        addContactDetails(page, details.landingPage);

        // Add the dataset id to sources if necessary
        checkDatasetId(page, details.landingPage);

        // Add the dataset id to sources if necessary
        checkRelatedDatasets(page, details.landingPageUri);

        // Add stats bulletins
        if (details.landingPage.getRelatedDocuments() != null) {
            page.setRelatedDocuments(details.landingPage.getRelatedDocuments());
        }

        return page;
    }

    /**
     * Check if datasetId is listed as a sourceDataset for the timeseries and if not add it
     *
     * @param timeSeries
     * @param landingPage
     */
    private static void checkDatasetId(TimeSeries timeSeries, DatasetLandingPage landingPage) {
        boolean datasetIsNew = true;

        // Check
        for (String datasetId : timeSeries.sourceDatasets) {
            if (landingPage.getDescription().getDatasetId().equalsIgnoreCase(datasetId)) {
                datasetIsNew = false;
                break;
            }
        }

        // Link
        if (datasetIsNew) {
            timeSeries.sourceDatasets.add(landingPage.getDescription().getDatasetId().toUpperCase());
        }
    }

    /**
     * Check if a landingPage is listed as a related dataset and if not add it
     *
     * @param page
     * @param landingPageUri
     * @throws URISyntaxException
     */
    private static void checkRelatedDatasets(TimeSeries page, String landingPageUri) throws URISyntaxException {
        List<Link> relatedDatasets = page.getRelatedDatasets();
        if (relatedDatasets == null) {
            relatedDatasets = new ArrayList<>();
        }

        // Check
        boolean datasetNotLinked = true;
        for (Link relatedDataset : relatedDatasets) {
            if (relatedDataset.getUri().toString().equalsIgnoreCase(landingPageUri)) {
                datasetNotLinked = false;
                break;
            }
        }

        // Link if necessary
        if (datasetNotLinked) {
            relatedDatasets.add(new Link(new URI(landingPageUri)));
            page.setRelatedDatasets(relatedDatasets);
        }
    }

    /**
     *
     * @param page
     * @param datasetPage
     */
    private static void addContactDetails(TimeSeries page, DatasetLandingPage datasetPage) {
        if (datasetPage.getDescription().getContact() != null) {
            Contact contact = new Contact();
            if (datasetPage.getDescription().getContact().getName() != null) {
                contact.setName(datasetPage.getDescription().getContact().getName());
            }
            if (datasetPage.getDescription().getContact().getTelephone() != null) {
                contact.setTelephone(datasetPage.getDescription().getContact().getTelephone());
            }
            if (datasetPage.getDescription().getContact().getEmail() != null) {
                contact.setEmail(datasetPage.getDescription().getContact().getEmail());
            }
            if (datasetPage.getDescription().getContact().getOrganisation() != null) {
                contact.setOrganisation(datasetPage.getDescription().getContact().getOrganisation());
            }
            page.getDescription().setContact(contact);
        }
    }

    /**
     *
     * @param inProgress
     * @param newSeries
     * @return
     */
    TimeSeries syncNewTimeSeriesMetadata(TimeSeries inProgress, TimeSeries newSeries) {
        if (inProgress.getDescription() == null || newSeries.getDescription() == null) {
            System.out.println("Error copying metadata in data publisher");
        }
        inProgress.getDescription().setSeasonalAdjustment(newSeries.getDescription().getSeasonalAdjustment());
        inProgress.getDescription().setCdid(newSeries.getDescription().getCdid());

        // Copy across the title if it is currently blank (equates to equalling Cdid)
        if (inProgress.getDescription().getTitle() == null || inProgress.getDescription().getTitle().equalsIgnoreCase("")) {
            inProgress.getDescription().setTitle(newSeries.getDescription().getTitle());
        } else if (inProgress.getDescription().getTitle().equalsIgnoreCase(inProgress.getCdid())) {
            inProgress.getDescription().setTitle(newSeries.getDescription().getTitle());
        }

        inProgress.getDescription().setDate(newSeries.getDescription().getDate());
        inProgress.getDescription().setNumber(newSeries.getDescription().getNumber());

        return inProgress;
    }


    /**
     * Get the publish path for a timeseries
     *
     * (TODO: This is currently based on datasetUri and cdid only but will update when code goes unique)
     *
     * @param series
     * @param details
     * @return
     */
    String publishUriForTimeseries(TimeSeries series, DataPublicationDetails details) {
        return details.getTimeseriesFolder() + "/" + series.getCdid().toLowerCase();
    }

    /**
     * Get the starting point for our timeseries by loading a
     *
     * @param series
     * @param publishedContentReader
     * @param details
     * @return
     * @throws ZebedeeException
     * @throws IOException
     */
    TimeSeries initialTimeseries(TimeSeries series, ContentReader publishedContentReader, DataPublicationDetails details) throws ZebedeeException, IOException, URISyntaxException {

        String publishUri = publishUriForTimeseries(series, details);

        // Try to get an existing timeseries
        try {
            TimeSeries existing = (TimeSeries) publishedContentReader.getContent(publishUri);
            return existing;
        } catch (NotFoundException e) {
            // If it doesn't exist create a new empty one using the description
            TimeSeries initial = new TimeSeries();
            initial.setDescription(series.getDescription());
            initial.setUri(new URI(publishUri));

            return initial;
        }
    }


    void mergeTimeseries(TimeSeries originalValues, TimeSeries newValues, DataPublicationDetails details) {
        mergeTimeSeriesValues(originalValues.years, newValues.years, details);
        mergeTimeSeriesValues(originalValues.months, newValues.months, details);
        mergeTimeSeriesValues(originalValues.quarters, newValues.quarters, details);
    }

    void mergeTimeSeriesValues(Set<TimeSeriesValue> currentValues, Set<TimeSeriesValue> updateValues, DataPublicationDetails details) {
        // Iterate through values
        for (TimeSeriesValue value : updateValues) {
            // Find the current value of the data point
            TimeSeriesValue current = getCurrentValue(currentValues, value);

            if (current != null) { // A point already exists for this data

                if (!current.value.equalsIgnoreCase(value.value)) { // A point already exists for this data

                    // Update the point
                    current.value = value.value;
                    current.sourceDataset = details.landingPage.getDescription().getDatasetId();
                    current.updateDate = new Date();

                    this.updates += 1;
                }
            } else {
                value.sourceDataset = details.landingPage.getDescription().getDatasetId();
                value.updateDate = new Date();

                currentValues.add(value);

                this.inserts += 1;
            }
        }
    }

    /**
     * If a {@link TimeSeriesValue} for value.time exists in currentValues returns that.
     * Otherwise null
     *
     * @param currentValues a set of {@link TimeSeriesValue}
     * @param value         a {@link TimeSeriesValue}
     * @return a {@link TimeSeriesValue} from currentValues
     */
    static TimeSeriesValue getCurrentValue(Set<TimeSeriesValue> currentValues, TimeSeriesValue value) {
        if (currentValues == null) {
            return null;
        }

        for (TimeSeriesValue current : currentValues) {
            if (current.compareTo(value) == 0) {
                return current;
            }
        }
        return null;
    }

}
