package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.restaurant.RestaurantDetail;
import com.DB.DBmarket.pojo.restaurant.RestaurantStore;
import com.DB.DBmarket.pojo.restaurant.RestaurantSummary;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.CurrentUserHolder;
import com.DB.DBmarket.service.OperationsService;
import com.DB.DBmarket.service.RestaurantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @Resource(name = "OperationsService")
    private OperationsService operationsService;

    @GetMapping("/list")
    public Result listRestaurants(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String category,
                                  @RequestParam(required = false) String sortBy,
                                  @RequestParam(required = false) Boolean onlyOpen,
                                  @RequestParam(required = false) Double minScore) {
        CurrentUser currentUser = CurrentUserHolder.get();
        String customerId = currentUser != null && currentUser.isCustomer() ? currentUser.getId() : null;
        log.info("list restaurants keyword:{}, category:{}, sortBy:{}, onlyOpen:{}, minScore:{}", keyword, category, sortBy, onlyOpen, minScore);
        List<RestaurantSummary> restaurants = restaurantService.listRestaurants(keyword, category, sortBy, onlyOpen, minScore, customerId);
        Map<String, Object> data = new HashMap<>();
        data.put("restaurant_list", restaurants);
        return Result.success(data);
    }

    @GetMapping("/info")
    public Result getRestaurantInfo(@RequestParam String id) {
        CurrentUser currentUser = CurrentUserHolder.get();
        String customerId = currentUser != null && currentUser.isCustomer() ? currentUser.getId() : null;
        log.info("get restaurant info by id:{}", id);
        RestaurantDetail restaurant = restaurantService.getRestaurantInfo(id, customerId);
        if (restaurant == null) {
            return Result.error("restaurant not found");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("restaurant_info", restaurant);
        return Result.success(data);
    }

    @GetMapping("/manage/info")
    public Result getManageInfo() {
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.isMerchant() && !currentUser.isAdmin()) {
            return Result.error("Only merchant can manage restaurant info.");
        }
        if (currentUser.isMerchant() && !operationsService.hasPermission(currentUser, "merchant.action.store.manage")) {
            return Result.error("Merchant permission denied: merchant.action.store.manage");
        }
        RestaurantStore store = restaurantService.getManageInfo(currentUser.getId());
        if (store == null) {
            return Result.error("restaurant not found");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("restaurant_manage_info", store);
        return Result.success(data);
    }

    @PostMapping("/manage/update")
    public Result updateManageInfo(@RequestBody RestaurantStore store) {
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.isMerchant() && !currentUser.isAdmin()) {
            return Result.error("Only merchant can update restaurant info.");
        }
        if (currentUser.isMerchant() && !operationsService.hasPermission(currentUser, "merchant.action.store.manage")) {
            return Result.error("Merchant permission denied: merchant.action.store.manage");
        }
        restaurantService.updateRestaurantInfo(currentUser.getId(), store);
        operationsService.recordAudit(currentUser, "STORE_UPDATE", "restaurant", currentUser.getId(),
                store == null ? currentUser.getId() : store.getName(),
                "更新门店资料", "SUCCESS");
        return Result.success();
    }
}
