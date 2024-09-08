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

@RestController()
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class JwtController {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @PostMapping("/reissue")
    public void reissue(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //get refresh token
        String refreshToken = jwtUtil.getRefreshToken(request);

        // 리프레시 토큰 유효성 체크
        if (!checkRefreshTokenValid(response, refreshToken)) return;

        // get access token
        // 이미 만료돼서 재발급 된 것이므로 별도로 체크할 필요 없음
        String accessToken = jwtUtil.getAccessToken(request);

        // 리프레시 토큰이 디비에 저장되어 있지 않다면(로그아웃 한 경우에 해당)
        // 이미 로그아웃해서 디비에는 리프레시 토큰이 삭제됐는데 해커가 이전에 리프레시 토큰을 탈취해 복제해 놓은 경우를 방지
        String data = redisUtil.getData(accessToken);
        if(!redisUtil.checkIfKeyExists(accessToken) && (data == null || !data.equals(refreshToken))) {
            setFailResponse(response, "INVALID_REFRESH_TOKEN", HttpStatus.UNAUTHORIZED);
            return;
        }


        String email = jwtUtil.getUserId(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        //make new JWT
        String newAccess = jwtUtil.createToken("access", email, role, 1000*60*60L);
        String newRefresh = jwtUtil.createToken("refresh", email, role, 1000*60*60*24L);

        redisUtil.deleteData(accessToken); // 기존 리프레시 토큰 삭제
        redisUtil.setData(newAccess, newRefresh, 1000*60*60*24L, TimeUnit.MILLISECONDS);
        
        //response
        response.addHeader("Authorization", "Bearer " + newAccess);
        response.addCookie(createCookie(newRefresh));

        setSuccessResponse(response, "TOKEN_REISSUE_SUCCESS", HttpStatus.OK);
    }

    /**
     * refresh 토큰의 유효성 검사
     */
    private boolean checkRefreshTokenValid(HttpServletResponse response, String refreshToken) throws IOException {
        if (refreshToken == null) {
            //response status code
            setFailResponse(response, "REFRESH_TOKEN_NULL", HttpStatus.UNAUTHORIZED);
            return false;
        }
        //expired check
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            //response status code
            setFailResponse(response, "REFRESH_TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED);
            return false;
        }
        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals("refresh")) {
            //response status code
            setFailResponse(response, "INVALID_REFRESH_TOKEN", HttpStatus.UNAUTHORIZED);
        }
        return true;
    }

    private Cookie createCookie(String value) {

        Cookie cookie = new Cookie("refresh", value);
        cookie.setMaxAge(24*60*60); // 쿠키 유효시간 24시간으로 설정
        cookie.setHttpOnly(true);

        return cookie;
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
