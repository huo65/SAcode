package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.mapper.AddressMapper;
import com.DB.DBmarket.mapper.OrderInfoMapper;
import com.DB.DBmarket.mapper.ProductMapper;
import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.Product;
import com.DB.DBmarket.pojo.utils.OrderInfoReturn;
import com.DB.DBmarket.pojo.utils.OrderList;
import com.DB.DBmarket.pojo.utils.ProductReturn;
import com.DB.DBmarket.pojo.utils.RandomIdGenerator;
import com.DB.DBmarket.service.OrderInfoService;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderInfoServiceImpl implements OrderInfoService {
    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private AddressMapper addressMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addOrder(OrderInfo orderInfo){
        try {
            //价格全为整数...
            Integer price= productMapper.getPrice(orderInfo.getProd());
            Integer account=price * orderInfo.getProdNum();
            orderInfo.setAccount(account);
            orderInfo.setTime(LocalDateTime.now());
            //orderInfo.setMer(userMapper.getIdByName(orderInfo.getMer()));
            List<String> merchantAddress = addressMapper.getAddress(orderInfo.getMer());
            Random random = new Random();

            orderInfo.setDeliAddr(merchantAddress.get(random.nextInt(merchantAddress.size())));

            if(orderInfo.getId() != null){
                //如果下单过该商家的商品,则使用上一次的订单ID
                orderInfo.setId(orderInfo.getId());
            }else{
                //否则生成新的ID
                String orderId = RandomIdGenerator.getRandomId();
                orderInfo.setId(orderId);
            }
            orderInfoMapper.addOrder(orderInfo);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public Integer pay(String id, ArrayList<Integer> prince_list, String order_id){
        double sum=0;
        for(Integer i:prince_list) sum+=i;//总价格大于0代表付款，否则退款
        List<Integer> stateList=orderInfoMapper.getOrderState(order_id);
        Integer state = stateList.get(0);
        if(sum>0&&state == -1)
            orderInfoMapper.payForOrder(order_id);
        if(sum>0&&state!=-1)
            return -1;//Result.error("You've already paid. You don't have to pay again.");
        if(sum>userMapper.getBalance(id)&&state==-1)
            return -2;//Result.error("The balance is insufficient.");
        double b=userMapper.getBalance(id)-sum;
        userMapper.refundOrPay(id,b);

        return 1;//Result.success();
    }

    @Override
    public OrderList getOrderInfo(String id, Integer state, Integer timeOrder) {
        OrderList orderList = new OrderList();

        // 获取客户订单信息
        ArrayList<OrderInfo> cusOrderList = orderInfoMapper.getCusOrder(id, state, timeOrder);
        ArrayList<OrderInfoReturn> cusOrderInfoList = dealOrderInfo(cusOrderList);

        // 获取商家订单信息
        ArrayList<OrderInfo> merOrderList = orderInfoMapper.getMerOrder(id, state, timeOrder);
        ArrayList<OrderInfoReturn> merOrderInfoList = dealOrderInfo(merOrderList);

        // 合并客户和商家订单信息
        // 设置订单列表
        orderList.setCusList(cusOrderInfoList);
        orderList.setMerList(merOrderInfoList);

        // 设置全部订单列表（只有管理员才能看到）
        String type = userMapper.getUserType(id);
        if ("admin".equals(type)) {
            ArrayList<OrderInfo> allOrders = orderInfoMapper.getAllOrders_518(id, state, timeOrder);
            ArrayList<OrderInfoReturn> allOrderInfoList =dealOrderInfo(allOrders);
            orderList.setAllOrderList(allOrderInfoList);
        }else {
                orderList.setAllOrderList(null);
        }

        return orderList;
    }

    @Override
    public List<Product> getProductList(String productId) {
        return productMapper.getProductById(productId);
    }

    @Override
    public List<Integer> getProductNumberList(String productNumbers) {
        // Assuming productNumbers is a comma-separated string of product numbers
        List<Integer> productNumberList = new ArrayList<>();
        String[] numbers = productNumbers.split(",");
        for (String number : numbers) {
            productNumberList.add(Integer.parseInt(number));
        }
        return productNumberList;
    }

    @Override
    public OrderInfo updateOrderState1(String orderId, int newState, String updateTime, String complain, String complainReason, String refundReason) {
        int rowsAffected = orderInfoMapper.updateOrderState1(orderId, newState, updateTime, complain, complainReason, refundReason);
        if (rowsAffected > 0) {
            return orderInfoMapper.getOrdersById(orderId).get(0);
        } else {
            return null;
        }
    }

    @Override
    public OrderInfo updateOrderState2(String orderId, int newState, String updateTime) {
        int rowsAffected = orderInfoMapper.updateOrderState2(orderId, newState, updateTime);
        if (rowsAffected > 0) {
              return orderInfoMapper.getOrdersById(orderId).get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<OrderInfo> getAllOrders() {
        return orderInfoMapper.getAllOrders();
    }

    @Override
    public List<OrderInfo> getOrdersByState(int state) {
        return orderInfoMapper.getOrdersByState(state);
    }

    @Override
    public List<OrderInfo> getOrderByOrderId(String orderId) {
        return orderInfoMapper.getOrdersById(orderId);
    }

    private ArrayList<OrderInfoReturn> dealOrderInfo(ArrayList<OrderInfo> list){
        //处理订单信息的地址，从id->字符串
        ArrayList<OrderInfoReturn> returnList = new ArrayList<>();
        Map<String,String> orderIdMap = new HashMap<>();
        for (OrderInfo orderInfo : list) {
            //获取商家ID
            String merchantId = orderInfo.getMer();
            String nowId = orderInfo.getId();
            if(orderIdMap.containsKey(nowId)){
                //如果存在该订单号
                OrderInfoReturn needAddOrderInfoReturn = null;
                for(OrderInfoReturn orderInfoReturn1:returnList){
                    if(orderInfoReturn1.getOrderInfo().getId().equals(nowId)){
                        //订单ID需要相同
                        needAddOrderInfoReturn = orderInfoReturn1;
                        System.out.println("订单ID："+nowId);
                        System.out.println("新订单ID："+orderInfoReturn1.getOrderInfo().getId());
                        //删除该订单
                        returnList.remove(orderInfoReturn1);
                        break;
                    }
                }
                List<ProductReturn> productList=needAddOrderInfoReturn.getProductList();
                List<String> mutiImgList = needAddOrderInfoReturn.getImgList();
                ProductReturn temp = productMapper.getOneProductReturnById(orderInfo.getProd());
                temp.setProdNum(orderInfo.getProdNum());
                productList.add(temp);

                mutiImgList.add(productMapper.getFirstImg(orderInfo.getProd()).get(0));
                //重新填回该订单
                needAddOrderInfoReturn.setProductList(productList);
                needAddOrderInfoReturn.setImgList(mutiImgList);
                needAddOrderInfoReturn.getOrderInfo().setAccount(caculateAccount(needAddOrderInfoReturn));
                returnList.add(needAddOrderInfoReturn);
            }else{
                OrderInfoReturn orderInfoReturn = new OrderInfoReturn();
                orderInfoReturn.setOrderInfo(orderInfo);
                orderInfoReturn.setDelivery(addressMapper.getAddressByAddressId(orderInfo.getDeliAddr()));
                orderInfoReturn.setReceive(addressMapper.getAddressByAddressId(orderInfo.getRecAddr()));

                ProductReturn temp = productMapper.getOneProductReturnById(orderInfo.getProd());
                temp.setProdNum(orderInfo.getProdNum());
                List<ProductReturn> productList = new ArrayList<>();
                productList.add(temp);
                orderInfoReturn.setProductList(productList);
                List<String> imgList =new ArrayList<>();
                imgList.add(productMapper.getFirstImg(orderInfo.getProd()).get(0));
                orderInfoReturn.setImgList(imgList);
                orderInfoReturn.setCusName(userMapper.getNameById(orderInfo.getCus()));
                orderInfoReturn.setMerName(userMapper.getNameById(orderInfo.getMer()));

                orderIdMap.put(orderInfo.getId(),merchantId);
                returnList.add(orderInfoReturn);
            }
        }
        return returnList;
    }

    private Integer caculateAccount(OrderInfoReturn orderInfoReturn){
        List<ProductReturn> productList = orderInfoReturn.getProductList();
        Integer account = 0;
        for(ProductReturn productReturn:productList){
            account += productReturn.getProdNum()*productReturn.getPrice();
        }
        return account;

    }
}



