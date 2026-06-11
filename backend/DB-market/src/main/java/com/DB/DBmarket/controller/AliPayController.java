package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.OrderInfo;
import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.utils.AliPay;
import com.DB.DBmarket.service.AliPayService;
import com.DB.DBmarket.service.OrderInfoService;
import com.alipay.api.AlipayApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.sql.In;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


@RestController
@Slf4j
@RequestMapping("/alipay")
public class AliPayController {
    @Resource
    private AliPayService aliPayService;
    @Resource
    private OrderInfoService orderInfoService;

    @GetMapping("/pay") // &subject=xxx&traceNo=xxx&totalAmount=xxx
    public Result pay(AliPay aliPay, HttpServletResponse httpResponse) throws IOException {
        log.info("pay by Alipay");
        // 检查订单是否存在(支付宝会自动判重)
//        if (orderInfoService.getOrderByOrderId(aliPay.getTraceNo()) == null) {
//            return Result.error("order doesn't exist!");
//        }
        aliPayService.pay(aliPay, httpResponse);
        return  Result.success();
    }


    //支付完成以后支付宝沙箱回调接口
    @PostMapping("/notify")  // 注意这里必须是POST接口
    public Result payNotify(HttpServletRequest request) throws Exception {
        log.info("notify");
        return aliPayService.payNotify(request);
    }

    @GetMapping("/refund")
    public Result returnPay(@RequestParam String tradeNo, @RequestParam  Integer totalAmount,@RequestParam String refundReason) throws AlipayApiException {
        log.info("refund");
        AliPay aliPay = new AliPay();
        aliPay.setTraceNo(tradeNo);
        aliPay.setTotalAmount(totalAmount);
        // 只有支付过的订单才能退款
        return aliPayService.returnPay(aliPay, refundReason);
    }

    @GetMapping("/check")
    public Result checkPaymentStatus(@RequestParam String id) {
        log.info("check pay state");
        List<OrderInfo> orderList = orderInfoService.getOrderByOrderId(id);
        if (orderList == null) return Result.error("order doesn't exist!");
        if (orderList.get(0).getState()==0) return Result.success();
        else return Result.error("pay failure!");
    }
}

