package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.Category;
import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class CategoryController {
    @Resource(name = "CategoryService")
    private CategoryService categoryService;

    @GetMapping("/category")
    public Result getAllCategories() {
        log.info("getAllCategories");
        //List<String> categories = categoryService.getAllCategories();
        List<Category> categories = categoryService.getAllCategories();
        if(categories != null) {
            log.info("get all categories success");
            Map<String,Object> data = new HashMap<>();
            data.put("category_list", categories);
            return Result.success(categories);
        } else {
            log.info("get all categories failed");
            return Result.error("get all categories failed");
        }
    }
    @PostMapping("/category/add")
    public Result addCategory(@RequestBody Category category){
        log.info("add category");
        boolean success=categoryService.addCategory(category.getName());
        if(success) {
            log.info("adding category success");
            return Result.success();
        }
        else {
            log.info("The Category is already existed,adding failed");
            return Result.error("The Category is already existed");
        }
    }
    @DeleteMapping("category/delete")
    public Result deleteCategory(@RequestParam String category) {
        log.info("delete category");
        categoryService.deleteCategory(category);
        return Result.success();
    }
}
