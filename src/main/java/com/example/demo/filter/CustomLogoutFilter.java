package com.example.demo.filter;

import com.example.demo.service.jwt.JwtUtil;
import com.example.demo.service.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomLogoutFilter extends GenericFilterBean {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();

        log.info("request uri: {}", requestUri);
        if (!requestUri.matches("^\\/users\\/logout$") || !requestMethod.equals("GET")) {
            filterChain.doFilter(request, response);
            return;
        }

        // get access token from Authorization header
        String authHeader = request.getHeader("Authorization");
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        if (accessToken == null) {
            log.error("Access token is null");
            setFailResponse(response, "LOGOUT_FAIL", HttpStatus.BAD_REQUEST);
            return;
        }

        // 리프레시 토큰이 디비에 저장되어 있지 않다면
        String data = redisUtil.getData(accessToken);
        if (!redisUtil.checkIfKeyExists(accessToken) && data == null) {
            setFailResponse(response, "LOGOUT_FAIL", HttpStatus.BAD_REQUEST);
            return;
        }

        // 로그아웃 진행
        doLogout(response, accessToken, jwtUtil.getUserId(accessToken));
    }


    private void doLogout(HttpServletResponse response, String accessToken, String userId) throws IOException {
        // Redis에서 accessToken과 관련된 데이터 삭제
        redisUtil.deleteData(accessToken);

        // Refresh 토큰 Cookie 값 0으로 설정하여 삭제
        Cookie refreshTokenCookie = new Cookie("refresh", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        // Access 토큰 Cookie 값 0으로 설정하여 삭제
        Cookie accessTokenCookie = new Cookie("access", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);

        setSuccessResponse(response, "LOGOUT_SUCCESS", HttpStatus.OK);
    }

    /**
     * refresh 토큰의 유효성 검사
     */
    private boolean checkRefreshTokenValid(HttpServletResponse response, String refreshToken) throws IOException {
        if (refreshToken == null || jwtUtil.isExpired(refreshToken)) {
            return false;
        }

        // 토큰이 refresh인지 확인 (발급 시 페이로드에 명시)
        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals("refresh")) {
            return false;
        }
        return true;
    }

    /**
     * 성공 응답 설정
     */
    private static void setSuccessResponse(HttpServletResponse response, String message, HttpStatus httpStatus) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(message);
        response.setStatus(httpStatus.value());
    }

    /**
     * 실패 응답 설정
     */
    private static void setFailResponse(HttpServletResponse response, String message, HttpStatus httpStatus) throws IOException {
        response.sendError(httpStatus.value(), message);
    }
}
