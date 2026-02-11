package com.example.wallet;

import com.example.wallet.dto.OperationType;
import com.example.wallet.dto.WalletOperationRequest;
import com.example.wallet.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WalletErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void testWalletNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testInvalidJson() throws Exception {
        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"valletId\": \"invalid-uuid\", \"amount\": 100 }")) // Invalid UUID format or just
                                                                                 // malformed
                .andExpect(status().isBadRequest());
        // The message might vary depending on deserialization error, keeping it simple
        // check for 400
    }

    @Test
    void testInsufficientFunds() throws Exception {
        UUID walletId = UUID.randomUUID();
        // Wallet doesn't exist, will hit 404 first usually if not found,
        // but let's assume updateWallet throws NotFound.
        // Wait, updateWallet checks existence.

        // Let's create a wallet first
        com.example.wallet.model.Wallet wallet = new com.example.wallet.model.Wallet(walletId, BigDecimal.ZERO);
        walletRepository.save(wallet);

        WalletOperationRequest request = new WalletOperationRequest(walletId, OperationType.WITHDRAW,
                new BigDecimal("100"));

        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient funds"));
    }
}
