package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.Category;
import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.CurrentUserHolder;
import com.DB.DBmarket.service.CategoryService;
import com.DB.DBmarket.service.OperationsService;
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
    @Resource(name = "OperationsService")
    private OperationsService operationsService;

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
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.isAdmin()) return Result.error("Only admin can manage categories.");
        if (!operationsService.hasPermission(currentUser, "admin.action.category.manage")) {
            return Result.error("Admin permission denied: admin.action.category.manage");
        }
        log.info("add category");
        boolean success=categoryService.addCategory(category.getName());
        if(success) {
            operationsService.recordAudit(currentUser, "CATEGORY_ADD", "category", category.getName(), category.getName(),
                    "新增商品分类", "SUCCESS");
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
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.isAdmin()) return Result.error("Only admin can manage categories.");
        if (!operationsService.hasPermission(currentUser, "admin.action.category.manage")) {
            return Result.error("Admin permission denied: admin.action.category.manage");
        }
        log.info("delete category");
        categoryService.deleteCategory(category);
        operationsService.recordAudit(currentUser, "CATEGORY_DELETE", "category", category, category,
                "删除商品分类", "SUCCESS");
        return Result.success();
    }
}
