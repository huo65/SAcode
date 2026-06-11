package com.DB.DBmarket.mapper;

import com.DB.DBmarket.pojo.Address;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AddressMapper {
    @Select("select addr_id from address where usr = #{id} ")
    List<String> getAddress(String id);

    @Insert("insert into address(addr_id,usr, location) VALUES (#{addrId}, #{usr}, #{location});")
    Boolean addAddress(Address address);
   @Select("select  count(*) from address")
    Integer countAddress();
    @Select("select location from address where addr_id = #{id} ")
    String getAddressByAddressId(String id);
    @Select("select usr from address where addr_id = #{addressId}")
    String getAddressOwner(String addressId);
    @Delete("DELETE FROM address")
    void deleteAllAddress();
    @Delete("DELETE FROM address WHERE addr_id = #{addressId}")
    void deleteAddressById(String addressId);
}
