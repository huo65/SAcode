package com.DB.DBmarket.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//接受搜索商品的请求数据的类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchProductRequest {
    //商品种类
    private String category;
    //商品最大价格
    private Integer max_price;
    //商品最小价格
    private Integer min_price;
    //价格排序 1为升序，0为降序
    private Integer priceOrder;
    //商品名称
    private String name;
    //商品状态
    private Integer state;
    //用户id
    private String id;
    //用户类型
    private String userType;
    //格式化，如果子段为空，设置为null
    public void format() {
        if ("".equals(category)) {
            category = null;
        }
        if ("".equals(name)) {
            name = null;
        }
    }

}
