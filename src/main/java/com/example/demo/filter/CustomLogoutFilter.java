package com.example.demo.filter;

import com.example.demo.service.JwtUtil;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomLogoutFilter extends GenericFilterBean {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // /member/logout 으로 GET 요청이 왔을 때 필터 로직 시작
        String requestUri = request.getRequestURI();
        if (!requestUri.matches("^\\/users\\/logout$")) {
            filterChain.doFilter(request, response);
            return;
        }
        String requestMethod = request.getMethod();
        if (!requestMethod.equals("GET")) {

            filterChain.doFilter(request, response);
            return;
        }

        //get refresh token
        String refreshToken = jwtUtil.getRefreshToken(request);

        // 리프레시 토큰 유효성 체크
        if (!checkRefreshTokenValid(response, refreshToken)) return;

        // get access token
        // 이미 만료돼서 재발급 된 것이므로 별도로 체크할 필요 없음
        String accessToken = jwtUtil.getAccessToken(request);

        // 리프레시 토큰이 디비에 저장되어 있지 않다면
        String data = redisUtil.getData(accessToken);
        if(!redisUtil.checkIfKeyExists(accessToken) && (data == null || !data.equals(accessToken))) {
            setFailResponse(response, "LOGOUT_FAIL", HttpStatus.BAD_REQUEST);
        }

        //로그아웃 진행
        doLogout(response, accessToken,jwtUtil.getUserId(accessToken));
    }

    private void doLogout(HttpServletResponse response, String accessToken, String userId) throws IOException {
        //Refresh 토큰 redis에서 제거
        redisUtil.deleteData(accessToken);

        //google Id, google Access Token redis에서 제거
        redisUtil.deleteData(userId);

        //Refresh 토큰 Cookie 값 0
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        setSuccessResponse(response, "LOGOUT_SUCCESS", HttpStatus.OK);
    }


    /**
     * refresh 토큰의 유효성 검사
     */
    private boolean checkRefreshTokenValid(HttpServletResponse response, String refreshToken) throws IOException {
        if (refreshToken == null) {
            //response status code
            setFailResponse(response, "LOGOUT_FAIL", HttpStatus.BAD_REQUEST);
            return false;
        }
        //expired check
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            //response status code
            setFailResponse(response, "LOGOUT_FAIL", HttpStatus.BAD_REQUEST);
            return false;
        }
        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals("refresh")) {
            //response status code
            setFailResponse(response, "LOGOUT_FAIL", HttpStatus.BAD_REQUEST);
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
