package com.github.onsdigital.zebedee.content.page.statistics.document.figure.table;

import com.github.onsdigital.zebedee.content.page.base.PageType;
import com.github.onsdigital.zebedee.content.page.statistics.document.figure.FigureBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Table extends FigureBase {

    private String title;
    private String html;
    private String filename;
    private Boolean firstLineTitle;
    private String headerRows;
    private TableModifications modifications;

    @Override
    public PageType getType() {
        return PageType.table;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Boolean getFirstLineTitle() {
        return firstLineTitle;
    }

    public void setFirstLineTitle(Boolean firstLineTitle) {
        this.firstLineTitle = firstLineTitle;
    }

    public String getHeaderRows() {
        return headerRows;
    }

    public void setHeaderRows(String headerRows) {
        this.headerRows = headerRows;
    }

    public TableModifications getModifications() {
        return modifications;
    }

    public void setModifications(TableModifications modifications) {
        this.modifications = modifications;
    }
}