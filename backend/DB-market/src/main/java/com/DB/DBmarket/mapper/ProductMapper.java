package com.DB.DBmarket.mapper;

import com.DB.DBmarket.pojo.Product;
import com.DB.DBmarket.pojo.SearchProductRequest;
import com.DB.DBmarket.pojo.utils.ProductReturn;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;

@Mapper
public interface ProductMapper {
    @Insert("insert into product(id,name,description,price,mer,cat_name,number,state) values(#{id},#{name},#{description},#{price},#{mer},#{cat_name},#{number},#{state})")
    void insertProduct(String id, String name, String description, Integer price, String mer, String cat_name, Integer number, Integer state);

    @Delete("delete from product where id=#{id}")
    void deleteProduct(String id);

    @SelectProvider(type = ProductMapper.ProductProvider.class, method = "searchProduct")
    List<Product> searchProduct(SearchProductRequest searchProductRequest);


    @Select("select price from market.product where id=#{prod}")
    Integer getPrice(String prod);

    @Select("SELECT * FROM product")
    List<Product> getAllProducts();

    @Select("SELECT * FROM product WHERE id = #{prod}")
    List<Product> getProductById(String prod);

    @Update("update product set state = #{state} where id = #{id}")
    void updateStateById(String id,Integer state);

    @UpdateProvider(type = ProductMapper.ProductProvider.class, method = "updateProduct")
    void updateProduct(Product product);

    @Select("select image from prod_img where prod = #{prod} ")
    List<String> getFirstImg(String prod);
    @Select("SELECT * FROM product WHERE id = #{prod}")
    Product getOneProductById(String prod);

    @Select("SELECT * FROM product WHERE id = #{prod}")
    ProductReturn getOneProductReturnById(String prod);

    void calculateProdRefundAndComp();
    @Select("SELECT Id from market.product")
    List<String> getAllProductsId();
    @Update("UPDATE product SET sales_refund=#{refundNum}, rate_refund=#{refundRate},complain=#{compNum}," +
            "complain_rate=#{compRate} WHERE id=#{id}")
    void updateProductRefundAndComp(String id, Integer refundNum, String refundRate, Integer compNum, String compRate);

    class ProductProvider {
        public static String searchProduct(SearchProductRequest searchProductRequest) {
            return new SQL(){
                {
                    SELECT("*");
                    FROM("product");
                    if (searchProductRequest.getUserType().equals("cus")){
                        WHERE("state = 1");
                    }else if (searchProductRequest.getUserType().equals("mer")){
                        WHERE("(mer=#{id} or state=1)");
                    }
                    if (searchProductRequest.getCategory() != null) {
                        WHERE("cat_name=#{category}");
                    }
                    if (searchProductRequest.getMax_price() != null) {
                        WHERE("price<=#{max_price}");
                    }
                    if (searchProductRequest.getMin_price() != null) {
                        WHERE("price>=#{min_price}");
                    }
                    if (searchProductRequest.getName() != null) {
                        WHERE("name LIKE CONCAT('%', #{name}, '%')");
                    }
                    if (searchProductRequest.getState() != null) {
                        WHERE("state=#{state}");
                    }
                    if (searchProductRequest.getPriceOrder() != null) {
                        if (searchProductRequest.getPriceOrder() == 1) {
                            ORDER_BY("price ASC");
                        } else {
                            ORDER_BY("price DESC");
                        }
                    }
                }
            }.toString();
        }

        public static String updateProduct(Product product) {
            return new SQL(){
                {
                    UPDATE("product");
                    SET("id = #{id}");

                    if (product.getName() != null && !product.getName().isEmpty()) {
                        SET("name = #{name}");
                    }
                    if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                        SET("description = #{description}");
                    }
                    if (product.getPrice() != null) {
                        SET("price = #{price}");
                    }
                    if (product.getCatName() != null && !product.getCatName().isEmpty()) {
                        SET("cat_name = #{catName}");
                    }
                    if (product.getNumber() != null) {
                        SET("number = #{number}");
                    }
                    WHERE("id = #{id}");
                }
            }.toString();
        }
    }
    @Delete("delete from market.product where cat_name=#{category}")
    void deleteProductByCategory(String category);

}
