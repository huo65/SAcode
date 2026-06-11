package com.DB.DBmarket.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartInfo {

    //商品类别名称
    @JsonProperty("cat_name")
    private String catName;
     //商品描述
    private String description;
    //取该商品的第一张图片
    private String firstImage;
     //商品id
    private String id;
     //商家id
    private String mer;
     //商家名称
    @JsonProperty("mer_name")
    private String merName;
     //商品名称
    private String name;
    /**
     * 库存量
     */
    private long number;
    /**
     * 加入到购物车的数量
     */
    private long numberInCart;
    /**
     * 商品价格
     */
    private long price;
    /**
     * -1未通过 0待审核 1审核通过
     */
    private long state;
}
