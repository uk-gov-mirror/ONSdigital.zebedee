package com.github.onsdigital.zebedee.model.publishing.client;

import java.text.MessageFormat;

public class TrainClientException extends RuntimeException {

    private String host;
    private String transactionId;
    private String uri;
    private int httpStatus;

    public TrainClientException(String host, String transactionId, String uri, int httpStatus) {
        super("train client received an unexpected error");
        this.host = host;
        this.transactionId = transactionId;
        this.uri = uri;
        this.httpStatus = httpStatus;
    }

    public String getHost() {
        return this.host;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public String getUri() {
        return this.uri;
    }

    public int getHttpStatus() {
        return this.httpStatus;
    }

    @Override
    public String getMessage() {
        return MessageFormat.format("{0}, host: {1}, transactionId {2}, uri: {3}", super.getMessage(), host, transactionId, uri);
    }
}
