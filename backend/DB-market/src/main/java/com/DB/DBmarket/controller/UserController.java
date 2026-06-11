package com.DB.DBmarket.controller;

import com.DB.DBmarket.mapper.AddressMapper;
import com.DB.DBmarket.pojo.*;
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
@CrossOrigin("*")
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
        if (payParmBody.getPrice_list().size() != payParmBody.getOrderIdList().size()) {
            return Result.error("The length of price_list and orderIdList must be the same.");
        }

        for (int i = 0; i < payParmBody.getOrderIdList().size(); i++) {
            String order_id = payParmBody.getOrderIdList().get(i);
            ArrayList<Integer> prices = payParmBody.getPrice_list().get(i);
            Integer state = orderInfoService.pay(payParmBody.getId(), prices, order_id);
            switch (state) {
                case -1:
                    return Result.error("You've already paid for one or more orders. You don't have to pay again.");
                case -2:
                    return Result.error("The balance is insufficient for one or more orders.");
                default:
                    orderInfoService.updateOrderState2(order_id, 0, String.valueOf(LocalDateTime.now()));
            }
        }
        return Result.success();
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

                String jwt = JwtUtils.generateJwt(claims);
                UserLoginResult userLogin = new UserLoginResult(jwt,u);
                return Result.success(userLogin);
            }
            //登录失败
            return Result.error("Incorrect username or password!");
    }
    @GetMapping("/getinfo")
    public Result GetInfo(@RequestParam String id){
        return Result.success(userService.getInfo(id));
    }

    @PostMapping("/register")
    public Result register(@RequestBody User user){
        log.info("User register:{}",user);
        if (userService.register(user)!=null) return Result.success("Registered successfully!");
        else return Result.error("User name already exists!");
    }

    @PostMapping("/update")
    public Result update(@RequestBody User user){
            log.info("User modify information:{}",user);
            userService.update(user);
            return Result.success();
    }

    @GetMapping("/address")
    public Result getUserAddress(String id){
        log.info("Query the user address based on user ID:{}",id);
       // List<String> addressList = userService.getUserAddress(id);
        List<Address> addressList = userService.getUserAddress(id);
        return Result.success(addressList);
    }

    @PostMapping("/addAdress")
    public Result addUsrAddress(@RequestBody Address address){
        Integer id = addressService.countAddress();
        address.setAddrId(Integer.toString(id+1));
        addressService.addUsrAddress(address);
        return Result.success();
    }

    @DeleteMapping("/deleteAddress")
    public Result deleteUsrAddress(@RequestParam(required = false) String addressId){
        if(StringUtils.isEmpty(addressId)){
            addressService.deleteAllAddress();
            return Result.success("delete all addresses successfully !");
        }
        else{
            addressService.deleteAddressById(addressId);
            return Result.success("delete address successfully!");
        }
    }

}
