package com.DB.DBmarket.mapper;

import com.DB.DBmarket.pojo.restaurant.RestaurantStore;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RestaurantStoreMapper {
    @Update("CREATE TABLE IF NOT EXISTS market.restaurant (" +
            "id VARCHAR(64) PRIMARY KEY COMMENT 'restaurant id'," +
            "merchant_id VARCHAR(64) NOT NULL COMMENT 'merchant user id'," +
            "name VARCHAR(128) NOT NULL COMMENT 'restaurant name'," +
            "logo VARCHAR(512) COMMENT 'logo'," +
            "cover VARCHAR(512) COMMENT 'cover'," +
            "description TEXT COMMENT 'description'," +
            "notice TEXT COMMENT 'notice'," +
            "status TINYINT NOT NULL DEFAULT 1 COMMENT '1 open, 0 closed'," +
            "business_hours VARCHAR(128) COMMENT 'business hours'," +
            "delivery_fee INT DEFAULT 0 COMMENT 'delivery fee'," +
            "min_order_amount INT DEFAULT 0 COMMENT 'minimum order amount'," +
            "service_radius_km DOUBLE DEFAULT 5 COMMENT 'service radius'," +
            "delivery_eta_minutes INT DEFAULT 30 COMMENT 'eta minutes'," +
            "feature_tags VARCHAR(500) COMMENT 'display tags separated by comma'," +
            "menu_categories VARCHAR(500) COMMENT 'menu categories separated by comma'," +
            "address_text VARCHAR(500) COMMENT 'restaurant address'," +
            "delivery_policy TEXT COMMENT 'delivery policy'," +
            "promo_text VARCHAR(255) COMMENT 'promo text'," +
            "UNIQUE KEY uk_restaurant_merchant (merchant_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店表'")
    void createTableIfMissing();

    @Select("select * from market.restaurant where id=#{id}")
    RestaurantStore getById(String id);

    @Select("select * from market.restaurant where merchant_id=#{merchantId}")
    RestaurantStore getByMerchantId(String merchantId);

    @Select("select * from market.restaurant order by merchant_id")
    List<RestaurantStore> listAll();

    @Insert("INSERT INTO market.restaurant(id, merchant_id, name, logo, cover, description, notice, status, business_hours, delivery_fee, min_order_amount, service_radius_km, delivery_eta_minutes, feature_tags, menu_categories, address_text, delivery_policy, promo_text) " +
            "VALUES(#{id}, #{merchantId}, #{name}, #{logo}, #{cover}, #{description}, #{notice}, #{status}, #{businessHours}, #{deliveryFee}, #{minOrderAmount}, #{serviceRadiusKm}, #{deliveryEtaMinutes}, #{featureTags}, #{menuCategories}, #{addressText}, #{deliveryPolicy}, #{promoText}) " +
            "ON DUPLICATE KEY UPDATE " +
            "name=VALUES(name), logo=VALUES(logo), cover=VALUES(cover), description=VALUES(description), notice=VALUES(notice), status=VALUES(status), business_hours=VALUES(business_hours), delivery_fee=VALUES(delivery_fee), min_order_amount=VALUES(min_order_amount), service_radius_km=VALUES(service_radius_km), delivery_eta_minutes=VALUES(delivery_eta_minutes), feature_tags=VALUES(feature_tags), menu_categories=VALUES(menu_categories), address_text=VALUES(address_text), delivery_policy=VALUES(delivery_policy), promo_text=VALUES(promo_text)")
    void upsert(RestaurantStore store);
}
