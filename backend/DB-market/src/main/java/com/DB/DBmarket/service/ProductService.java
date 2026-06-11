package com.DB.DBmarket.service;

import com.DB.DBmarket.pojo.Product;
import com.DB.DBmarket.pojo.SearchProductRequest;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    List<Product> getProductById(String productId);
    Integer getPrice(String prod);

    boolean addProduct(Product product);
    boolean deleteProduct(String id);
    List<Product> searchProduct(SearchProductRequest searchProductRequest);

    void updateState(String id, Integer state);
    void updateProduct(Product product);

    void calculateRefundAndComp();
}
