package com.DB.DBmarket.pojo.restaurant;

import com.DB.DBmarket.pojo.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RestaurantDetail extends RestaurantSummary {
    private List<Product> productList = new ArrayList<>();
}
