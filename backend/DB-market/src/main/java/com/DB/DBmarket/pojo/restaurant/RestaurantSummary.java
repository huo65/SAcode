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
    private String name;
    private String portrait;
    private String description;
    private String address;
    private String cover;
    private Integer menuCount;
    private Integer minPrice;
    private List<String> categories = new ArrayList<>();
}
