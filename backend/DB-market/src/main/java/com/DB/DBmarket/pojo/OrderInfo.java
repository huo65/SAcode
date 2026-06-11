package com.DB.DBmarket.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo {
    //订单id
    private  String id;
    //买家Id
    private  String cus;
    //卖家Id
    private String mer;
    //商品Id
    private  String prod;
    //下单购买数量
    @JsonProperty("prod_num")
    private Integer prodNum;
    //下单时间
    private LocalDateTime time;
    //发货地址id
    @JsonProperty("deli_addr")
    private String deliAddr;
    //收货地址id
    @JsonProperty("rec_addr")
    private  String recAddr;
    //订单状态   -3 已退货/退款  -2退货中 -1下单未支付 0已支付 1已发货 2已收货
    private Integer state;
    //订单金额
    private Integer account;
    //2024.5.22
    //是否被投诉  0未被投诉，1被投诉
    private String  complain;
    //投诉理由
    private String complainReason;
    //退款理由，请求退款
    private String refundReason;
}
