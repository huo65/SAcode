package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.OrderReview;
import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.utils.CurrentUserHolder;
import com.DB.DBmarket.service.OrderReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/review")
public class OrderReviewController {

    @Resource(name = "OrderReviewService")
    private OrderReviewService orderReviewService;

    @PostMapping("/add")
    public Result addReview(@RequestBody OrderReview orderReview) {
        try {
            OrderReview saved = orderReviewService.submitReview(CurrentUserHolder.require(), orderReview);
            Map<String, Object> data = new HashMap<>();
            data.put("review", saved);
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
