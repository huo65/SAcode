package com.DB.DBmarket.service.impl;

import cn.hutool.json.JSONObject;
import com.DB.DBmarket.config.AliPayConfig;
import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.utils.AliPay;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.RandomIdGenerator;
import com.DB.DBmarket.service.AliPayService;
import com.DB.DBmarket.service.OrderInfoService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AliPayServiceImpl implements AliPayService {
    private static final String FORMAT = "JSON";
    private static final String CHARSET = "UTF-8";
    private static final String SIGN_TYPE = "RSA2";
    private static final CurrentUser SYSTEM_USER = new CurrentUser("system", "system", "admin");

    @Resource
    private AliPayConfig aliPayConfig;

    @Resource
    private OrderInfoService orderInfoService;

    @Override
    public String createPayForm(CurrentUser currentUser, List<String> orderIdList) throws IOException {
        validateConfig();
        List<String> normalizedOrderIds = normalizeOrderIds(orderIdList);
        double totalAmount = orderInfoService.calculatePayableTotal(currentUser, normalizedOrderIds);
        if (totalAmount <= 0) {
            throw new IllegalArgumentException("No payable orders found.");
        }

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        if (StringUtils.hasText(aliPayConfig.getNotifyUrl()) && !aliPayConfig.getNotifyUrl().contains("your-public-domain")) {
            request.setNotifyUrl(aliPayConfig.getNotifyUrl());
        }
        if (StringUtils.hasText(aliPayConfig.getReturnUrl())) {
            request.setReturnUrl(aliPayConfig.getReturnUrl());
        }
        JSONObject bizContent = new JSONObject();
        bizContent.set("out_trade_no", buildTraceNo());
        bizContent.set("total_amount", String.format(Locale.US, "%.2f", totalAmount));
        bizContent.set("subject", buildSubject(normalizedOrderIds));
        bizContent.set("product_code", "FAST_INSTANT_TRADE_PAY");
        bizContent.set("passback_params", URLEncoder.encode(String.join(",", normalizedOrderIds), CHARSET));
        request.setBizContent(bizContent.toString());

        try {
            return createClient().pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            throw new IOException("Create Alipay payment form failed.", e);
        }
    }

    @Override
    public Result payNotify(HttpServletRequest request) throws AlipayApiException {
        Map<String, String> params = collectParams(request);
        if (!verifySignature(params)) {
            return Result.error("Invalid Alipay signature.");
        }
        String tradeStatus = params.get("trade_status");
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            List<String> orderIds = parseOrderIds(params.get("passback_params"));
            orderInfoService.payOrdersByExternal(SYSTEM_USER, orderIds, params.get("gmt_payment"));
            return Result.success();
        }
        return Result.error("pay or other error!");
    }

    @Override
    public String payReturn(HttpServletRequest request) throws Exception {
        Map<String, String> params = collectParams(request);
        if (!verifySignature(params)) {
            return buildReturnPage(false, "Payment verification failed.");
        }
        String tradeStatus = params.get("trade_status");
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            List<String> orderIds = parseOrderIds(params.get("passback_params"));
            orderInfoService.payOrdersByExternal(SYSTEM_USER, orderIds, params.get("gmt_payment"));
            return buildReturnPage(true, "Payment completed successfully.");
        }
        return buildReturnPage(false, "Payment was not completed.");
    }

    @Override
    public Result returnPay(AliPay aliPay, String refundReason) throws AlipayApiException {
        // 7天无理由退款
//        String now = DateUtil.now();
//        Orders orders = ordersMapper.getByNo(aliPay.getTraceNo());
//        if (orders != null) {
//            // hutool工具类，判断时间间隔
//            long between = DateUtil.between(DateUtil.parseDateTime(orders.getPaymentTime()), DateUtil.parseDateTime(now), DateUnit.DAY);
//            if (between > 7) {
//                return Result.error("-1", "该订单已超过7天，不支持退款");
//            }
//        }

        validateConfig();
        AlipayClient alipayClient = createClient();
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.set("refund_amount", aliPay.getTotalAmount());
        bizContent.set("out_trade_no", aliPay.getTraceNo());
        request.setBizContent(bizContent.toString());

        AlipayTradeRefundResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            System.out.println("调用成功");

            String payTime = String.valueOf(LocalDateTime.now());
            orderInfoService.updateOrderState1(aliPay.getTraceNo(),-3, payTime, null, "", refundReason);

            return Result.success();
        } else {
            System.out.println(response.getBody());
            return Result.error(response.getBody());
        }
    }

    private AlipayClient createClient() {
        return new DefaultAlipayClient(
                aliPayConfig.getGatewayUrl(),
                aliPayConfig.getAppId(),
                aliPayConfig.getAppPrivateKey(),
                FORMAT,
                CHARSET,
                aliPayConfig.getAlipayPublicKey(),
                SIGN_TYPE
        );
    }

    private void validateConfig() {
        if (!aliPayConfig.isConfigured()) {
            throw new IllegalStateException("AliPay sandbox config is incomplete. Please configure appId, appPrivateKey and alipayPublicKey.");
        }
        if (!StringUtils.hasText(aliPayConfig.getReturnUrl())) {
            throw new IllegalStateException("AliPay returnUrl is required.");
        }
    }

    private String buildTraceNo() {
        return "ALI" + RandomIdGenerator.getRandomId() + System.currentTimeMillis();
    }

    private String buildSubject(List<String> orderIds) {
        return "DB Market Orders(" + orderIds.size() + ")";
    }

    private Map<String, String> collectParams(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> request.getParameter(entry.getKey())
                ));
    }

    private boolean verifySignature(Map<String, String> params) throws AlipayApiException {
        return com.alipay.api.internal.util.AlipaySignature.rsaCheckV1(
                params,
                aliPayConfig.getAlipayPublicKey(),
                CHARSET,
                SIGN_TYPE
        );
    }

    private List<String> parseOrderIds(String passbackParams) {
        List<String> orderIds = new ArrayList<>();
        if (!StringUtils.hasText(passbackParams)) {
            return orderIds;
        }
        String decoded;
        try {
            decoded = URLDecoder.decode(passbackParams, CHARSET);
        } catch (Exception e) {
            throw new IllegalArgumentException("Decode passback_params failed.", e);
        }
        for (String orderId : decoded.split(",")) {
            String normalized = orderId == null ? "" : orderId.trim();
            if (!normalized.isEmpty()) {
                orderIds.add(normalized);
            }
        }
        return orderIds;
    }

    private List<String> normalizeOrderIds(List<String> orderIdList) {
        LinkedHashSet<String> uniqueIds = new LinkedHashSet<>();
        if (orderIdList != null) {
            for (String orderId : orderIdList) {
                if (orderId != null && !orderId.trim().isEmpty()) {
                    uniqueIds.add(orderId.trim());
                }
            }
        }
        return new ArrayList<>(uniqueIds);
    }

    private String buildReturnPage(boolean success, String message) {
        String title = success ? "Payment Success" : "Payment Failed";
        String color = success ? "#67c23a" : "#f56c6c";
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>" + title + "</title></head>"
                + "<body style=\"font-family:Arial,sans-serif;display:flex;align-items:center;justify-content:center;height:100vh;margin:0;background:#f5f7fa;\">"
                + "<div style=\"padding:32px 40px;background:#fff;border-radius:12px;box-shadow:0 8px 24px rgba(0,0,0,0.08);text-align:center;\">"
                + "<h2 style=\"margin:0 0 12px;color:" + color + ";\">" + title + "</h2>"
                + "<p style=\"margin:0 0 20px;color:#606266;\">" + message + "</p>"
                + "<p style=\"margin:0;color:#909399;font-size:12px;\">This window will close automatically.</p>"
                + "</div><script>setTimeout(function(){window.close();},1500);</script></body></html>";
    }
}
