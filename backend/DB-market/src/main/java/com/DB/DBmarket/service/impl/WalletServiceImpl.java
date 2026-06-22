package com.DB.DBmarket.service.impl;

import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.mapper.WalletTransactionMapper;
import com.DB.DBmarket.pojo.User;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.RandomIdGenerator;
import com.DB.DBmarket.pojo.wallet.WalletTransaction;
import com.DB.DBmarket.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("WalletService")
public class WalletServiceImpl implements WalletService {
    public static final String TYPE_RECHARGE = "RECHARGE";
    public static final String TYPE_PAY = "PAY";
    public static final String TYPE_REFUND = "REFUND";
    public static final String TYPE_MERCHANT_INCOME = "MERCHANT_INCOME";
    public static final String TYPE_MERCHANT_REFUND = "MERCHANT_REFUND";

    @Resource
    private WalletTransactionMapper walletTransactionMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletTransaction recharge(CurrentUser currentUser, String targetUserId, Integer amount, String remark) {
        if (currentUser == null) {
            throw new IllegalArgumentException("Please login first.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }
        if (!currentUser.isAdmin() && !currentUser.isCustomer()) {
            throw new IllegalArgumentException("Only customers can recharge wallet balance.");
        }
        if (!currentUser.isAdmin() && hasText(targetUserId) && !currentUser.getId().equals(targetUserId.trim())) {
            throw new IllegalArgumentException("No permission to recharge this wallet.");
        }
        String userId = currentUser.isAdmin() && hasText(targetUserId) ? targetUserId.trim() : currentUser.getId();
        return changeBalance(currentUser, userId, TYPE_RECHARGE, amount, null,
                hasText(remark) ? remark.trim() : "模拟充值入账");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletTransaction payOrder(CurrentUser currentUser, Integer amount, String orderId) {
        if (currentUser == null) {
            throw new IllegalArgumentException("Please login first.");
        }
        if (!currentUser.isCustomer()) {
            throw new IllegalArgumentException("Only customers can pay with wallet balance.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }
        return changeBalance(currentUser, currentUser.getId(), TYPE_PAY, -amount, orderId, "订单余额支付");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletTransaction creditMerchantOrder(CurrentUser currentUser, String merchantId, Integer amount, String orderId) {
        if (!hasText(merchantId)) {
            throw new IllegalArgumentException("Merchant is required.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Merchant income amount must be greater than zero.");
        }
        User merchant = requireUser(merchantId.trim());
        if (!"mer".equals(merchant.getType()) && !"merchant".equals(merchant.getType())) {
            throw new IllegalArgumentException("Order merchant is invalid.");
        }
        return changeBalance(currentUser, merchant.getId(), TYPE_MERCHANT_INCOME, amount, orderId, "订单收入入账");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletTransaction reverseMerchantOrderIncome(CurrentUser currentUser, String merchantId, Integer amount, String orderId, String remark) {
        if (!hasText(merchantId)) {
            throw new IllegalArgumentException("Merchant is required.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Merchant refund amount must be greater than zero.");
        }
        return changeBalance(currentUser, merchantId.trim(), TYPE_MERCHANT_REFUND, -amount, orderId,
                hasText(remark) ? remark.trim() : "订单退款扣回收入");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletTransaction refundOrder(CurrentUser currentUser, String userId, Integer amount, String orderId, String remark) {
        if (currentUser == null) {
            throw new IllegalArgumentException("Please login first.");
        }
        if (!currentUser.isAdmin() && !currentUser.isMerchant()) {
            throw new IllegalArgumentException("Only merchant or admin can refund orders.");
        }
        if (!hasText(userId)) {
            throw new IllegalArgumentException("Refund user is required.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }
        return changeBalance(currentUser, userId.trim(), TYPE_REFUND, amount, orderId,
                hasText(remark) ? remark.trim() : "订单退款回滚");
    }

    @Override
    public Map<String, Object> getWallet(CurrentUser currentUser, String userId) {
        String targetUserId = resolveReadableUserId(currentUser, userId);
        ensureInitialized();
        User user = requireUser(targetUserId);
        Map<String, Object> wallet = new LinkedHashMap<>();
        wallet.put("userId", user.getId());
        wallet.put("userName", user.getName());
        wallet.put("balance", user.getBalance());
        wallet.put("totalRecharge", safeAbsSum(targetUserId, TYPE_RECHARGE));
        wallet.put("totalPay", safeAbsSum(targetUserId, TYPE_PAY));
        wallet.put("totalRefund", safeAbsSum(targetUserId, TYPE_REFUND));
        wallet.put("totalMerchantIncome", safeAbsSum(targetUserId, TYPE_MERCHANT_INCOME));
        wallet.put("totalMerchantRefund", safeAbsSum(targetUserId, TYPE_MERCHANT_REFUND));
        wallet.put("recentTransactions", walletTransactionMapper.list(targetUserId, null, 10));
        return wallet;
    }

    @Override
    public List<WalletTransaction> listTransactions(CurrentUser currentUser, String userId, String type, Integer limit) {
        String targetUserId = null;
        if (currentUser == null) {
            throw new IllegalArgumentException("Please login first.");
        }
        if (currentUser.isAdmin()) {
            targetUserId = hasText(userId) ? userId.trim() : null;
        } else {
            targetUserId = currentUser.getId();
        }
        ensureInitialized();
        int safeLimit = limit == null || limit <= 0 ? 100 : Math.min(limit, 200);
        return walletTransactionMapper.list(targetUserId, normalizeType(type), safeLimit);
    }

    private WalletTransaction changeBalance(CurrentUser actor, String userId, String type, Integer delta, String orderId, String remark) {
        ensureInitialized();
        User user = requireUserForUpdate(userId);
        int before = user.getBalance() == null ? 0 : user.getBalance();
        int after = before + delta;
        if (after < 0) {
            throw new IllegalArgumentException("余额不足");
        }
        int updated = userMapper.updateBalance(userId, after);
        if (updated <= 0) {
            throw new IllegalArgumentException("Update wallet balance failed.");
        }
        WalletTransaction transaction = new WalletTransaction(
                RandomIdGenerator.getRandomId(),
                user.getId(),
                user.getName(),
                type,
                delta,
                before,
                after,
                orderId,
                remark,
                actor == null ? null : actor.getId(),
                actor == null ? null : actor.getName(),
                actor == null ? null : actor.getType(),
                LocalDateTime.now()
        );
        walletTransactionMapper.insert(transaction);
        return transaction;
    }

    private String resolveReadableUserId(CurrentUser currentUser, String userId) {
        if (currentUser == null) {
            throw new IllegalArgumentException("Please login first.");
        }
        if (currentUser.isAdmin() && hasText(userId)) {
            return userId.trim();
        }
        return currentUser.getId();
    }

    private User requireUser(String userId) {
        User user = userMapper.getInfo(userId);
        if (user == null) {
            throw new IllegalArgumentException("User does not exist.");
        }
        return user;
    }

    private User requireUserForUpdate(String userId) {
        User user = userMapper.getInfoForUpdate(userId);
        if (user == null) {
            throw new IllegalArgumentException("User does not exist.");
        }
        return user;
    }

    private void ensureInitialized() {
        walletTransactionMapper.createTableIfMissing();
    }

    private Integer safeAbsSum(String userId, String type) {
        Integer sum = walletTransactionMapper.sumByUserAndType(userId, type);
        return sum == null ? 0 : Math.abs(sum);
    }

    private String normalizeType(String type) {
        if (!hasText(type)) {
            return null;
        }
        String normalized = type.trim().toUpperCase();
        if ("ALL".equals(normalized)) {
            return null;
        }
        if (!TYPE_RECHARGE.equals(normalized) && !TYPE_PAY.equals(normalized) && !TYPE_REFUND.equals(normalized)
                && !TYPE_MERCHANT_INCOME.equals(normalized) && !TYPE_MERCHANT_REFUND.equals(normalized)
                && !"ADJUST".equals(normalized)) {
            throw new IllegalArgumentException("Invalid wallet transaction type.");
        }
        return normalized;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
