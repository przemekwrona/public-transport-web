package pl.wrona.webserver.blik.business.transactions;

import lombok.RequiredArgsConstructor;
import org.igeolab.iot.pt.server.blik.api.model.Transaction;
import org.igeolab.iot.pt.server.blik.api.model.TransactionsResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.blik.client.TransactionClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {


    private final TransactionClient transactionClient;


    public List<Transaction> findTransactionsAggregationByAccountNumerAndSinceYear(String accountNumber, int year) {

        String accountId = accountNumber.toUpperCase();

        List<Transaction> returnTransactions = new ArrayList<>();
        for (int currentYear = year; currentYear >= year; currentYear--) {
            var transactions = findTransactionsAggregationByAccountNumerAndYear(accountNumber, currentYear);
            returnTransactions.addAll(transactions);

            if (transactions.isEmpty()) {
                break;
            }
        }

        return returnTransactions;

    }

    public List<Transaction> findTransactionsAggregationByAccountNumerAndYear(String accountId, int year) {

        int daysToDownload = 50;
        LocalDate now = LocalDate.of(year, 1, 1);

        List<Transaction> listOfTransactions = new ArrayList<>();

        TransactionsResponse transactionsBodyResponse = transactionClient.accountsAccountIdTransactionsGet(accountId, now, daysToDownload).getBody();
        listOfTransactions.addAll(transactionsBodyResponse.getTransactions());

        while (!transactionsBodyResponse.getTransactions().isEmpty() ) {
            now = now.plusDays(daysToDownload);

            var transactionsBody = transactionClient.accountsAccountIdTransactionsGet(accountId, now, daysToDownload).getBody();
            listOfTransactions.addAll(transactionsBody.getTransactions());
        }

        return listOfTransactions;
    }
}
