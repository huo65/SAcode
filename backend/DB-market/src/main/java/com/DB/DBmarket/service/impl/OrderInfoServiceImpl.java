package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.mapper.AddressMapper;
import com.DB.DBmarket.mapper.OrderInfoMapper;
import com.DB.DBmarket.mapper.OrderReviewMapper;
import com.DB.DBmarket.mapper.ProductMapper;
import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.Product;
import com.DB.DBmarket.pojo.utils.CurrentUser;
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
    @Resource
    private OrderReviewMapper orderReviewMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addOrder(OrderInfo orderInfo){
        try {
            Product product = productMapper.getOneProductById(orderInfo.getProd());
            if (product == null) {
                throw new IllegalArgumentException("Product does not exist.");
            }
            if (product.getState() == null || product.getState() != 1) {
                throw new IllegalArgumentException("Product has not been approved.");
            }
            if (orderInfo.getProdNum() == null || orderInfo.getProdNum() <= 0) {
                throw new IllegalArgumentException("Invalid product quantity.");
            }
            if (product.getNumber() == null || product.getNumber() < orderInfo.getProdNum()) {
                throw new IllegalArgumentException("Insufficient stock.");
            }
            if (orderInfo.getCus() == null || orderInfo.getRecAddr() == null) {
                throw new IllegalArgumentException("Customer and receive address are required.");
            }
            //价格全为整数...
            Integer price= product.getPrice();
            Integer account=price * orderInfo.getProdNum();
            orderInfo.setAccount(account);
            orderInfo.setTime(LocalDateTime.now());
            orderInfo.setMer(product.getMer());
            if (orderInfo.getState() == null) orderInfo.setState(-1);
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
    @Transactional(rollbackFor = Exception.class)
    public void payOrders(CurrentUser currentUser, List<String> orderIdList) {
        if (orderIdList == null || orderIdList.isEmpty()) {
            throw new IllegalArgumentException("Order list is empty.");
        }
        double total = 0;
        List<OrderInfo> rows = new ArrayList<>();
        Set<String> seenRows = new HashSet<>();
        for (String orderId : orderIdList) {
            List<OrderInfo> orderRows = orderInfoMapper.getOrdersById(orderId);
            if (orderRows == null || orderRows.isEmpty()) {
                throw new IllegalArgumentException("Order does not exist: " + orderId);
            }
            for (OrderInfo order : orderRows) {
                String rowKey = order.getId() + ":" + order.getProd();
                if (seenRows.contains(rowKey)) continue;
                seenRows.add(rowKey);
                if (!currentUser.isAdmin() && !currentUser.getId().equals(order.getCus())) {
                    throw new IllegalArgumentException("No permission to pay this order.");
                }
                if (order.getState() == null || order.getState() != -1) {
                    throw new IllegalArgumentException("Only unpaid orders can be paid.");
                }
                // Use FOR UPDATE lock to prevent concurrent stock deduction overselling
                Product product = productMapper.getOneProductByIdForUpdate(order.getProd());
                if (product == null || product.getNumber() == null || product.getNumber() < order.getProdNum()) {
                    throw new IllegalArgumentException("Insufficient stock for order " + orderId);
                }
                total += order.getAccount();
                rows.add(order);
            }
        }
        if (!currentUser.isAdmin() && userMapper.getBalance(currentUser.getId()) < total) {
            throw new IllegalArgumentException("The balance is insufficient.");
        }
        for (OrderInfo order : rows) {
            int affected = productMapper.decrementStock(order.getProd(), order.getProdNum());
            if (affected <= 0) throw new IllegalArgumentException("Insufficient stock for order " + order.getId());
        }
        if (!currentUser.isAdmin()) {
            userMapper.refundOrPay(currentUser.getId(), userMapper.getBalance(currentUser.getId()) - total);
        }
        String now = String.valueOf(LocalDateTime.now());
        for (String orderId : new HashSet<>(orderIdList)) {
            orderInfoMapper.updateOrderState(orderId, 0, now, null, now, null, null, null);
        }
    }

    @Override
    public double calculatePayableTotal(CurrentUser currentUser, List<String> orderIdList) {
        List<OrderInfo> rows = collectPayableRows(currentUser, orderIdList, false);
        return rows.stream().mapToDouble(OrderInfo::getAccount).sum();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrdersByExternal(CurrentUser currentUser, List<String> orderIdList, String payTime) {
        List<OrderInfo> rows = collectPayableRows(currentUser, orderIdList, true);
        if (rows.isEmpty()) {
            return;
        }
        for (OrderInfo order : rows) {
            int affected = productMapper.decrementStock(order.getProd(), order.getProdNum());
            if (affected <= 0) {
                throw new IllegalArgumentException("Insufficient stock for order " + order.getId());
            }
        }
        String updateTime = payTime == null || payTime.trim().isEmpty()
                ? String.valueOf(LocalDateTime.now())
                : payTime;
        for (String orderId : normalizeOrderIds(orderIdList)) {
            orderInfoMapper.updateOrderState(orderId, 0, updateTime, null, updateTime, null, null, null);
        }
    }

    @Override
    public boolean areOrdersPaid(List<String> orderIdList) {
        List<String> normalizedIds = normalizeOrderIds(orderIdList);
        if (normalizedIds.isEmpty()) {
            return false;
        }
        for (String orderId : normalizedIds) {
            List<OrderInfo> rows = orderInfoMapper.getOrdersById(orderId);
            if (rows == null || rows.isEmpty()) {
                return false;
            }
            for (OrderInfo row : rows) {
                if (row.getState() == null || row.getState() != 0) {
                    return false;
                }
            }
        }
        return true;
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
        if ("driver".equals(type)) {
            ArrayList<OrderInfo> driverOrders = orderInfoMapper.getDriverOrder(id, state, timeOrder);
            orderList.setDriverList(dealOrderInfo(driverOrders));
        } else {
            orderList.setDriverList(null);
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
    @Transactional(rollbackFor = Exception.class)
    public OrderInfo transitionOrder(CurrentUser currentUser, String orderId, int targetState, String complain, String complainReason, String refundReason) {
        List<OrderInfo> rows = orderInfoMapper.getOrdersById(orderId);
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("Order does not exist.");
        }
        OrderInfo first = rows.get(0);
        int currentState = first.getState();
        String driverId = null;
        boolean clearDriverId = false;

        if (targetState == 4) {
            if (!currentUser.isMerchant() || !currentUser.getId().equals(first.getMer()) || currentState != 0) {
                throw new IllegalArgumentException("Only the merchant can accept a paid order for preparation.");
            }
        } else if (targetState == 3) {
            if (currentUser.isMerchant()) {
                if (!currentUser.getId().equals(first.getMer()) || currentState != 4) {
                    throw new IllegalArgumentException("Only the merchant can send a preparing order to drivers.");
                }
            } else if (currentUser.isDriver()) {
                if (currentState != 1 || first.getDriverId() == null || !currentUser.getId().equals(first.getDriverId())) {
                    throw new IllegalArgumentException("Only the assigned driver can reject a delivering order.");
                }
                clearDriverId = true;
            } else {
                throw new IllegalArgumentException("Only merchant or driver can move order to waiting delivery.");
            }
        } else if (targetState == 1) {
            if (!currentUser.isDriver() || currentState != 3) {
                throw new IllegalArgumentException("Only a driver can take a waiting delivery order.");
            }
            driverId = currentUser.getId();
        } else if (targetState == 2) {
            if (!currentUser.isCustomer() || !currentUser.getId().equals(first.getCus()) || currentState != 1) {
                throw new IllegalArgumentException("Only the customer can receive a delivering order.");
            }
        } else if (targetState == -2) {
            if (!currentUser.isCustomer() || !currentUser.getId().equals(first.getCus()) || !(currentState == 1 || currentState == 2)) {
                throw new IllegalArgumentException("Only the customer can request refund after delivery starts.");
            }
        } else if (targetState == -3) {
            if (!(currentUser.isMerchant() && currentUser.getId().equals(first.getMer())) && !currentUser.isAdmin()) {
                throw new IllegalArgumentException("Only merchant or admin can confirm refund.");
            }
            if (currentState != -2 && currentState != 0 && currentState != 4) {
                throw new IllegalArgumentException("Only paid, preparing or refunding orders can be refunded.");
            }
            if ((currentState == 0 || currentState == 4) && (refundReason == null || refundReason.trim().isEmpty())) {
                throw new IllegalArgumentException("Merchant cancellation requires a refund reason.");
            }
            refundAndRestoreOrder(orderId, rows, first);
        } else if (targetState == -1 || targetState == 0) {
            throw new IllegalArgumentException("Use order creation or payment endpoint for this state.");
        } else {
            throw new IllegalArgumentException("Invalid order state.");
        }

        String now = String.valueOf(LocalDateTime.now());
        int affected = clearDriverId
                ? orderInfoMapper.updateOrderStateAndClearDriver(orderId, targetState, now, null, complain, complainReason, refundReason)
                : orderInfoMapper.updateOrderState(orderId, targetState, now, driverId, null, complain, complainReason, refundReason);
        if (affected <= 0) return null;
        return orderInfoMapper.getOrdersById(orderId).get(0);
    }

    private void refundAndRestoreOrder(String orderId, List<OrderInfo> rows, OrderInfo first) {
        if (first.getCus() != null) {
            double balance = userMapper.getBalance(first.getCus());
            userMapper.refundOrPay(first.getCus(), balance + orderInfoMapper.getOrderAccount(orderId));
        }
        // Restore stock for all items in this order
        for (OrderInfo orderItem : rows) {
            productMapper.incrementStock(orderItem.getProd(), orderItem.getProdNum());
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
                com.DB.DBmarket.pojo.OrderReview review = orderReviewMapper.getByOrderId(orderInfo.getId());
                if (review != null) {
                    review.setCustomerName(userMapper.getNameById(review.getCus()));
                }
                orderInfoReturn.setReview(review);
                orderInfoReturn.setReviewed(review != null);

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

    private List<OrderInfo> collectPayableRows(CurrentUser currentUser, List<String> orderIdList, boolean lockStock) {
        List<String> normalizedIds = normalizeOrderIds(orderIdList);
        if (normalizedIds.isEmpty()) {
            throw new IllegalArgumentException("Order list is empty.");
        }
        List<OrderInfo> rows = new ArrayList<>();
        Set<String> seenRows = new HashSet<>();
        for (String orderId : normalizedIds) {
            List<OrderInfo> orderRows = orderInfoMapper.getOrdersById(orderId);
            if (orderRows == null || orderRows.isEmpty()) {
                throw new IllegalArgumentException("Order does not exist: " + orderId);
            }
            for (OrderInfo order : orderRows) {
                String rowKey = order.getId() + ":" + order.getProd();
                if (seenRows.contains(rowKey)) {
                    continue;
                }
                seenRows.add(rowKey);
                if (!currentUser.isAdmin() && !currentUser.getId().equals(order.getCus())) {
                    throw new IllegalArgumentException("No permission to pay this order.");
                }
                if (order.getState() != null && order.getState() == 0) {
                    continue;
                }
                if (order.getState() == null || order.getState() != -1) {
                    throw new IllegalArgumentException("Only unpaid orders can be paid.");
                }
                Product product = lockStock
                        ? productMapper.getOneProductByIdForUpdate(order.getProd())
                        : productMapper.getOneProductById(order.getProd());
                if (product == null || product.getNumber() == null || product.getNumber() < order.getProdNum()) {
                    throw new IllegalArgumentException("Insufficient stock for order " + orderId);
                }
                rows.add(order);
            }
        }
        return rows;
    }

    private List<String> normalizeOrderIds(List<String> orderIdList) {
        if (orderIdList == null) {
            return Collections.emptyList();
        }
        List<String> normalizedIds = new ArrayList<>();
        Set<String> uniqueIds = new LinkedHashSet<>();
        for (String orderId : orderIdList) {
            if (orderId == null) {
                continue;
            }
            String normalized = orderId.trim();
            if (!normalized.isEmpty()) {
                uniqueIds.add(normalized);
            }
        }
        normalizedIds.addAll(uniqueIds);
        return normalizedIds;
    }
}



