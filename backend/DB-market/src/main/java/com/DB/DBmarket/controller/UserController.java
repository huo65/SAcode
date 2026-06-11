package com.DB.DBmarket.controller;

import com.DB.DBmarket.mapper.AddressMapper;
import com.DB.DBmarket.pojo.*;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.CurrentUserHolder;
import com.DB.DBmarket.pojo.utils.JwtUtils;
import com.DB.DBmarket.pojo.utils.PayParmBody;
import com.DB.DBmarket.service.AddressService;
import com.DB.DBmarket.service.OrderInfoService;
import com.DB.DBmarket.service.UserService;
import com.alibaba.druid.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Resource
    private OrderInfoService orderInfoService;
    @Resource
    private UserService userService;
    @Resource
    private AddressService addressService;
    @Resource
    private AddressMapper addressMapper;

    @PostMapping("/order")
    public Result UserInOrder(@RequestBody OrderInfo orderInfo) {
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.isCustomer() && !currentUser.isAdmin()) {
            return Result.error("Only customers can create orders.");
        }
        if (!currentUser.isAdmin()) {
            orderInfo.setCus(currentUser.getId());
        }
        log.info("User order:{}",orderInfo);
        if(orderInfoService.addOrder(orderInfo)){
            Map<String,Object> data=new HashMap<>();
            //此时orderInfo已经经过处理，值发生改变
            data.put("order_info",orderInfo);
            data.put("delivery",addressMapper.getAddressByAddressId(orderInfo.getMer()));
            data.put("receive",addressMapper.getAddressByAddressId(orderInfo.getCus()));
            return Result.success(data);
        }
        else return Result.error("Add order failed!");
    }

    @PostMapping("/pay")
    public Result UserPay(@RequestBody PayParmBody payParmBody) {
        try {
            orderInfoService.payOrders(CurrentUserHolder.require(), payParmBody.getOrderIdList());
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }




    //登录
    @PostMapping("/login")
    public Result login(@RequestBody User user){
        log.info("User login:{}",user);
        // 登录成功，生成令牌，下发令牌
        User u = userService.login(user);
            if(u != null){
                Map<String,Object> claims=new HashMap<>();
                claims.put("id",u.getId());
                claims.put("name",u.getName());
                claims.put("type",u.getType());

                String jwt = JwtUtils.generateJwt(claims);
                UserLoginResult userLogin = new UserLoginResult(jwt,u);
                return Result.success(userLogin);
            }
            //登录失败
            return Result.error("Incorrect username or password!");
    }
    @GetMapping("/getinfo")
    public Result GetInfo(@RequestParam(required = false) String id){
        CurrentUser currentUser = CurrentUserHolder.require();
        String targetId = currentUser.isAdmin() && id != null ? id : currentUser.getId();
        return Result.success(userService.getInfo(targetId));
    }

    @PostMapping("/register")
    public Result register(@RequestBody User user){
        log.info("User register:{}",user);
        if (userService.register(user)!=null) return Result.success("Registered successfully!");
        else return Result.error("User name already exists!");
    }

    @PostMapping("/update")
    public Result update(@RequestBody User user){
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.isAdmin() && !currentUser.getId().equals(user.getId())) {
            return Result.error("No permission to update this user.");
        }
        log.info("User modify information:{}",user);
        userService.update(user);
        return Result.success();
    }

    @GetMapping("/address")
    public Result getUserAddress(String id){
        CurrentUser currentUser = CurrentUserHolder.require();
        String targetId = currentUser.isAdmin() && !StringUtils.isEmpty(id) ? id : currentUser.getId();
        log.info("Query the user address based on user ID:{}",targetId);
       // List<String> addressList = userService.getUserAddress(id);
        List<Address> addressList = userService.getUserAddress(targetId);
        return Result.success(addressList);
    }

    @PostMapping("/addAdress")
    public Result addUsrAddressLegacy(@RequestBody Address address){
        return addUsrAddress(address);
    }

    @PostMapping("/addAddress")
    public Result addUsrAddress(@RequestBody Address address){
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.isAdmin()) {
            address.setUsr(currentUser.getId());
        }
        Integer id = addressService.countAddress();
        address.setAddrId(Integer.toString(id+1));
        addressService.addUsrAddress(address);
        return Result.success();
    }

    @DeleteMapping("/deleteAddress")
    public Result deleteUsrAddress(@RequestParam(required = false) String addressId){
        if(StringUtils.isEmpty(addressId)){
            CurrentUser currentUser = CurrentUserHolder.require();
            if (!currentUser.isAdmin()) return Result.error("Only admin can delete all addresses.");
            addressService.deleteAllAddress();
            return Result.success("delete all addresses successfully !");
        }
        else{
            CurrentUser currentUser = CurrentUserHolder.require();
            String owner = addressMapper.getAddressOwner(addressId);
            if (!currentUser.isAdmin() && !currentUser.getId().equals(owner)) {
                return Result.error("No permission to delete this address.");
            }
            addressService.deleteAddressById(addressId);
            return Result.success("delete address successfully!");
        }
    }

    @GetMapping("/list")
    public Result listUsers(@RequestParam(required = false) String type) {
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.isAdmin()) return Result.error("Only admin can list users.");
        return Result.success(userService.listUsers(type));
    }

    @PostMapping("/disabled")
    public Result updateDisabled(@RequestBody Map<String, Object> request) {
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!currentUser.isAdmin()) return Result.error("Only admin can update user status.");
        String id = (String) request.get("id");
        Integer disabled = (Integer) request.get("disabled");
        if (id == null || disabled == null || (disabled != 0 && disabled != 1)) {
            return Result.error("Invalid user status request.");
        }
        return userService.updateDisabled(id, disabled) ? Result.success() : Result.error("Update user status failed.");
    }

}
