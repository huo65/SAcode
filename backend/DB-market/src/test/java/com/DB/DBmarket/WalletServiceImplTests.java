package com.DB.DBmarket;

import com.DB.DBmarket.mapper.UserMapper;
import com.DB.DBmarket.mapper.WalletTransactionMapper;
import com.DB.DBmarket.pojo.User;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.wallet.WalletTransaction;
import com.DB.DBmarket.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTests {
    @Mock
    private WalletTransactionMapper walletTransactionMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    void customerRechargeIncreasesOwnBalanceAndRecordsTransaction() {
        CurrentUser customer = new CurrentUser("2", "customer", "cus");
        when(userMapper.getInfoForUpdate("2")).thenReturn(buildUser("2", "customer", 20));
        when(userMapper.updateBalance("2", 70)).thenReturn(1);

        WalletTransaction transaction = walletService.recharge(customer, null, 50, "demo recharge");

        assertEquals("RECHARGE", transaction.getType());
        assertEquals(Integer.valueOf(50), transaction.getAmount());
        assertEquals(Integer.valueOf(20), transaction.getBalanceBefore());
        assertEquals(Integer.valueOf(70), transaction.getBalanceAfter());
        verify(walletTransactionMapper).insert(any(WalletTransaction.class));
    }

    @Test
    void customerCannotRechargeOtherWallet() {
        CurrentUser customer = new CurrentUser("2", "customer", "cus");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> walletService.recharge(customer, "3", 50, null));

        assertEquals("No permission to recharge this wallet.", ex.getMessage());
    }

    @Test
    void payOrderRejectsInsufficientBalance() {
        CurrentUser customer = new CurrentUser("2", "customer", "cus");
        when(userMapper.getInfoForUpdate("2")).thenReturn(buildUser("2", "customer", 30));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> walletService.payOrder(customer, 40, "order-1"));

        assertEquals("余额不足", ex.getMessage());
    }

    @Test
    void payOrderRecordsNegativeAmount() {
        CurrentUser customer = new CurrentUser("2", "customer", "cus");
        when(userMapper.getInfoForUpdate("2")).thenReturn(buildUser("2", "customer", 100));
        when(userMapper.updateBalance("2", 60)).thenReturn(1);

        walletService.payOrder(customer, 40, "order-1");

        ArgumentCaptor<WalletTransaction> captor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionMapper).insert(captor.capture());
        assertEquals("PAY", captor.getValue().getType());
        assertEquals(Integer.valueOf(-40), captor.getValue().getAmount());
        assertEquals("order-1", captor.getValue().getRelatedOrderId());
    }

    @Test
    void nonAdminListsOnlyOwnTransactions() {
        CurrentUser customer = new CurrentUser("2", "customer", "cus");
        when(walletTransactionMapper.list("2", null, 100)).thenReturn(Collections.emptyList());

        List<WalletTransaction> transactions = walletService.listTransactions(customer, "3", null, null);

        assertTrue(transactions.isEmpty());
        verify(walletTransactionMapper).list("2", null, 100);
    }

    @Test
    void walletTypeAllMeansNoTypeFilter() {
        CurrentUser admin = new CurrentUser("1", "admin", "admin");
        when(walletTransactionMapper.list(null, null, 100)).thenReturn(Collections.emptyList());

        List<WalletTransaction> transactions = walletService.listTransactions(admin, null, "all", null);

        assertTrue(transactions.isEmpty());
        verify(walletTransactionMapper).list(null, null, 100);
    }

    private User buildUser(String id, String name, Integer balance) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setBalance(balance);
        return user;
    }
}
