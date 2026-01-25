package pl.wrona.webserver.blik.business.transactions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import pl.wrona.webserver.BaseIntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest extends BaseIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Test
    public void shouldRequestTransactions() {
        //given
        String acountId = "08969347-af5f-40d7-ba2d-1d059fad4c85";
        int year = 2025;

        //when
        var transactions = transactionService.findTransactionsAggregationByAccountNumerAndSinceYear(acountId, 2025);

        //then
        int a = 0;
    }

}