package pl.wrona.webserver.blik.client;

import org.igeolab.iot.pt.server.blik.transaction.api.TransactionsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "transaction-api", value = "http://34.116.239.211:8080/api")
public interface TransactionClient extends TransactionsApi {
}
