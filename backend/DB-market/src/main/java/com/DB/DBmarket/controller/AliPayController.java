package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.utils.AliPay;
import com.DB.DBmarket.pojo.utils.CurrentUserHolder;
import com.DB.DBmarket.service.AliPayService;
import com.DB.DBmarket.service.OrderInfoService;
import com.alipay.api.AlipayApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@Slf4j
@RequestMapping("/alipay")
public class AliPayController {
    @Resource
    private AliPayService aliPayService;
    @Resource
    private OrderInfoService orderInfoService;

    @GetMapping("/pay")
    public void pay(@RequestParam String orderIds, HttpServletResponse httpResponse) throws IOException {
        log.info("pay by Alipay");
        List<String> orderIdList = parseOrderIds(orderIds);
        String form = aliPayService.createPayForm(CurrentUserHolder.require(), orderIdList);
        httpResponse.setContentType("text/html;charset=UTF-8");
        httpResponse.getWriter().write(form);
        httpResponse.getWriter().flush();
    }

    //支付完成以后支付宝沙箱回调接口
    @PostMapping("/notify")  // 注意这里必须是POST接口
    public Result payNotify(HttpServletRequest request) throws Exception {
        log.info("notify");
        return aliPayService.payNotify(request);
    }

    @GetMapping(value = "/return", produces = "text/html;charset=UTF-8")
    public String payReturn(HttpServletRequest request) throws Exception {
        log.info("return");
        return aliPayService.payReturn(request);
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
    public Result checkPaymentStatus(@RequestParam String orderIds) {
        log.info("check pay state");
        Map<String, Object> data = new HashMap<>();
        List<String> orderIdList = parseOrderIds(orderIds);
        data.put("paid", orderInfoService.areOrdersPaid(orderIdList));
        data.put("orderIds", orderIdList);
        return Result.success(data);
    }

    private List<String> parseOrderIds(String orderIds) {
        return Arrays.stream(orderIds.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}

