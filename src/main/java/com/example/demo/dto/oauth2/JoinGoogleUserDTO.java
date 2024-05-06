package com.example.demo.dto.oauth2;

import lombok.Data;

@Data
public class JoinGoogleUserDTO {

    private final String googleId;
    private final String nickname;
    private final String role;
}
