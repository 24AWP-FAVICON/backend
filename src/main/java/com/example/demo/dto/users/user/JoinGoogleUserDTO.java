package com.example.demo.dto.users.user;

import lombok.Data;

@Data
public class JoinGoogleUserDTO {

    private final String userId;
    private final String nickname;
    private final String role;
}
