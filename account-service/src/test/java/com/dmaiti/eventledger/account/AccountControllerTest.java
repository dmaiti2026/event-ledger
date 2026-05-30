package com.dmaiti.eventledger.account;

import com.dmaiti.eventledger.account.dto.AccountResponse;
import com.dmaiti.eventledger.account.dto.BalanceResponse;
import com.dmaiti.eventledger.account.dto.TransactionRequest;
import com.dmaiti.eventledger.account.dto.TransactionResponse;
import com.dmaiti.eventledger.account.model.TransactionType;
import com.dmaiti.eventledger.account.repository.AccountRepository;
import com.dmaiti.eventledger.account.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void applyCreditTransaction_returns201() {
        ResponseEntity<TransactionResponse> response = postTransaction(
                "acct-001", "evt-001", TransactionType.CREDIT, "500.00", "2026-05-10T10:00:00Z");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType()).isEqualTo(TransactionType.CREDIT);
        assertThat(response.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.getBody().getAccountId()).isEqualTo("acct-001");
        assertThat(response.getBody().getEventId()).isEqualTo("evt-001");
    }

    @Test
    void applyDebitTransaction_returns201() {
        ResponseEntity<TransactionResponse> response = postTransaction(
                "acct-002", "evt-002", TransactionType.DEBIT, "150.00", "2026-05-10T10:00:00Z");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getType()).isEqualTo(TransactionType.DEBIT);
        assertThat(response.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void balance_isCorrectAfterCreditsAndDebits() {
        postTransaction("acct-bal", "evt-b1", TransactionType.CREDIT, "500.00", "2026-05-10T10:00:00Z");
        postTransaction("acct-bal", "evt-b2", TransactionType.CREDIT, "200.00", "2026-05-11T10:00:00Z");
        postTransaction("acct-bal", "evt-b3", TransactionType.DEBIT,  "150.00", "2026-05-12T10:00:00Z");

        ResponseEntity<BalanceResponse> response = restTemplate.getForEntity(
                "/accounts/acct-bal/balance", BalanceResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getBalance()).isEqualByComparingTo(new BigDecimal("550.00"));
        assertThat(response.getBody().getTransactionCount()).isEqualTo(3);
    }

    @Test
    void balance_isZeroForAccountWithNoTransactions() {
        ResponseEntity<BalanceResponse> response = restTemplate.getForEntity(
                "/accounts/acct-new/balance", BalanceResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void outOfOrder_balanceIsCorrectRegardlessOfArrivalOrder() {
        // Arrive in order: May 12, May 8, May 10 (intentionally out of chronological order)
        postTransaction("acct-oor", "evt-oor1", TransactionType.CREDIT, "500.00", "2026-05-12T10:00:00Z");
        postTransaction("acct-oor", "evt-oor2", TransactionType.CREDIT, "200.00", "2026-05-08T09:00:00Z");
        postTransaction("acct-oor", "evt-oor3", TransactionType.DEBIT,  "150.00", "2026-05-10T14:00:00Z");

        ResponseEntity<BalanceResponse> balanceResp = restTemplate.getForEntity(
                "/accounts/acct-oor/balance", BalanceResponse.class);

        assertThat(balanceResp.getBody().getBalance()).isEqualByComparingTo(new BigDecimal("550.00"));
    }

    @Test
    void outOfOrder_transactionsReturnedOrderedByEventTimestamp() {
        // Arrive in order: May 12, May 8, May 10
        postTransaction("acct-ord", "evt-ord1", TransactionType.CREDIT, "500.00", "2026-05-12T10:00:00Z");
        postTransaction("acct-ord", "evt-ord2", TransactionType.CREDIT, "200.00", "2026-05-08T09:00:00Z");
        postTransaction("acct-ord", "evt-ord3", TransactionType.DEBIT,  "150.00", "2026-05-10T14:00:00Z");

        ResponseEntity<AccountResponse> accountResp = restTemplate.getForEntity(
                "/accounts/acct-ord", AccountResponse.class);

        List<TransactionResponse> txns = accountResp.getBody().getTransactions();
        assertThat(txns).hasSize(3);
        // Must be in chronological order: May 8 → May 10 → May 12
        assertThat(txns.get(0).getEventTimestamp()).isEqualTo(Instant.parse("2026-05-08T09:00:00Z"));
        assertThat(txns.get(1).getEventTimestamp()).isEqualTo(Instant.parse("2026-05-10T14:00:00Z"));
        assertThat(txns.get(2).getEventTimestamp()).isEqualTo(Instant.parse("2026-05-12T10:00:00Z"));
    }

    @Test
    void getAccountDetails_returnsCorrectBalanceAndTransactionCount() {
        postTransaction("acct-det", "evt-d1", TransactionType.CREDIT, "300.00", "2026-05-10T10:00:00Z");
        postTransaction("acct-det", "evt-d2", TransactionType.DEBIT,  "100.00", "2026-05-11T10:00:00Z");

        ResponseEntity<AccountResponse> response = restTemplate.getForEntity(
                "/accounts/acct-det", AccountResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAccountId()).isEqualTo("acct-det");
        assertThat(response.getBody().getBalance()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(response.getBody().getTransactions()).hasSize(2);
    }

    @Test
    void health_returnsUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    private ResponseEntity<TransactionResponse> postTransaction(String accountId, String eventId,
                                                                 TransactionType type, String amount,
                                                                 String timestamp) {
        TransactionRequest request = new TransactionRequest();
        request.setEventId(eventId);
        request.setType(type);
        request.setAmount(new BigDecimal(amount));
        request.setCurrency("USD");
        request.setEventTimestamp(Instant.parse(timestamp));
        return restTemplate.postForEntity("/accounts/" + accountId + "/transactions",
                request, TransactionResponse.class);
    }
}
