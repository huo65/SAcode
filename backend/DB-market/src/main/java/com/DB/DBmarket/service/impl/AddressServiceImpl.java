package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.mapper.AddressMapper;
import com.DB.DBmarket.pojo.Address;
import com.DB.DBmarket.service.AddressService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
    @Resource
    private AddressMapper addressMapper;
    @Override
    public List<String> getAddressById(String id) {
        return addressMapper.getAddress(id);
    }

    @Override
    public Boolean addUsrAddress(Address address) {
        return addressMapper.addAddress(address);
    }

    @Override
    public Integer countAddress() {
        return addressMapper.countAddress();
    }

    @Override
    public void deleteAllAddress(){
        addressMapper.deleteAllAddress();;
    }

    @Override
    public void deleteAddressById(String addressId){
        addressMapper.deleteAddressById(addressId);
    }
}
