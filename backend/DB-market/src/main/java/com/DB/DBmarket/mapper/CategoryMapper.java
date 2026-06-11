package com.DB.DBmarket.mapper;

import com.DB.DBmarket.pojo.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM category")
        List<Category> getAllCategories();
    @Select("select count(*) from market.category where name=#{category}")
        Integer search(String category);
    @Insert("insert into market.category values(#{category})")
        void addCategory(String category);
    @Delete("delete from market.category where name=#{category}")
        void deleteCategory(String category);
}
