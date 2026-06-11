package com.DB.DBmarket.service;

import com.DB.DBmarket.pojo.restaurant.RestaurantDetail;
import com.DB.DBmarket.pojo.restaurant.RestaurantSummary;

import java.util.List;

public interface RestaurantService {
    List<RestaurantSummary> listRestaurants(String keyword, String category);

    RestaurantDetail getRestaurantInfo(String merchantId);
}
