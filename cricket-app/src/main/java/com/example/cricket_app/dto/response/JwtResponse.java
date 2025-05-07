package com.example.cricket_app.dto.response;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String role;
}
