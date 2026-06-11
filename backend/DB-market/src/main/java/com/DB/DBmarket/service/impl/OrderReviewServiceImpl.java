package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.mapper.OrderInfoMapper;
import com.DB.DBmarket.mapper.OrderReviewMapper;
import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.OrderReview;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.service.OrderReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service("OrderReviewService")
public class OrderReviewServiceImpl implements OrderReviewService {

    @Resource
    private OrderReviewMapper orderReviewMapper;

    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderReview submitReview(CurrentUser currentUser, OrderReview orderReview) {
        if (orderReview == null || orderReview.getOrderId() == null || orderReview.getOrderId().trim().isEmpty()) {
            throw new IllegalArgumentException("Order id is required.");
        }
        if (!currentUser.isCustomer()) {
            throw new IllegalArgumentException("Only customers can review orders.");
        }
        if (orderReview.getScore() == null || orderReview.getScore() < 1 || orderReview.getScore() > 5) {
            throw new IllegalArgumentException("Score must be between 1 and 5.");
        }
        if (orderReview.getContent() == null || orderReview.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Review content is required.");
        }

        List<OrderInfo> orderRows = orderInfoMapper.getOrdersById(orderReview.getOrderId());
        if (orderRows == null || orderRows.isEmpty()) {
            throw new IllegalArgumentException("Order does not exist.");
        }
        OrderInfo first = orderRows.get(0);
        if (!currentUser.getId().equals(first.getCus())) {
            throw new IllegalArgumentException("No permission to review this order.");
        }
        if (first.getState() == null || first.getState() != 2) {
            throw new IllegalArgumentException("Only completed orders can be reviewed.");
        }
        if (orderReviewMapper.getByOrderId(orderReview.getOrderId()) != null) {
            throw new IllegalArgumentException("This order has already been reviewed.");
        }

        orderReview.setCus(currentUser.getId());
        orderReview.setMer(first.getMer());
        orderReview.setContent(orderReview.getContent().trim());
        orderReview.setCreatedTime(LocalDateTime.now());
        orderReviewMapper.insert(orderReview);

        OrderReview saved = orderReviewMapper.getByOrderId(orderReview.getOrderId());
        if (saved != null) {
            saved.setCustomerName(userMapper.getNameById(saved.getCus()));
        }
        return saved;
    }

    @Override
    public List<OrderReview> listMerchantReviews(String merchantId) {
        if (merchantId == null || merchantId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderReview> reviews = orderReviewMapper.listByMerchantId(merchantId);
        if (reviews == null) {
            return new ArrayList<>();
        }
        for (OrderReview review : reviews) {
            review.setCustomerName(userMapper.getNameById(review.getCus()));
        }
        return reviews;
    }

    @Override
    public boolean hasReview(String orderId) {
        return orderId != null && orderReviewMapper.getByOrderId(orderId) != null;
    }
}
