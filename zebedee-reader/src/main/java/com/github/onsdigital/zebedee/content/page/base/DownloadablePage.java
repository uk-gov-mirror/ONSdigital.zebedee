package com.github.onsdigital.zebedee.content.page.base;

import com.github.onsdigital.zebedee.content.page.statistics.dataset.DownloadSection;

import java.util.List;

/**
 * Common interface for all pages that have a set of downloads for the page.
 */
public interface DownloadablePage {
    List<DownloadSection> getDownloads();
}
