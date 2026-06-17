package com.DB.DBmarket.service;

import com.DB.DBmarket.pojo.OrderReview;
import com.DB.DBmarket.pojo.utils.CurrentUser;

import java.util.List;
import java.util.Map;

public interface OrderReviewService {
    OrderReview submitReview(CurrentUser currentUser, OrderReview orderReview);

    OrderReview replyReview(CurrentUser currentUser, String orderId, String replyContent);

    Map<String, Object> listMerchantReviewBoard(CurrentUser currentUser, String keyword, Integer rating, String replyStatus);

    Map<String, Object> listCustomerReviewBoard(CurrentUser currentUser, String userId);

    List<OrderReview> listMerchantReviews(String merchantId);

    boolean hasReview(String orderId);
}
