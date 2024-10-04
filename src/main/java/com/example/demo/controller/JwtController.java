package com.example.demo.controller;

import com.example.demo.service.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.demo.service.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * JwtController는 JWT 토큰을 재발급하는 기능을 제공하는 컨트롤러입니다.
 */
@RestController()
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class JwtController {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    /**
     * JWT 토큰을 재발급합니다.
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @throws IOException I/O 예외가 발생할 경우
     */
    @PostMapping("/reissue")
    public void reissue(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 리프레시 토큰 가져오기
        String refreshToken = jwtUtil.getRefreshToken(request);

        // 리프레시 토큰 유효성 체크
        if (!checkRefreshTokenValid(response, refreshToken)) return;

        // 액세스 토큰 가져오기
        String accessToken = jwtUtil.getAccessToken(request);

        // 리프레시 토큰이 디비에 저장되어 있지 않은지 확인 (로그아웃된 경우)
        String data = redisUtil.getData(accessToken);
        if(!redisUtil.checkIfKeyExists(accessToken) && (data == null || !data.equals(refreshToken))) {
            setFailResponse(response, "INVALID_REFRESH_TOKEN", HttpStatus.UNAUTHORIZED);
            return;
        }

        String email = jwtUtil.getUserId(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        // 새로운 JWT 토큰 생성
        String newAccess = jwtUtil.createToken("access", email, role, 1000*60*60L);
        String newRefresh = jwtUtil.createToken("refresh", email, role, 1000*60*60*24L);

        redisUtil.deleteData(accessToken); // 기존 리프레시 토큰 삭제
        redisUtil.setData(newAccess, newRefresh, 1000*60*60*24L, TimeUnit.MILLISECONDS);

        // 응답 설정
        response.addHeader("Authorization", "Bearer " + newAccess);
        response.addCookie(createCookie(newRefresh));

        setSuccessResponse(response, "TOKEN_REISSUE_SUCCESS", HttpStatus.OK);
    }

    /**
     * 리프레시 토큰의 유효성을 검사합니다.
     *
     * @param response    HTTP 응답 객체
     * @param refreshToken 검증할 리프레시 토큰
     * @return 유효한 경우 true, 그렇지 않으면 false
     * @throws IOException I/O 예외가 발생할 경우
     */
    private boolean checkRefreshTokenValid(HttpServletResponse response, String refreshToken) throws IOException {
        if (refreshToken == null) {
            setFailResponse(response, "REFRESH_TOKEN_NULL", HttpStatus.UNAUTHORIZED);
            return false;
        }
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            setFailResponse(response, "REFRESH_TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED);
            return false;
        }

        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals("refresh")) {
            setFailResponse(response, "INVALID_REFRESH_TOKEN", HttpStatus.UNAUTHORIZED);
        }
        return true;
    }

    /**
     * 새로운 리프레시 토큰을 위한 쿠키를 생성합니다.
     *
     * @param value 쿠키에 저장할 값
     * @return 생성된 쿠키 객체
     */
    private Cookie createCookie(String value) {
        Cookie cookie = new Cookie("refresh", value);
        cookie.setMaxAge(24*60*60); // 쿠키 유효시간 24시간으로 설정
        cookie.setHttpOnly(true);
        return cookie;
    }

    /**
     * 성공 응답을 설정합니다.
     *
     * @param response   HTTP 응답 객체
     * @param message    성공 메시지
     * @param httpStatus 응답 상태 코드
     * @throws IOException I/O 예외가 발생할 경우
     */
    private static void setSuccessResponse(HttpServletResponse response, String message, HttpStatus httpStatus) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(message);
        response.setStatus(httpStatus.value());
    }

    /**
     * 실패 응답을 설정합니다.
     *
     * @param response   HTTP 응답 객체
     * @param message    실패 메시지
     * @param httpStatus 응답 상태 코드
     * @throws IOException I/O 예외가 발생할 경우
     */
    private static void setFailResponse(HttpServletResponse response, String message, HttpStatus httpStatus) throws IOException {
        response.sendError(httpStatus.value(), message);
    }
}
