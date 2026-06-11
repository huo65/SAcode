package com.DB.DBmarket.service;

import com.DB.DBmarket.pojo.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();

    boolean addCategory(String category);

    void deleteCategory(String category);
}
