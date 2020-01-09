package com.github.onsdigital.zebedee.model.publishing.verify;

public class ContentHashEntity {

    private String uri;
    private String hash;

    public ContentHashEntity(String uri, String hash) {
        this.uri = uri;
        this.hash = hash;
    }

    public String getUri() {
        return uri;
    }

    public String getHash() {
        return hash;
    }
}
