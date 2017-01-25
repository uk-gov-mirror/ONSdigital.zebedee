package com.github.onsdigital.zebedee.search.model;

import com.github.onsdigital.zebedee.content.page.base.PageDescription;
import com.github.onsdigital.zebedee.content.page.base.PageType;
import com.github.onsdigital.zebedee.content.page.statistics.dataset.DownloadSection;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bren on 03/09/15.
 */
public class SearchDocument {
    private URI uri;
    private PageType type;
    private PageDescription description;
    private List<URI> topics;
    private List<String> searchBoost;
    /**
     * The document being index will contain link to downloadable PDFs
     * and XLS we need to index these as part of the overall document
     */
    private List<DownloadSection> downloads = new ArrayList<>();
    private String pageData;


    public PageDescription getDescription() {
        return description;
    }

    public void setDescription(PageDescription description) {
        this.description = description;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public PageType getType() {
        return type;
    }

    public void setType(PageType type) {
        this.type = type;
    }

    public List<URI> getTopics() {
        return topics;
    }

    public void setTopics(List<URI> topics) {
        this.topics = topics;
    }

    public List<String> getSearchBoost() {
        return searchBoost;
    }

    public void setSearchBoost(List<String> searchBoost) {
        this.searchBoost = searchBoost;
    }

    public List<DownloadSection> getDownloads() {
        return downloads;
    }

    public void setDownloads(final List<DownloadSection> downloads) {
        this.downloads = downloads;
    }

    public String getPageData() {
        return pageData;
    }

    public void setPageData(final String pageData) {
        this.pageData = pageData;
    }
}
