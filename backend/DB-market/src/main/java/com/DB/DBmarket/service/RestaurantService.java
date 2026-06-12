package com.DB.DBmarket.service;

import com.DB.DBmarket.pojo.restaurant.RestaurantDetail;
import com.DB.DBmarket.pojo.restaurant.RestaurantStore;
import com.DB.DBmarket.pojo.restaurant.RestaurantSummary;

import java.util.List;

public interface RestaurantService {
    List<RestaurantSummary> listRestaurants(String keyword, String category, String sortBy, Boolean onlyOpen, Double minScore, String customerId);

    RestaurantDetail getRestaurantInfo(String restaurantId, String customerId);

    RestaurantStore getManageInfo(String merchantId);

    void updateRestaurantInfo(String merchantId, RestaurantStore store);
}
