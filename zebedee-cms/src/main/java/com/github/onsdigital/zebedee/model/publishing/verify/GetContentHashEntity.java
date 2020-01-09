package com.github.onsdigital.zebedee.model.publishing.verify;

public class GetContentHashEntity {

    private String uri;
    private String transactionId;
    private String hash;

    public GetContentHashEntity(final String uri, final String transactionId, final String hash) {
        this.uri = uri;
        this.transactionId = transactionId;
        this.hash = hash;
    }

    public String getUri() {
        return this.uri;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public String getHash() {
        return this.hash;
    }
}
