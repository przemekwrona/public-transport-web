package pl.wrona.webserver.otp;

import jakarta.annotation.Resource;
import org.igeolab.iot.otp.api.OtpApi;
import org.springframework.cloud.openfeign.FeignClient;

@Resource
@FeignClient(value = "${otp.client.warsaw.name}", url = "${otp.client.warsaw.url}")
public interface OtpClient extends OtpApi {
}
