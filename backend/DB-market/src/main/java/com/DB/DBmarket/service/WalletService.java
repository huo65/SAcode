package com.DB.DBmarket.service;

import com.DB.DBmarket.pojo.wallet.WalletTransaction;
import com.DB.DBmarket.pojo.utils.CurrentUser;

import java.util.List;
import java.util.Map;

public interface WalletService {
    WalletTransaction recharge(CurrentUser currentUser, String targetUserId, Integer amount, String remark);

    WalletTransaction payOrder(CurrentUser currentUser, Integer amount, String orderId);

    /** Credits the merchant who fulfilled a paid order. */
    WalletTransaction creditMerchantOrder(CurrentUser currentUser, String merchantId, Integer amount, String orderId);

    /** Reverses a merchant order credit before refunding the customer. */
    WalletTransaction reverseMerchantOrderIncome(CurrentUser currentUser, String merchantId, Integer amount, String orderId, String remark);

    WalletTransaction refundOrder(CurrentUser currentUser, String userId, Integer amount, String orderId, String remark);

    Map<String, Object> getWallet(CurrentUser currentUser, String userId);

    List<WalletTransaction> listTransactions(CurrentUser currentUser, String userId, String type, Integer limit);
}
