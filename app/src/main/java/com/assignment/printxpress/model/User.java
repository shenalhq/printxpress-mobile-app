package com.assignment.printxpress.model;

/**
 * Simple model class used to hold PrintXpress data.
 */
public class User {
    public long id;
    public String name;
    public String email;
    public String phone;
    public String address;
    public String profileUri;

    public User(long id, String name, String email, String phone, String address, String profileUri) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.profileUri = profileUri == null ? "" : profileUri;
    }
}
