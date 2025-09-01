package com.app.backend.controller.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@Builder
public class ResultDTO<T> {
    private HttpStatus statusCode;
    private String resultMsg;
    private T resultData;

    public ResultDTO(final HttpStatus statusCode, final String resultMsg) {
        this.statusCode = statusCode;
        this.resultMsg = resultMsg;
        this.resultData = null;
    }

    // 1번 메소드
    public static<T> ResultDTO<T> res(final HttpStatus statusCode, final String resultMsg) {
        return res(statusCode, resultMsg, null);
    }
    // 2번 메소드
    public static<T> ResultDTO<T> res(final HttpStatus statusCode, final String resultMsg, final T t) {
        return ResultDTO.<T>builder()
                .statusCode(statusCode)
                .resultMsg(resultMsg)
                .resultData(t)
                .build();
    }
}
