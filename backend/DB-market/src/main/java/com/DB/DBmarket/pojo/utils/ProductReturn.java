package com.DB.DBmarket.pojo.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductReturn {
    //商品ID
    private  String id;
    //商品名称
    private  String name;
    //商品描述
    private String description;
    //商品价格
    private Integer price;
    //商家id
    private String mer;
    //商品类别
    @JsonProperty("cat_name")
    private String catName;
    //商品库存
    private Integer number;
    //商品状态(-1 未通过审核 0审核中 1审核通过)
    private Integer state;
    //商品图片
    @JsonProperty("image_list")
    private List<String> imageList;
    private String merName;

    //商品在此次订单的购买数量
    @JsonProperty("prod_num")
    private Integer prodNum;
    //2024.5.22
    private Integer salesRefund;//该商品退货量
    private String rateRefund;//该商品退货率  %
    private Integer complain;//该商品投诉量
    private String  complainRate;//该商品投诉率

    //直接用toString时，Integer属性为空会报错，所以重写toString方法
    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", mer='" + mer + '\'' +
                ", catName='" + catName + '\'' +
                ", number=" + number + '\'' +
                ", state=" + state + '\'' +
                ", imageList=" + imageList +
                '}';
    }
}
