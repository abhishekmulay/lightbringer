package com.ir.lightbringer.models;

import java.util.List;

/**
 * Created by Abhishek Mulay on 5/10/17.
 */
public class HW1Model {
    // required fields
    private String documentId;
    private String text;

    // extra fields
    private String fileId;
    private String first;
    private String second;
    private String dateline;
    private List<String> heads;
    private List<String> bylines;

    public HW1Model(String docId, String text, String fileId, String first, String second, String dateline, List<String> heads, List<String> bylines) {
        this.documentId = docId;
        this.text = text;
        this.fileId = fileId;
        this.first = first;
        this.second = second;
        this.heads = heads;
        this.bylines = bylines;
        this.dateline = dateline;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public String getDateline() {
        return dateline;
    }

    public void setDateline(String dateline) {
        this.dateline = dateline;
    }

    public List<String> getHeads() {
        return heads;
    }

    public void setHeads(List<String> heads) {
        this.heads = heads;
    }

    public List<String> getBylines() {
        return bylines;
    }

    public void setBylines(List<String> bylines) {
        this.bylines = bylines;
    }

    @Override
    public String toString() {
        return "HW1Model{" +
                "documentId='" + documentId + '\'' +
                ", text='" + text + '\'' +
                ", fileId='" + fileId + '\'' +
                ", first='" + first + '\'' +
                ", second='" + second + '\'' +
                ", dateline='" + dateline + '\'' +
                ", heads=" + heads +
                ", bylines=" + bylines +
                '}';
    }
}
