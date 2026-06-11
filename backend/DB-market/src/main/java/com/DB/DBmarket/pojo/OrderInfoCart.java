package com.DB.DBmarket.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfoCart {
    private String cus;
    private String mer;
    private String prod;
    @JsonProperty("prod_num")
    private Integer prodNum;
    private String time;
    @JsonProperty("deli_addr")
    private String deliAddr;
    @JsonProperty("rec_addr")
    private String recAddr;
    private int state;
    private int account;
}
