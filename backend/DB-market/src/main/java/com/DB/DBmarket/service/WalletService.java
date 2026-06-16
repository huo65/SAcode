package com.DB.DBmarket.service;

import com.DB.DBmarket.pojo.wallet.WalletTransaction;
import com.DB.DBmarket.pojo.utils.CurrentUser;

import java.util.List;
import java.util.Map;

public interface WalletService {
    WalletTransaction recharge(CurrentUser currentUser, String targetUserId, Integer amount, String remark);

    WalletTransaction payOrder(CurrentUser currentUser, Integer amount, String orderId);

    WalletTransaction refundOrder(CurrentUser currentUser, String userId, Integer amount, String orderId, String remark);

    Map<String, Object> getWallet(CurrentUser currentUser, String userId);

    List<WalletTransaction> listTransactions(CurrentUser currentUser, String userId, String type, Integer limit);
}
