package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.restaurant.RestaurantDetail;
import com.DB.DBmarket.pojo.restaurant.RestaurantSummary;
import com.DB.DBmarket.service.RestaurantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/restaurant")
public class RestaurantController {

    @Resource(name = "RestaurantService")
    private RestaurantService restaurantService;

    @GetMapping("/list")
    public Result listRestaurants(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String category) {
        log.info("list restaurants keyword:{}, category:{}", keyword, category);
        List<RestaurantSummary> restaurants = restaurantService.listRestaurants(keyword, category);
        Map<String, Object> data = new HashMap<>();
        data.put("restaurant_list", restaurants);
        return Result.success(data);
    }

    @GetMapping("/info")
    public Result getRestaurantInfo(@RequestParam String id) {
        log.info("get restaurant info by id:{}", id);
        RestaurantDetail restaurant = restaurantService.getRestaurantInfo(id);
        if (restaurant == null) {
            return Result.error("restaurant not found");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("restaurant_info", restaurant);
        return Result.success(data);
    }
}
