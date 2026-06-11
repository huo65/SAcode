package com.DB.DBmarket.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProdImgMapper {
    @Insert("insert into prod_img(prod,image) values(#{prod},#{image})")
    void insertProdImg(String prod, String image);

    @Delete("delete from prod_img where prod=#{prod}")
    void deleteProdImg(String prod);

    @Select("select image from prod_img where prod=#{prod}")
    List<String> getProdImg(String prod);
    @Delete("delete from market.prod_img where prod in (select id from market.product where cat_name=#{category})")
    void deleteProdImgByCategory(String category);
}
