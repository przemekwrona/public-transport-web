package pl.wrona.webserver.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private String exceptionCode;

    public BusinessException(String exceptionCode, String message) {
        super(message);
        this.exceptionCode = exceptionCode;
    }
}
