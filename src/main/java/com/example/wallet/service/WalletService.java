package com.example.wallet.service;

import com.example.wallet.dto.WalletOperationRequest;
import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {
    void updateWallet(WalletOperationRequest request);

    BigDecimal getWalletBalance(UUID walletId);
}
