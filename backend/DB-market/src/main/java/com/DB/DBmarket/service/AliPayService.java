package com.DB.DBmarket.service;


import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.utils.AliPay;
import com.alipay.api.AlipayApiException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AliPayService {
    void pay(AliPay aliPay, HttpServletResponse httpResponse) throws IOException;

    Result payNotify(HttpServletRequest request) throws AlipayApiException;

    Result returnPay(AliPay aliPay, String refundReason) throws AlipayApiException;

}
