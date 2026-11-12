package com.assignment.printxpress.model;

public class SavedDesign {
    public final long id;
    public final String name;
    public final String uri;
    public final String notes;
    public final String createdAt;

    public SavedDesign(long id, String name, String uri, String notes, String createdAt) {
        this.id = id;
        this.name = name;
        this.uri = uri;
        this.notes = notes;
        this.createdAt = createdAt;
    }
}
