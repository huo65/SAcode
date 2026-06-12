package com.DB.DBmarket.pojo.restaurant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSummary {
    private String id;
    private String merchantId;
    private String name;
    private String portrait;
    private String description;
    private String address;
    private String cover;
    private Integer menuCount;
    private Integer minPrice;
    private Double averageScore;
    private Integer reviewCount;
    private Integer status;
    private String statusText;
    private String businessHours;
    private Integer deliveryFee;
    private Integer minOrderAmount;
    private Double distanceKm;
    private Integer deliveryEtaMinutes;
    private String notice;
    private String deliveryPolicy;
    private String promoText;
    private List<String> categories = new ArrayList<>();
    private List<String> serviceTags = new ArrayList<>();
    private List<String> menuCategories = new ArrayList<>();
}
