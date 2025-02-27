package com.github.onsdigital.zebedee.model.approval.tasks.timeseries;

import com.github.onsdigital.zebedee.exceptions.ZebedeeException;
import com.github.onsdigital.zebedee.model.Collection;
import com.github.onsdigital.zebedee.model.CollectionWriter;
import com.github.onsdigital.zebedee.reader.CollectionReader;
import com.github.onsdigital.zebedee.util.SlackNotification;
import com.github.onsdigital.zebedee.util.slack.PostMessageField;

import java.io.IOException;
import java.util.List;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * This class handles the process of compressing and verifying timeseries, including retry attempts.
 */
public class TimeSeriesCompressionTask {

    private static final int maxAttempts = 5; // number of attempts made at generating and verifying zip files.

    protected TimeSeriesCompressor timeSeriesCompressor;
    protected ZipFileVerifier zipFileVerifier;

    /**
     * Initialise a new instance of TimeSeriesCompressionTask
     *
     * @param timeSeriesCompressor
     * @param zipFileVerifier
     */
    public TimeSeriesCompressionTask(TimeSeriesCompressor timeSeriesCompressor, ZipFileVerifier zipFileVerifier) {
        this.timeSeriesCompressor = timeSeriesCompressor;
        this.zipFileVerifier = zipFileVerifier;
    }

    public TimeSeriesCompressionTask() {
        this.timeSeriesCompressor = new TimeSeriesCompressor();
        this.zipFileVerifier = new ZipFileVerifier();
    }

    /**
     * Compresses time series directories and verifies that the zip files are not corrupt.
     *
     * @param collection
     * @param collectionReader
     * @param collectionWriter
     * @throws ZebedeeException
     * @throws IOException
     */
    public boolean compressTimeseries(Collection collection, CollectionReader collectionReader, CollectionWriter collectionWriter) throws ZebedeeException, IOException {
        info().data("collectionId", collection.getDescription().getId()).log("Compressing time series directories");
        int attempt = 1;
        List<TimeseriesCompressionResult> failedZipFiles = null; // populated on a failed attempt

        while (attempt <= maxAttempts) {
            List<TimeseriesCompressionResult> zipFiles = createZipFiles(collection, collectionReader, collectionWriter, attempt, failedZipFiles);

            failedZipFiles = verifyZipFiles(collection, collectionReader, collectionWriter, attempt, zipFiles);
            if (failedZipFiles.size() == 0) {
                info().data("collectionId", collection.getDescription().getId()).log("Verified time series zip files");
                return true;
            }

            attempt++;
        }

        return false; // if we got this far we have hit the limit of attempts, so its failed.
    }

    private List<TimeseriesCompressionResult> createZipFiles(Collection collection, CollectionReader collectionReader, CollectionWriter collectionWriter, int attempt, List<TimeseriesCompressionResult> failedZipFiles) throws ZebedeeException, IOException {
        List<TimeseriesCompressionResult> zipFiles;
        if (attempt == 1) { // on the first attempt we check all the files.
            zipFiles = timeSeriesCompressor.compressFiles(collectionReader.getReviewed(), collectionWriter.getReviewed(), collection.getDescription().isEncrypted());
        } else { // on additional attempts we check only the failed files.
            zipFiles = timeSeriesCompressor.compressFiles(collectionReader.getReviewed(), collectionWriter.getReviewed(), collection.getDescription().isEncrypted(), failedZipFiles);
        }
        return zipFiles;
    }

    private List<TimeseriesCompressionResult> verifyZipFiles(Collection collection, CollectionReader collectionReader, CollectionWriter collectionWriter, int attempt, List<TimeseriesCompressionResult> zipFiles) throws IOException {
        List<TimeseriesCompressionResult> failedZipFiles;
        info().data("collectionId", collection.getDescription().getId()).data("attempt",
                attempt).log("Verifying " + zipFiles.size() + " time series zip files");
        failedZipFiles = zipFileVerifier.verifyZipFiles(
                zipFiles,
                collectionReader.getReviewed(),
                collectionReader.getRoot(),
                collectionWriter.getRoot());

        for (TimeseriesCompressionResult failedZipFile : failedZipFiles) {
            SlackNotification.collectionWarning(collection,
                    "Failed verification of time series zip file",
                    new PostMessageField("Attempt", Integer.toString(attempt, 10), true),
                    new PostMessageField("Zip path", failedZipFile.zipPath.toString(), false)
            );
            info().data("attempt", attempt).data("zipPath", failedZipFile.zipPath.toString())
                    .log("Failed verification of time series zip file");
        }
        return failedZipFiles;
    }


}
