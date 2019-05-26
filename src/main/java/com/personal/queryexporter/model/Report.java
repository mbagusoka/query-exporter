package com.personal.queryexporter.model;

import java.util.List;

public class Report {

    private String query;
    private List<String> params;
    private String fileName;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
