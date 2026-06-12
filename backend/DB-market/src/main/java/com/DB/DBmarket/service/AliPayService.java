package com.DB.DBmarket.service;


import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.AliPay;
import com.alipay.api.AlipayApiException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public interface AliPayService {
    String createPayForm(CurrentUser currentUser, List<String> orderIdList) throws IOException;

    Result payNotify(HttpServletRequest request) throws AlipayApiException;

    String payReturn(HttpServletRequest request) throws Exception;

    Result returnPay(AliPay aliPay, String refundReason) throws AlipayApiException;

}
