package com.example.mail_service;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class AppResponse {
    private String message;
    private int code;
    private Object data;

    public static AppResponse success(Object data) {
        return  success(data, 2000);
    }

    public static AppResponse success(Object data, int status) {
        return AppResponse.builder()
                .code(status)
                .message("Success") // hardcode
                .data(data)
                .build();
    }

    public static AppResponse success() {
        return AppResponse.builder().code(2000).message("Success").build();
    }
}
