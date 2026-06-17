package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.OrderReview;
import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.utils.CurrentUserHolder;
import com.DB.DBmarket.service.OrderReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PostMapping("/reply")
    public Result replyReview(@RequestBody Map<String, Object> request) {
        try {
            String orderId = request.get("orderId") == null ? null : String.valueOf(request.get("orderId"));
            if (orderId == null || orderId.trim().isEmpty()) {
                orderId = request.get("reviewId") == null ? null : String.valueOf(request.get("reviewId"));
            }
            String replyContent = request.get("replyContent") == null ? null : String.valueOf(request.get("replyContent"));
            if (replyContent == null || replyContent.trim().isEmpty()) {
                replyContent = request.get("reply") == null ? null : String.valueOf(request.get("reply"));
            }
            OrderReview saved = orderReviewService.replyReview(CurrentUserHolder.require(), orderId, replyContent);
            Map<String, Object> data = new HashMap<>();
            data.put("review", saved);
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/merchant/list")
    public Result listMerchantReviews(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Integer rating,
                                      @RequestParam(required = false) String replyStatus) {
        try {
            return Result.success(orderReviewService.listMerchantReviewBoard(
                    CurrentUserHolder.require(),
                    keyword,
                    rating,
                    replyStatus
            ));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/customer/list")
    public Result listCustomerReviews(@RequestParam(required = false) String usrId,
                                      @RequestParam(required = false) String userId) {
        try {
            String resolvedUserId = usrId == null || usrId.trim().isEmpty() ? userId : usrId;
            return Result.success(orderReviewService.listCustomerReviewBoard(
                    CurrentUserHolder.require(),
                    resolvedUserId
            ));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
