package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.mapper.OrderInfoMapper;
import com.DB.DBmarket.mapper.OrderReviewMapper;
import com.DB.DBmarket.mapper.ProductMapper;
import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.OrderReview;
import com.DB.DBmarket.pojo.Product;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.service.OrderReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("OrderReviewService")
public class OrderReviewServiceImpl implements OrderReviewService {

    @Resource
    private OrderReviewMapper orderReviewMapper;

    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ProductMapper productMapper;

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
    @Transactional(rollbackFor = Exception.class)
    public OrderReview replyReview(CurrentUser currentUser, String orderId, String replyContent) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order id is required.");
        }
        if (!currentUser.isMerchant()) {
            throw new IllegalArgumentException("Only merchants can reply reviews.");
        }
        if (replyContent == null || replyContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Reply content is required.");
        }

        OrderReview review = orderReviewMapper.getByOrderId(orderId);
        if (review == null) {
            throw new IllegalArgumentException("Review does not exist.");
        }
        if (!currentUser.getId().equals(review.getMer())) {
            throw new IllegalArgumentException("No permission to reply this review.");
        }
        orderReviewMapper.replyReview(orderId, replyContent.trim(), LocalDateTime.now());
        OrderReview saved = orderReviewMapper.getByOrderId(orderId);
        if (saved != null) {
            saved.setCustomerName(userMapper.getNameById(saved.getCus()));
        }
        return saved;
    }

    @Override
    public Map<String, Object> listMerchantReviewBoard(CurrentUser currentUser, String keyword, Integer rating, String replyStatus) {
        if (currentUser == null || !currentUser.isMerchant()) {
            throw new IllegalArgumentException("Only merchants can list merchant reviews.");
        }
        List<OrderReview> reviews = listMerchantReviews(currentUser.getId());
        List<Map<String, Object>> rows = new ArrayList<>();
        int[] distribution = new int[6];
        int goodCount = 0;
        int pendingCount = 0;
        int totalScore = 0;

        for (OrderReview review : reviews) {
            Integer score = review.getScore() == null ? 0 : review.getScore();
            if (score >= 1 && score <= 5) {
                distribution[score]++;
                totalScore += score;
                if (score >= 4) {
                    goodCount++;
                }
            }
            boolean replied = hasText(review.getReplyContent());
            if (!replied) {
                pendingCount++;
            }

            Map<String, Object> row = buildReviewRow(review, replied);
            if (matchesFilters(row, review, keyword, rating, replyStatus, replied)) {
                rows.add(row);
            }
        }

        Map<String, Object> overview = new LinkedHashMap<>();
        int total = reviews.size();
        overview.put("total", total);
        overview.put("average", total == 0 ? 0 : Math.round((totalScore * 10.0) / total) / 10.0);
        overview.put("goodRate", total == 0 ? 0 : Math.round(goodCount * 100.0 / total));
        overview.put("pending", pendingCount);

        List<Map<String, Object>> ratingDistribution = new ArrayList<>();
        for (int star = 5; star >= 1; star--) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("star", star);
            item.put("count", distribution[star]);
            item.put("percent", total == 0 ? 0 : Math.round(distribution[star] * 100.0 / total));
            ratingDistribution.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", rows);
        data.put("overview", overview);
        data.put("ratingDistribution", ratingDistribution);
        return data;
    }

    @Override
    public Map<String, Object> listCustomerReviewBoard(CurrentUser currentUser, String userId) {
        if (currentUser == null || !currentUser.isCustomer()) {
            throw new IllegalArgumentException("Only customers can list customer reviews.");
        }
        String customerId = hasText(userId) ? userId.trim() : currentUser.getId();
        if (!currentUser.getId().equals(customerId)) {
            throw new IllegalArgumentException("No permission to list this customer's reviews.");
        }
        List<OrderReview> reviews = orderReviewMapper.listByCustomerId(customerId);
        if (reviews == null) {
            reviews = Collections.emptyList();
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (OrderReview review : reviews) {
            review.setCustomerName(userMapper.getNameById(review.getCus()));
            Map<String, Object> row = buildReviewRow(review, hasText(review.getReplyContent()));
            row.put("restaurantName", userMapper.getNameById(review.getMer()));
            row.put("shopName", row.get("restaurantName"));
            row.put("createTime", review.getCreatedTime());
            rows.add(row);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", rows);
        data.put("reviews", rows);
        data.put("total", rows.size());
        return data;
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

    private Map<String, Object> buildReviewRow(OrderReview review, boolean replied) {
        List<OrderInfo> orderRows = orderInfoMapper.getOrdersById(review.getOrderId());
        String productName = "-";
        String productImage = "";
        if (orderRows != null && !orderRows.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (OrderInfo order : orderRows) {
                Product product = productMapper.getOneProductById(order.getProd());
                if (product != null && hasText(product.getName())) {
                    names.add(product.getName());
                }
                if (!hasText(productImage)) {
                    List<String> images = productMapper.getFirstImg(order.getProd());
                    if (images != null && !images.isEmpty()) {
                        productImage = images.get(0);
                    }
                }
            }
            if (!names.isEmpty()) {
                productName = names.stream().distinct().collect(Collectors.joining("、"));
            }
        }

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", review.getOrderId());
        row.put("reviewId", review.getOrderId());
        row.put("orderId", review.getOrderId());
        row.put("userName", review.getCustomerName());
        row.put("customerName", review.getCustomerName());
        row.put("productName", productName);
        row.put("productImage", productImage);
        row.put("rating", review.getScore());
        row.put("score", review.getScore());
        row.put("content", review.getContent());
        row.put("tags", Collections.emptyList());
        row.put("images", Collections.emptyList());
        row.put("replied", replied);
        row.put("reply", review.getReplyContent());
        row.put("replyContent", review.getReplyContent());
        row.put("replyTime", review.getReplyTime());
        row.put("createdAt", review.getCreatedTime());
        row.put("createdTime", review.getCreatedTime());
        return row;
    }

    private boolean matchesFilters(Map<String, Object> row, OrderReview review, String keyword, Integer rating, String replyStatus, boolean replied) {
        if (rating != null && rating != 0) {
            int score = review.getScore() == null ? 0 : review.getScore();
            if (rating == -1) {
                if (score < 1 || score > 2) return false;
            } else if (score != rating) {
                return false;
            }
        }
        if (hasText(replyStatus)) {
            String normalized = replyStatus.trim().toLowerCase();
            if (!"all".equals(normalized)) {
                if ("pending".equals(normalized) && replied) return false;
                if ("replied".equals(normalized) && !replied) return false;
            }
        }
        if (hasText(keyword)) {
            String kw = keyword.trim().toLowerCase();
            String haystack = String.valueOf(row.get("orderId")) + " " +
                    String.valueOf(row.get("userName")) + " " +
                    String.valueOf(row.get("productName")) + " " +
                    String.valueOf(row.get("content"));
            return haystack.toLowerCase().contains(kw);
        }
        return true;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
