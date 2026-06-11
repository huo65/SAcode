package com.DB.DBmarket.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReview {
    private String orderId;
    private String cus;
    private String mer;
    private Integer score;
    private String content;
    private LocalDateTime createdTime;
    private String replyContent;
    private LocalDateTime replyTime;
    private String customerName;
}
