package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.mapper.OrderReviewMapper;
import com.DB.DBmarket.mapper.ProdImgMapper;
import com.DB.DBmarket.mapper.ProductMapper;
import com.DB.DBmarket.mapper.RestaurantStoreMapper;
import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.Address;
import com.DB.DBmarket.pojo.OrderReview;
import com.DB.DBmarket.pojo.Product;
import com.DB.DBmarket.pojo.SearchProductRequest;
import com.DB.DBmarket.pojo.User;
import com.DB.DBmarket.pojo.restaurant.RestaurantDetail;
import com.DB.DBmarket.pojo.restaurant.RestaurantStore;
import com.DB.DBmarket.pojo.restaurant.RestaurantSummary;
import com.DB.DBmarket.service.RestaurantService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service("RestaurantService")
public class RestaurantServiceImpl implements RestaurantService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProdImgMapper prodImgMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private OrderReviewMapper orderReviewMapper;

    @Resource
    private RestaurantStoreMapper restaurantStoreMapper;

    @Override
    public List<RestaurantSummary> listRestaurants(String keyword, String category, String sortBy, Boolean onlyOpen, Double minScore, String customerId) {
        ensureRestaurantTable();
        Map<String, User> merchantMap = loadEnabledMerchants();
        List<Product> products = loadVisibleProducts(category, merchantMap);
        Map<String, List<Product>> productGroups = groupProductsByMerchant(products);
        Map<String, RestaurantStore> storeMap = loadStoreMap();
        List<RestaurantSummary> restaurants = new ArrayList<>();
        String keywordNormalized = normalize(keyword);

        for (Map.Entry<String, List<Product>> entry : productGroups.entrySet()) {
            User merchant = merchantMap.get(entry.getKey());
            if (merchant == null) {
                continue;
            }
            RestaurantStore store = mergeStore(storeMap.get(entry.getKey()), merchant, entry.getValue());
            RestaurantSummary summary = buildSummary(store, merchant, entry.getValue(), customerId);
            if (!matchesKeyword(summary, entry.getValue(), keywordNormalized)) {
                continue;
            }
            if (Boolean.TRUE.equals(onlyOpen) && (summary.getStatus() == null || summary.getStatus() != 1)) {
                continue;
            }
            if (minScore != null && summary.getAverageScore() != null && summary.getAverageScore() < minScore) {
                continue;
            }
            restaurants.add(summary);
        }

        restaurants.sort(resolveComparator(sortBy));
        return restaurants;
    }

    @Override
    public RestaurantDetail getRestaurantInfo(String restaurantId, String customerId) {
        ensureRestaurantTable();
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            return null;
        }
        Map<String, User> merchantMap = loadEnabledMerchants();
        User merchant = merchantMap.get(restaurantId);
        if (merchant == null) {
            RestaurantStore store = getStoreByIdOrMerchant(restaurantId);
            if (store != null) {
                merchant = merchantMap.get(store.getMerchantId());
                restaurantId = store.getMerchantId();
            }
        }
        if (merchant == null) {
            return null;
        }

        List<Product> products = loadVisibleProducts(null, merchantMap);
        List<Product> merchantProducts = new ArrayList<>();
        for (Product product : products) {
            if (merchant.getId().equals(product.getMer())) {
                merchantProducts.add(product);
            }
        }
        if (merchantProducts.isEmpty()) {
            return null;
        }

        merchantProducts.sort(Comparator.comparing(Product::getCatName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Product::getName, String.CASE_INSENSITIVE_ORDER));

        RestaurantStore store = mergeStore(getStoreByIdOrMerchant(restaurantId), merchant, merchantProducts);
        RestaurantSummary summary = buildSummary(store, merchant, merchantProducts, customerId);
        RestaurantDetail detail = new RestaurantDetail();
        detail.setId(summary.getId());
        detail.setMerchantId(summary.getMerchantId());
        detail.setName(summary.getName());
        detail.setPortrait(summary.getPortrait());
        detail.setDescription(summary.getDescription());
        detail.setAddress(summary.getAddress());
        detail.setCover(summary.getCover());
        detail.setMenuCount(summary.getMenuCount());
        detail.setMinPrice(summary.getMinPrice());
        detail.setAverageScore(summary.getAverageScore());
        detail.setReviewCount(summary.getReviewCount());
        detail.setStatus(summary.getStatus());
        detail.setStatusText(summary.getStatusText());
        detail.setBusinessHours(summary.getBusinessHours());
        detail.setDeliveryFee(summary.getDeliveryFee());
        detail.setMinOrderAmount(summary.getMinOrderAmount());
        detail.setDistanceKm(summary.getDistanceKm());
        detail.setDeliveryEtaMinutes(summary.getDeliveryEtaMinutes());
        detail.setNotice(summary.getNotice());
        detail.setDeliveryPolicy(summary.getDeliveryPolicy());
        detail.setPromoText(summary.getPromoText());
        detail.setCategories(summary.getCategories());
        detail.setServiceTags(summary.getServiceTags());
        detail.setMenuCategories(summary.getMenuCategories());
        detail.setProductList(merchantProducts);
        detail.setReviewList(resolveReviews(merchant.getId()));
        detail.setStoreInfo(store);
        return detail;
    }

    @Override
    public RestaurantStore getManageInfo(String merchantId) {
        ensureRestaurantTable();
        if (isBlank(merchantId)) {
            return null;
        }
        User merchant = userMapper.getInfo(merchantId);
        if (merchant == null || !"mer".equals(merchant.getType())) {
            return null;
        }
        List<Product> products = loadVisibleProducts(null, Collections.singletonMap(merchantId, merchant));
        return mergeStore(restaurantStoreMapper.getByMerchantId(merchantId), merchant, products);
    }

    @Override
    public void updateRestaurantInfo(String merchantId, RestaurantStore payload) {
        ensureRestaurantTable();
        User merchant = userMapper.getInfo(merchantId);
        if (merchant == null || !"mer".equals(merchant.getType())) {
            throw new IllegalArgumentException("merchant not found");
        }
        List<Product> products = loadVisibleProducts(null, Collections.singletonMap(merchantId, merchant));
        RestaurantStore merged = mergeStore(restaurantStoreMapper.getByMerchantId(merchantId), merchant, products);
        merged.setId(merchantId);
        merged.setMerchantId(merchantId);
        merged.setName(firstNonBlank(payload.getName(), merged.getName()));
        merged.setLogo(firstNonBlank(payload.getLogo(), merged.getLogo()));
        merged.setCover(firstNonBlank(payload.getCover(), merged.getCover()));
        merged.setDescription(firstNonBlank(payload.getDescription(), merged.getDescription()));
        merged.setNotice(firstNonBlank(payload.getNotice(), merged.getNotice()));
        merged.setStatus(payload.getStatus() == null ? merged.getStatus() : payload.getStatus());
        merged.setBusinessHours(firstNonBlank(payload.getBusinessHours(), merged.getBusinessHours()));
        merged.setDeliveryFee(payload.getDeliveryFee() == null ? merged.getDeliveryFee() : payload.getDeliveryFee());
        merged.setMinOrderAmount(payload.getMinOrderAmount() == null ? merged.getMinOrderAmount() : payload.getMinOrderAmount());
        merged.setServiceRadiusKm(payload.getServiceRadiusKm() == null ? merged.getServiceRadiusKm() : payload.getServiceRadiusKm());
        merged.setDeliveryEtaMinutes(payload.getDeliveryEtaMinutes() == null ? merged.getDeliveryEtaMinutes() : payload.getDeliveryEtaMinutes());
        merged.setFeatureTags(firstNonBlank(payload.getFeatureTags(), merged.getFeatureTags()));
        merged.setMenuCategories(firstNonBlank(payload.getMenuCategories(), merged.getMenuCategories()));
        merged.setAddressText(firstNonBlank(payload.getAddressText(), merged.getAddressText()));
        merged.setDeliveryPolicy(firstNonBlank(payload.getDeliveryPolicy(), merged.getDeliveryPolicy()));
        merged.setPromoText(firstNonBlank(payload.getPromoText(), merged.getPromoText()));
        restaurantStoreMapper.upsert(merged);
    }

    private void ensureRestaurantTable() {
        try {
            restaurantStoreMapper.createTableIfMissing();
        } catch (Exception ignore) {
            // fallback to legacy aggregation when DDL is unavailable
        }
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
            List<Product> merchantProducts = productGroups.computeIfAbsent(product.getMer(), key -> new ArrayList<>());
            merchantProducts.add(product);
        }
        return productGroups;
    }

    private Map<String, RestaurantStore> loadStoreMap() {
        Map<String, RestaurantStore> storeMap = new HashMap<>();
        try {
            List<RestaurantStore> stores = restaurantStoreMapper.listAll();
            if (stores != null) {
                for (RestaurantStore store : stores) {
                    storeMap.put(store.getMerchantId(), store);
                }
            }
        } catch (Exception ignore) {
            // fallback to legacy aggregation
        }
        return storeMap;
    }

    private RestaurantStore getStoreByIdOrMerchant(String id) {
        try {
            RestaurantStore store = restaurantStoreMapper.getById(id);
            if (store != null) {
                return store;
            }
            return restaurantStoreMapper.getByMerchantId(id);
        } catch (Exception ignore) {
            return null;
        }
    }

    private RestaurantStore mergeStore(RestaurantStore existing, User merchant, List<Product> products) {
        RestaurantStore store = existing == null ? new RestaurantStore() : existing;
        store.setId(firstNonBlank(store.getId(), merchant.getId()));
        store.setMerchantId(firstNonBlank(store.getMerchantId(), merchant.getId()));
        store.setName(firstNonBlank(store.getName(), merchant.getName()));
        store.setLogo(firstNonBlank(store.getLogo(), merchant.getPortrait()));
        store.setCover(firstNonBlank(store.getCover(), resolveCover(products, merchant.getPortrait())));
        store.setDescription(firstNonBlank(store.getDescription(), merchant.getDescription(), "课堂展示版精选门店"));
        store.setNotice(firstNonBlank(store.getNotice(), "欢迎光临，本店支持课堂展示版完整下单体验。"));
        store.setStatus(store.getStatus() == null ? 1 : store.getStatus());
        store.setBusinessHours(firstNonBlank(store.getBusinessHours(), "10:00-21:30"));
        store.setDeliveryFee(store.getDeliveryFee() == null ? 4 : store.getDeliveryFee());
        store.setMinOrderAmount(store.getMinOrderAmount() == null ? resolveMinPrice(products) : store.getMinOrderAmount());
        store.setServiceRadiusKm(store.getServiceRadiusKm() == null ? 5.0 : store.getServiceRadiusKm());
        store.setDeliveryEtaMinutes(store.getDeliveryEtaMinutes() == null ? 28 : store.getDeliveryEtaMinutes());
        store.setFeatureTags(firstNonBlank(store.getFeatureTags(), String.join(",", resolveDefaultFeatureTags(products))));
        store.setMenuCategories(firstNonBlank(store.getMenuCategories(), String.join(",", resolveCategories(products))));
        store.setAddressText(firstNonBlank(store.getAddressText(), resolveAddress(merchant.getId())));
        store.setDeliveryPolicy(firstNonBlank(store.getDeliveryPolicy(), "满额起送，骑手接单后按课堂展示版路线送达。"));
        store.setPromoText(firstNonBlank(store.getPromoText(), "新客首单享课堂展示版门店体验"));
        return store;
    }

    private RestaurantSummary buildSummary(RestaurantStore store, User merchant, List<Product> products, String customerId) {
        RestaurantSummary summary = new RestaurantSummary();
        summary.setId(store.getId());
        summary.setMerchantId(merchant.getId());
        summary.setName(store.getName());
        summary.setPortrait(firstNonBlank(store.getLogo(), merchant.getPortrait()));
        summary.setDescription(store.getDescription());
        summary.setAddress(store.getAddressText());
        summary.setCover(firstNonBlank(store.getCover(), resolveCover(products, merchant.getPortrait())));
        summary.setMenuCount(products.size());
        summary.setMinPrice(resolveMinPrice(products));
        List<OrderReview> reviews = resolveReviews(merchant.getId());
        summary.setAverageScore(resolveAverageScore(reviews));
        summary.setReviewCount(reviews.size());
        summary.setCategories(resolveCategories(products));
        summary.setStatus(store.getStatus());
        summary.setStatusText(resolveStatusText(store.getStatus()));
        summary.setBusinessHours(store.getBusinessHours());
        summary.setDeliveryFee(store.getDeliveryFee());
        summary.setMinOrderAmount(store.getMinOrderAmount());
        summary.setDistanceKm(resolveDistanceKm(customerId, merchant.getId(), store));
        summary.setDeliveryEtaMinutes(store.getDeliveryEtaMinutes());
        summary.setNotice(store.getNotice());
        summary.setDeliveryPolicy(store.getDeliveryPolicy());
        summary.setPromoText(store.getPromoText());
        summary.setServiceTags(splitCsv(store.getFeatureTags()));
        summary.setMenuCategories(resolveMenuCategories(store, products));
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

    private List<String> resolveDefaultFeatureTags(List<Product> products) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        tags.add("品牌门店");
        tags.add("课堂展示推荐");
        if (products.size() >= 6) {
            tags.add("菜单丰富");
        }
        if (resolveMinPrice(products) <= 20) {
            tags.add("平价优选");
        }
        return new ArrayList<>(tags);
    }

    private List<OrderReview> resolveReviews(String merchantId) {
        List<OrderReview> reviews = orderReviewMapper.listByMerchantId(merchantId);
        if (reviews == null) {
            return new ArrayList<>();
        }
        for (OrderReview review : reviews) {
            review.setCustomerName(userMapper.getNameById(review.getCus()));
        }
        return reviews;
    }

    private Double resolveAverageScore(List<OrderReview> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 4.6;
        }
        double total = 0;
        for (OrderReview review : reviews) {
            total += review.getScore() == null ? 0 : review.getScore();
        }
        return Math.round((total / reviews.size()) * 10.0) / 10.0;
    }

    private List<String> resolveMenuCategories(RestaurantStore store, List<Product> products) {
        List<String> categories = splitCsv(store.getMenuCategories());
        if (!categories.isEmpty()) {
            return categories;
        }
        return resolveCategories(products);
    }

    private Double resolveDistanceKm(String customerId, String merchantId, RestaurantStore store) {
        String base = firstNonBlank(store.getAddressText(), merchantId);
        if (!isBlank(customerId)) {
            List<Address> customerAddresses = userMapper.getAddressById(customerId);
            if (customerAddresses != null && !customerAddresses.isEmpty()) {
                base = base + "|" + customerAddresses.get(0).getLocation();
            }
        }
        int hash = Math.abs(base.hashCode());
        return Math.round((1.2 + (hash % 45) / 10.0) * 10.0) / 10.0;
    }

    private Comparator<RestaurantSummary> resolveComparator(String sortBy) {
        String normalized = normalize(sortBy);
        if ("score".equals(normalized)) {
            return Comparator.comparing(RestaurantSummary::getAverageScore, Comparator.nullsLast(Double::compareTo)).reversed()
                    .thenComparing(RestaurantSummary::getName, String.CASE_INSENSITIVE_ORDER);
        }
        if ("distance".equals(normalized)) {
            return Comparator.comparing(RestaurantSummary::getDistanceKm, Comparator.nullsLast(Double::compareTo))
                    .thenComparing(RestaurantSummary::getName, String.CASE_INSENSITIVE_ORDER);
        }
        if ("price".equals(normalized)) {
            return Comparator.comparing(RestaurantSummary::getMinPrice, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(RestaurantSummary::getName, String.CASE_INSENSITIVE_ORDER);
        }
        if ("reviews".equals(normalized)) {
            return Comparator.comparing(RestaurantSummary::getReviewCount, Comparator.nullsLast(Integer::compareTo)).reversed()
                    .thenComparing(RestaurantSummary::getName, String.CASE_INSENSITIVE_ORDER);
        }
        return Comparator.comparing(RestaurantSummary::getStatus, Comparator.nullsLast(Integer::compareTo)).reversed()
                .thenComparing(RestaurantSummary::getAverageScore, Comparator.nullsLast(Double::compareTo)).reversed()
                .thenComparing(RestaurantSummary::getReviewCount, Comparator.nullsLast(Integer::compareTo)).reversed()
                .thenComparing(RestaurantSummary::getName, String.CASE_INSENSITIVE_ORDER);
    }

    private boolean matchesKeyword(RestaurantSummary summary, List<Product> products, String keywordNormalized) {
        if (isBlank(keywordNormalized)) {
            return true;
        }
        if (containsText(summary.getName(), keywordNormalized)
                || containsText(summary.getDescription(), keywordNormalized)
                || containsText(summary.getAddress(), keywordNormalized)
                || containsText(summary.getNotice(), keywordNormalized)
                || containsText(summary.getPromoText(), keywordNormalized)) {
            return true;
        }
        for (String category : summary.getCategories()) {
            if (containsText(category, keywordNormalized)) {
                return true;
            }
        }
        for (String category : summary.getMenuCategories()) {
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

    private List<String> splitCsv(String source) {
        if (isBlank(source)) {
            return new ArrayList<>();
        }
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (String part : source.split(",")) {
            String value = part == null ? "" : part.trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return new ArrayList<>(values);
    }

    private String resolveStatusText(Integer status) {
        return status != null && status == 1 ? "营业中" : "休息中";
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
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
