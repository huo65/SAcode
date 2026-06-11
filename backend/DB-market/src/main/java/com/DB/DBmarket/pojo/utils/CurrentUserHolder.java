package com.DB.DBmarket.pojo.utils;

public class CurrentUserHolder {
    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private CurrentUserHolder() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static CurrentUser require() {
        CurrentUser user = get();
        if (user == null) {
            throw new IllegalStateException("Please log in first.");
        }
        return user;
    }
}
