package com.DB.DBmarket.pojo.utils;

public class CurrentUser {
    private final String id;
    private final String name;
    private final String type;

    public CurrentUser(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isAdmin() {
        return "admin".equals(type);
    }

    public boolean isCustomer() {
        return "cus".equals(type);
    }

    public boolean isMerchant() {
        return "mer".equals(type);
    }

    public boolean isDriver() {
        return "driver".equals(type);
    }
}
