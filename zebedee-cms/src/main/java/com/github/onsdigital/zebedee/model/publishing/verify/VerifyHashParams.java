package com.github.onsdigital.zebedee.model.publishing.verify;

public class VerifyHashParams {

    private String collectionID;
    private String host;
    private String transactionId;
    private String uri;

    public VerifyHashParams(final String collectionID, final String host, final String transactionId, final String uri) {
        this.collectionID = collectionID;
        this.host = host;
        this.transactionId = transactionId;
        this.uri = uri;
    }

    public String getCollectionID() {
        return this.collectionID;
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
}
