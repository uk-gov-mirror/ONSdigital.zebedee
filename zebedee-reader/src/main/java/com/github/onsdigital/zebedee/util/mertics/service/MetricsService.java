package com.github.onsdigital.zebedee.util.mertics.service;

import com.github.onsdigital.zebedee.reader.api.filter.before.RequestMetricsFilter;
import com.github.onsdigital.zebedee.util.mertics.client.SplunkClient;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static com.github.onsdigital.zebedee.Configuration.SplunkConfiguration.getEventsCollectionURI;
import static com.github.onsdigital.zebedee.Configuration.SplunkConfiguration.getServiceArgs;
import static com.github.onsdigital.zebedee.Configuration.SplunkConfiguration.isSplunkEnabled;
import static com.github.onsdigital.zebedee.logging.ReaderLogger.info;
import static com.github.onsdigital.zebedee.logging.ReaderLogger.warn;

/**
 * Defines API for sevices to capture stats for the metric dashboard.
 */
public abstract class MetricsService {

    protected static MetricsService service = null;

    /**
     * @return the configured implementation of the service.
     */
    public static MetricsService getInstance() {
        if (service == null) {
            if (isSplunkEnabled()) {
                info().log("Splunk MetricsService successfully configured");
                service = new SplunkMetricsServiceImpl(new SplunkClient(getServiceArgs()), getEventsCollectionURI());
            } else {
                warn().log("No MetricsService configured enabling DummyMetricsServiceImpl");
                service = new DummyMetricsServiceImpl();
            }
        }
        return service;
    }

    public static void reset() {
        service = null;
    }

    /**
     * Call this to capture a request for processing later - see
     * {@link RequestMetricsFilter}
     *
     * @param request the request to capture.
     */
    public abstract void captureRequest(HttpServletRequest request);

    /**
     * To record this data you must call {@Link MetricsService#captureRequest} first.
     */
    public abstract void captureRequestResponseTimeMetrics();

    /**
     * To record this data you must call {@Link MetricsService#captureRequest} first.
     */
    public abstract void captureErrorMetrics();

    /**
     * Capture metrics for Ping times.
     */
    public abstract void capturePing(long ms);


    public abstract void captureCollectionPublishMetrics(String collectionId, long timeTaken, int numberOfFiles, String collectionType, Date publishDate);
}
