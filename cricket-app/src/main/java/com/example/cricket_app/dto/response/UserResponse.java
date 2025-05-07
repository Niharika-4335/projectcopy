package com.example.cricket_app.dto.response;


import com.example.cricket_app.enums.UserRole;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private UserRole role;
    private Double balance;
}
