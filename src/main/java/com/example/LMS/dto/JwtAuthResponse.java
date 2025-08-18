package com.example.LMS.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtAuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";

    public JwtAuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // getters and setters
}
