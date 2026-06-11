package com.DB.DBmarket.service;

import com.DB.DBmarket.pojo.OrderReview;
import com.DB.DBmarket.pojo.utils.CurrentUser;

import java.util.List;

public interface OrderReviewService {
    OrderReview submitReview(CurrentUser currentUser, OrderReview orderReview);

    List<OrderReview> listMerchantReviews(String merchantId);

    boolean hasReview(String orderId);
}
