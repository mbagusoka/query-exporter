package com.personal.queryexporter.model;

public class Report {

    private String query;
    private String fileName;
    private int rowLimit;
    private String pathOutput;
    private boolean zipFlag;
    private boolean passwordFlag;
    private String password;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getRowLimit() {
        return rowLimit;
    }

    public void setRowLimit(int rowLimit) {
        this.rowLimit = rowLimit;
    }

    public String getPathOutput() {
        return pathOutput;
    }

    public void setPathOutput(String pathOutput) {
        this.pathOutput = pathOutput;
    }

    public boolean isZipFlag() {
        return zipFlag;
    }

    public void setZipFlag(boolean zipFlag) {
        this.zipFlag = zipFlag;
    }

    public boolean isPasswordFlag() {
        return passwordFlag;
    }

    public void setPasswordFlag(boolean passwordFlag) {
        this.passwordFlag = passwordFlag;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
