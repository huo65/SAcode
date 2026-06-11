package com.DB.DBmarket.service;

import com.DB.DBmarket.pojo.Address;

import java.util.List;

public interface AddressService {
    List<String> getAddressById(String id);
    Boolean addUsrAddress(Address address);
    Integer countAddress();
    void deleteAllAddress ();
    void deleteAddressById (String addressId);
}
