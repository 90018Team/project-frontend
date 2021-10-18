package com.example.help.ui.contact;

public class Contact {
    private String name;
    private String phoneNumber;

    public Contact(String contactName, String contactPhone) {
        name = contactName;
        phoneNumber = contactPhone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phone) {
        this.phoneNumber = phone;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
