package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.mapper.ProdImgMapper;
import com.DB.DBmarket.mapper.ProductMapper;
import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.Address;
import com.DB.DBmarket.pojo.Product;
import com.DB.DBmarket.pojo.SearchProductRequest;
import com.DB.DBmarket.pojo.User;
import com.DB.DBmarket.pojo.restaurant.RestaurantDetail;
import com.DB.DBmarket.pojo.restaurant.RestaurantSummary;
import com.DB.DBmarket.service.RestaurantService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

@Service("RestaurantService")
public class RestaurantServiceImpl implements RestaurantService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProdImgMapper prodImgMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public List<RestaurantSummary> listRestaurants(String keyword, String category) {
        Map<String, User> merchantMap = loadEnabledMerchants();
        List<Product> products = loadVisibleProducts(category, merchantMap);
        Map<String, List<Product>> productGroups = groupProductsByMerchant(products);
        List<RestaurantSummary> restaurants = new ArrayList<>();
        String keywordNormalized = normalize(keyword);

        for (Map.Entry<String, List<Product>> entry : productGroups.entrySet()) {
            User merchant = merchantMap.get(entry.getKey());
            if (merchant == null) {
                continue;
            }
            RestaurantSummary summary = buildSummary(merchant, entry.getValue());
            if (matchesKeyword(summary, entry.getValue(), keywordNormalized)) {
                restaurants.add(summary);
            }
        }

        restaurants.sort(Comparator.comparing(RestaurantSummary::getName, String.CASE_INSENSITIVE_ORDER));
        return restaurants;
    }

    @Override
    public RestaurantDetail getRestaurantInfo(String merchantId) {
        if (merchantId == null || merchantId.trim().isEmpty()) {
            return null;
        }
        Map<String, User> merchantMap = loadEnabledMerchants();
        User merchant = merchantMap.get(merchantId);
        if (merchant == null) {
            return null;
        }

        List<Product> products = loadVisibleProducts(null, merchantMap);
        List<Product> merchantProducts = new ArrayList<>();
        for (Product product : products) {
            if (merchantId.equals(product.getMer())) {
                merchantProducts.add(product);
            }
        }
        if (merchantProducts.isEmpty()) {
            return null;
        }

        merchantProducts.sort(Comparator.comparing(Product::getCatName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Product::getName, String.CASE_INSENSITIVE_ORDER));

        RestaurantSummary summary = buildSummary(merchant, merchantProducts);
        RestaurantDetail detail = new RestaurantDetail();
        detail.setId(summary.getId());
        detail.setName(summary.getName());
        detail.setPortrait(summary.getPortrait());
        detail.setDescription(summary.getDescription());
        detail.setAddress(summary.getAddress());
        detail.setCover(summary.getCover());
        detail.setMenuCount(summary.getMenuCount());
        detail.setMinPrice(summary.getMinPrice());
        detail.setCategories(summary.getCategories());
        detail.setProductList(merchantProducts);
        return detail;
    }

    private Map<String, User> loadEnabledMerchants() {
        List<User> merchants = userMapper.listUsers("mer");
        Map<String, User> merchantMap = new HashMap<>();
        for (User merchant : merchants) {
            if (merchant.getDisabled() != null && merchant.getDisabled() == 1) {
                continue;
            }
            merchantMap.put(merchant.getId(), merchant);
        }
        return merchantMap;
    }

    private List<Product> loadVisibleProducts(String category, Map<String, User> merchantMap) {
        SearchProductRequest request = new SearchProductRequest();
        request.setUserType("cus");
        request.setCategory(isBlank(category) ? null : category.trim());
        List<Product> products = productMapper.searchProduct(request);
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }

        List<Product> visibleProducts = new ArrayList<>();
        for (Product product : products) {
            User merchant = merchantMap.get(product.getMer());
            if (merchant == null) {
                continue;
            }
            product.setImageList(prodImgMapper.getProdImg(product.getId()));
            product.setMerName(merchant.getName());
            visibleProducts.add(product);
        }
        return visibleProducts;
    }

    private Map<String, List<Product>> groupProductsByMerchant(List<Product> products) {
        Map<String, List<Product>> productGroups = new HashMap<>();
        for (Product product : products) {
            List<Product> merchantProducts = productGroups.get(product.getMer());
            if (merchantProducts == null) {
                merchantProducts = new ArrayList<>();
                productGroups.put(product.getMer(), merchantProducts);
            }
            merchantProducts.add(product);
        }
        return productGroups;
    }

    private RestaurantSummary buildSummary(User merchant, List<Product> products) {
        RestaurantSummary summary = new RestaurantSummary();
        summary.setId(merchant.getId());
        summary.setName(merchant.getName());
        summary.setPortrait(merchant.getPortrait());
        summary.setDescription(merchant.getDescription());
        summary.setAddress(resolveAddress(merchant.getId()));
        summary.setCover(resolveCover(products, merchant.getPortrait()));
        summary.setMenuCount(products.size());
        summary.setMinPrice(resolveMinPrice(products));
        summary.setCategories(resolveCategories(products));
        return summary;
    }

    private String resolveAddress(String merchantId) {
        List<Address> addresses = userMapper.getAddressById(merchantId);
        if (addresses == null || addresses.isEmpty()) {
            return "";
        }
        return addresses.get(0).getLocation();
    }

    private String resolveCover(List<Product> products, String portrait) {
        for (Product product : products) {
            if (product.getImageList() != null && !product.getImageList().isEmpty()) {
                return product.getImageList().get(0);
            }
        }
        return portrait;
    }

    private Integer resolveMinPrice(List<Product> products) {
        Integer minPrice = null;
        for (Product product : products) {
            if (product.getPrice() == null) {
                continue;
            }
            if (minPrice == null || product.getPrice() < minPrice) {
                minPrice = product.getPrice();
            }
        }
        return minPrice == null ? 0 : minPrice;
    }

    private List<String> resolveCategories(List<Product> products) {
        LinkedHashSet<String> categories = new LinkedHashSet<>();
        for (Product product : products) {
            if (!isBlank(product.getCatName())) {
                categories.add(product.getCatName());
            }
        }
        return new ArrayList<>(categories);
    }

    private boolean matchesKeyword(RestaurantSummary summary, List<Product> products, String keywordNormalized) {
        if (isBlank(keywordNormalized)) {
            return true;
        }
        if (containsText(summary.getName(), keywordNormalized)
                || containsText(summary.getDescription(), keywordNormalized)
                || containsText(summary.getAddress(), keywordNormalized)) {
            return true;
        }
        for (String category : summary.getCategories()) {
            if (containsText(category, keywordNormalized)) {
                return true;
            }
        }
        for (Product product : products) {
            if (containsText(product.getName(), keywordNormalized)
                    || containsText(product.getDescription(), keywordNormalized)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsText(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String normalize(String text) {
        return text == null ? null : text.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
