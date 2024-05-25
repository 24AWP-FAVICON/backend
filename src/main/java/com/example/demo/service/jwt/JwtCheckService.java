package com.example.demo.service.jwt;

import com.example.demo.exception.TokenExpiredException;
import com.example.demo.exception.TokenInvalidException;
import com.example.demo.service.jwt.JwtUtil;
import com.example.demo.service.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class JwtCheckService {

    private final RedisUtil redisUtil;

    private final JwtUtil jwtUtil;

    public String checkJwt(HttpServletRequest request, HttpServletResponse response) {

        String accessToken = request.getHeader("Authorization");
        if (accessToken == null)
            throw new TokenInvalidException("TOKEN_NOT_FOUND");

        accessToken= accessToken.replace("Bearer ","");

        //redis에서 토큰 확인
        if (!redisUtil.checkIfKeyExists(accessToken)) {
            log.warn("Access Token INVALID");
            throw new TokenInvalidException("TOKEN_NOT_FOUND");
        }

        //토큰 소멸 시간 검증
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            log.warn("Access Token Expired");
            throw new TokenExpiredException("TOKEN_EXPIRED");
        }

        //토큰에서 userId 획득
        return jwtUtil.getUserId(accessToken);

    }
}
