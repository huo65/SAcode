package com.DB.DBmarket.pojo.restaurant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantStore {
    private String id;
    private String merchantId;
    private String name;
    private String logo;
    private String cover;
    private String description;
    private String notice;
    private Integer status;
    private String businessHours;
    private Integer deliveryFee;
    private Integer minOrderAmount;
    private Double serviceRadiusKm;
    private Integer deliveryEtaMinutes;
    private String featureTags;
    private String menuCategories;
    private String addressText;
    private String deliveryPolicy;
    private String promoText;
}
