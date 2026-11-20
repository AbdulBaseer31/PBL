package com.vrs.model;

public class Customer {
    private String customerId;
    private String name;
    private int age;
    private String address;
    private String contactNumber;
    private String password;
    private String role;

    public Customer(String customerId, String name, int age, String address, String contactNumber, String password, String role) {
        this.customerId = customerId;
        this.name = name;
        this.age = age;
        this.address = address;
        this.contactNumber = contactNumber;
        this.password = password;
        this.role = role;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getAddress() { return address; }
    public String getContactNumber() { return contactNumber; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}

