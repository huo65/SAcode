package com.DB.DBmarket;

import com.DB.DBmarket.mapper.ProdImgMapper;
import com.DB.DBmarket.mapper.ProductMapper;
import com.DB.DBmarket.mapper.OrderReviewMapper;
import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.OrderReview;
import com.DB.DBmarket.pojo.Address;
import com.DB.DBmarket.pojo.Product;
import com.DB.DBmarket.pojo.User;
import com.DB.DBmarket.pojo.restaurant.RestaurantDetail;
import com.DB.DBmarket.pojo.restaurant.RestaurantSummary;
import com.DB.DBmarket.service.impl.RestaurantServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTests {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProdImgMapper prodImgMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private OrderReviewMapper orderReviewMapper;

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    @Test
    void listRestaurantsGroupsProductsByMerchant() {
        User merchantA = buildMerchant("mer001", "Spicy House", "great food", 0);
        User merchantB = buildMerchant("mer002", "Sweet Cafe", "dessert", 0);
        Product productA1 = buildProduct("prod001", "Noodle", "spicy noodle", "mer001", "Hot", 18);
        Product productA2 = buildProduct("prod002", "Rice", "fried rice", "mer001", "Rice", 12);
        Product productB = buildProduct("prod003", "Cake", "cheese cake", "mer002", "Dessert", 25);

        when(userMapper.listUsers("mer")).thenReturn(Arrays.asList(merchantA, merchantB));
        when(productMapper.searchProduct(any())).thenReturn(Arrays.asList(productA1, productA2, productB));
        when(prodImgMapper.getProdImg("prod001")).thenReturn(Collections.singletonList("/img/noodle.png"));
        when(prodImgMapper.getProdImg("prod002")).thenReturn(Collections.singletonList("/img/rice.png"));
        when(prodImgMapper.getProdImg("prod003")).thenReturn(Collections.singletonList("/img/cake.png"));
        when(userMapper.getAddressById("mer001")).thenReturn(Collections.singletonList(new Address("addr1", "mer001", "Downtown")));
        when(userMapper.getAddressById("mer002")).thenReturn(Collections.singletonList(new Address("addr2", "mer002", "Uptown")));
        when(orderReviewMapper.listByMerchantId("mer001")).thenReturn(Collections.singletonList(buildReview("order-1", "cus001", "mer001", 4)));
        when(orderReviewMapper.listByMerchantId("mer002")).thenReturn(Collections.emptyList());
        when(userMapper.getNameById("cus001")).thenReturn("customer");

        List<RestaurantSummary> restaurants = restaurantService.listRestaurants(null, null);

        assertEquals(2, restaurants.size());
        RestaurantSummary spicyHouse = restaurants.get(0);
        assertEquals("Spicy House", spicyHouse.getName());
        assertEquals(Integer.valueOf(2), spicyHouse.getMenuCount());
        assertEquals(Integer.valueOf(12), spicyHouse.getMinPrice());
        assertEquals("Downtown", spicyHouse.getAddress());
        assertEquals(Double.valueOf(4.0), spicyHouse.getAverageScore());
    }

    @Test
    void listRestaurantsSupportsKeywordFiltering() {
        User merchant = buildMerchant("mer001", "Spicy House", "great food", 0);
        Product product = buildProduct("prod001", "Noodle", "spicy noodle", "mer001", "Hot", 18);

        when(userMapper.listUsers("mer")).thenReturn(Collections.singletonList(merchant));
        when(productMapper.searchProduct(any())).thenReturn(Collections.singletonList(product));
        when(prodImgMapper.getProdImg("prod001")).thenReturn(Collections.singletonList("/img/noodle.png"));
        when(userMapper.getAddressById("mer001")).thenReturn(Collections.singletonList(new Address("addr1", "mer001", "Downtown")));
        when(orderReviewMapper.listByMerchantId("mer001")).thenReturn(Collections.emptyList());

        List<RestaurantSummary> restaurants = restaurantService.listRestaurants("spicy", null);
        List<RestaurantSummary> filteredOut = restaurantService.listRestaurants("burger", null);

        assertEquals(1, restaurants.size());
        assertEquals(0, filteredOut.size());
    }

    @Test
    void getRestaurantInfoReturnsMerchantMenu() {
        User merchant = buildMerchant("mer001", "Spicy House", "great food", 0);
        Product productA = buildProduct("prod001", "Noodle", "spicy noodle", "mer001", "Hot", 18);
        Product productB = buildProduct("prod002", "Rice", "fried rice", "mer001", "Rice", 12);

        when(userMapper.listUsers("mer")).thenReturn(Collections.singletonList(merchant));
        when(productMapper.searchProduct(any())).thenReturn(Arrays.asList(productA, productB));
        when(prodImgMapper.getProdImg("prod001")).thenReturn(Collections.singletonList("/img/noodle.png"));
        when(prodImgMapper.getProdImg("prod002")).thenReturn(Collections.singletonList("/img/rice.png"));
        when(userMapper.getAddressById("mer001")).thenReturn(Collections.singletonList(new Address("addr1", "mer001", "Downtown")));
        when(orderReviewMapper.listByMerchantId("mer001")).thenReturn(Collections.singletonList(buildReview("order-1", "cus001", "mer001", 5)));
        when(userMapper.getNameById("cus001")).thenReturn("customer");

        RestaurantDetail detail = restaurantService.getRestaurantInfo("mer001");

        assertNotNull(detail);
        assertEquals("Spicy House", detail.getName());
        assertEquals(2, detail.getProductList().size());
        assertEquals(Integer.valueOf(12), detail.getMinPrice());
        assertEquals(1, detail.getReviewList().size());
    }

    @Test
    void getRestaurantInfoReturnsNullForUnknownMerchant() {
        when(userMapper.listUsers("mer")).thenReturn(Collections.emptyList());

        RestaurantDetail detail = restaurantService.getRestaurantInfo("mer404");

        assertNull(detail);
    }

    private User buildMerchant(String id, String name, String description, Integer disabled) {
        User merchant = new User();
        merchant.setId(id);
        merchant.setType("mer");
        merchant.setName(name);
        merchant.setDescription(description);
        merchant.setPortrait("/img/" + id + ".png");
        merchant.setDisabled(disabled);
        return merchant;
    }

    private Product buildProduct(String id, String name, String description, String mer, String category, Integer price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription(description);
        product.setMer(mer);
        product.setCatName(category);
        product.setPrice(price);
        product.setState(1);
        return product;
    }

    private OrderReview buildReview(String orderId, String cus, String mer, Integer score) {
        OrderReview review = new OrderReview();
        review.setOrderId(orderId);
        review.setCus(cus);
        review.setMer(mer);
        review.setScore(score);
        review.setContent("good");
        return review;
    }
}
