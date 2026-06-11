package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.Product;
import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.SearchProductRequest;
import com.DB.DBmarket.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/product")
public class ProductController {

    @Resource(name = "ProductService")
    private ProductService productService;

    @PostMapping("/add")
    public Result addProduct(@RequestBody Product product) {
        log.info("addProduct");
        log.info(product.toString());
        if(productService.addProduct(product)) {
            log.info("add product success");
            return Result.success();
        } else {
            log.info("add product failed");
            return Result.error("add product failed");
        }
    }

    @DeleteMapping("/delete")
    public Result deleteProduct(@RequestParam("id") String id) {
        log.info("deleteProduct");
        log.info(id);
        if(productService.deleteProduct(id)) {
            log.info("delete product success");
            return Result.success();
        } else {
            log.info("delete product failed");
            return Result.error("delete product failed");
        }
    }

    @GetMapping("/search")
    public Result searchProduct(@ModelAttribute SearchProductRequest searchProductRequest) {
        searchProductRequest.format();
        log.info("search product:{}",searchProductRequest);
        productService.calculateRefundAndComp();
        List<Product> products = productService.searchProduct(searchProductRequest);
        if(products != null) {
            log.info("search product success");
            Map<String, Object> data = new HashMap<>();
            data.put("prod_list", products);
            return Result.success(data);
        } else {
            log.info("search product failed");
            return Result.error("search product failed");
        }
    }

    @GetMapping("/info")
    public Result getProductInfo(@RequestParam(required = false) String productId) {
        List<Product> products;
        if (productId == null || productId.trim().isEmpty()) {
            products = productService.getAllProducts();
        } else {
            products = productService.getProductById(productId);
        }
        if(products != null) {
            log.info("get product info success");
            Map<String, Object> data = new HashMap<>();
            data.put("prod_list", products);
            return Result.success(data);
        } else {
            log.info("get product info failed");
            return Result.error("get product info failed");
        }
    }

    @PostMapping("/update")
    public Result updateProduct(@RequestBody Product product){
        log.info("update product:{}",product);
        productService.updateProduct(product);
        return Result.success();
    }

    @GetMapping("/check")
    public Result checkProduct(@RequestParam("id") String id,@RequestParam("state") Integer state){
        log.info("admin check products");
        if (state < -1 || state > 1) return Result.error("Invalid product state!");
        productService.updateState(id, state);
        return Result.success();
    }
}
