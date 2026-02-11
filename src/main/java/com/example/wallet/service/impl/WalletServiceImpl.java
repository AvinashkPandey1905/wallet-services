package com.example.wallet.service.impl;

import com.example.wallet.dto.WalletOperationRequest;
import com.example.wallet.model.Wallet;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.WalletService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public void updateWallet(WalletOperationRequest request) {
        Wallet wallet = walletRepository.findWalletById(request.getValletId())
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

        if (request.getOperationType() == com.example.wallet.dto.OperationType.DEPOSIT) {
            wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        } else {
            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new IllegalArgumentException("Insufficient funds");
            }
            wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        }

        walletRepository.save(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getWalletBalance(UUID walletId) {
        return walletRepository.findById(walletId)
                .map(Wallet::getBalance)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));
    }
}
