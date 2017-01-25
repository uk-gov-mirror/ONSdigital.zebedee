package com.github.onsdigital.zebedee.content.page.base;

import com.github.onsdigital.zebedee.content.base.Content;
import com.github.onsdigital.zebedee.content.partial.Link;

import java.net.URI;
import java.util.List;

/**
 * Created by bren on 10/06/15.
 * <p>
 * This is the generic pageData object that that has common properties of all page types on the website
 */
public abstract class Page extends Content {

    protected PageType type;

    private URI uri;

    private PageDescription description;

    private List<Link> topics;
    
    /**
     * Contains the raw text from the data.json file.
     */
    private String pageData;

    public Page() {
        this.type = getType();
    }

    public String getPageData() {
        return pageData;
    }

    public void setPageData(final String pageData) {
        this.pageData = pageData;
    }

    public abstract PageType getType();

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

    public List<Link> getTopics() {
        return topics;
    }

    public void setTopics(List<Link> topics) {
        this.topics = topics;
    }
}
