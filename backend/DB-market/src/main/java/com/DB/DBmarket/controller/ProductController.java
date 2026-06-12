package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.Product;
import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.SearchProductRequest;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.CurrentUserHolder;
import com.DB.DBmarket.service.OperationsService;
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
    @Resource(name = "OperationsService")
    private OperationsService operationsService;

    @PostMapping("/add")
    public Result addProduct(@RequestBody Product product) {
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.isMerchant() && !currentUser.isAdmin()) return Result.error("Only merchants can add products.");
        if (!currentUser.isAdmin()) product.setMer(currentUser.getId());
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
        CurrentUser currentUser = CurrentUserHolder.require();
        Product product = null;
        List<Product> products = productService.getProductById(id);
        if (products != null && !products.isEmpty()) product = products.get(0);
        if (product == null) return Result.error("Product does not exist.");
        if (!currentUser.isAdmin() && (!currentUser.isMerchant() || !currentUser.getId().equals(product.getMer()))) {
            return Result.error("No permission to delete this product.");
        }
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
        CurrentUser currentUser = CurrentUserHolder.get();
        searchProductRequest.setId(currentUser == null ? null : currentUser.getId());
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
        CurrentUser currentUser = CurrentUserHolder.require();
        List<Product> products = productService.getProductById(product.getId());
        if (products == null || products.isEmpty()) return Result.error("Product does not exist.");
        Product existing = products.get(0);
        if (!currentUser.isAdmin() && (!currentUser.isMerchant() || !currentUser.getId().equals(existing.getMer()))) {
            return Result.error("No permission to update this product.");
        }
        if (!currentUser.isAdmin()) product.setMer(currentUser.getId());
        log.info("update product:{}",product);
        productService.updateProduct(product);
        return Result.success();
    }

    @GetMapping("/check")
    public Result checkProduct(@RequestParam("id") String id,@RequestParam("state") Integer state){
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.isAdmin()) return Result.error("Only admin can check products.");
        if (!operationsService.hasPermission(currentUser, "admin.action.product.audit")) {
            return Result.error("Admin permission denied: admin.action.product.audit");
        }
        log.info("admin check products");
        if (state < -1 || state > 1) return Result.error("Invalid product state!");
        productService.updateState(id, state);
        operationsService.recordAudit(currentUser, "PRODUCT_AUDIT", "product", id, id,
                "商品审核状态更新为 " + state, "SUCCESS");
        return Result.success();
    }
}
