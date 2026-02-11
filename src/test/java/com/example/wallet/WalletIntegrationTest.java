package com.example.wallet;

import com.example.wallet.dto.OperationType;
import com.example.wallet.dto.WalletOperationRequest;
import com.example.wallet.model.Wallet;
import com.example.wallet.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class WalletIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
    }

    @Test
    void testDepositAndWithdraw() throws Exception {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, BigDecimal.ZERO);
        walletRepository.save(wallet);

        // Deposit 1000
        WalletOperationRequest depositRequest = new WalletOperationRequest();
        depositRequest.setValletId(walletId);
        depositRequest.setOperationType(OperationType.DEPOSIT);
        depositRequest.setAmount(new BigDecimal("1000"));

        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk());

        // Withdraw 500
        WalletOperationRequest withdrawRequest = new WalletOperationRequest();
        withdrawRequest.setValletId(walletId);
        withdrawRequest.setOperationType(OperationType.WITHDRAW);
        withdrawRequest.setAmount(new BigDecimal("500"));

        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk());

        // Check Balance
        mockMvc.perform(get("/api/v1/wallets/" + walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(500.0));
    }

    @Test
    void testConcurrency() throws Exception {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, BigDecimal.ZERO);
        walletRepository.save(wallet);

        int threads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> {
                try {
                    WalletOperationRequest request = new WalletOperationRequest();
                    request.setValletId(walletId);
                    request.setOperationType(OperationType.DEPOSIT);
                    request.setAmount(BigDecimal.ONE);

                    mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isOk());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        assertEquals(threads, successCount.get());

        Wallet updatedWallet = walletRepository.findById(walletId).orElseThrow();
        // threads * 1 = threads
        assertEquals(new BigDecimal(threads).setScale(2), updatedWallet.getBalance());
    }
}
