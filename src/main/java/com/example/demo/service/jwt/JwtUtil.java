package com.example.demo.service.jwt;

import com.example.demo.entity.users.user.User;
import com.example.demo.exception.ComponentNotFoundException;
import com.example.demo.repository.users.user.UserRepository;
import com.example.demo.service.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtUtil {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisUtil redisUtil;
    private SecretKey secretKey;

    /**
     * JwtUtil 생성자.
     * @param secret JWT의 시크릿 키
     */
    public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
        // 시크릿 키 생성
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    /**
     * JWT에서 카테고리를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 카테고리 문자열
     */
    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    /**
     * JWT에서 사용자 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 ID 문자열
     */
    public String getUserId(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userId", String.class);
        } catch (ExpiredJwtException e) {
            return e.getClaims().get("userId", String.class);
        }
    }

    /**
     * JWT에서 역할(role)을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 역할 문자열
     */
    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    /**
     * JWT가 만료되었는지 확인합니다.
     *
     * @param token JWT 토큰
     * @return 만료 여부
     */
    public Boolean isExpired(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return Boolean.TRUE;
        }
    }

    /**
     * 새로운 JWT 토큰을 생성합니다.
     *
     * @param category 카테고리
     * @param userId 사용자 ID
     * @param role 역할
     * @param expiredMs 만료 시간 (밀리초)
     * @return 생성된 JWT 토큰
     */
    public String createToken(String category, String userId, String role, Long expiredMs) {
        String nickname = userRepository.findById(userId).get().getNickname();

        return Jwts.builder()
                .claim("category", category)
                .claim("userId", userId)
                .claim("role", role)
                .claim("nickname", nickname)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * HTTP 요청에서 Access Token을 가져옵니다.
     *
     * @param request HTTP 요청
     * @return Access Token 문자열
     */
    public String getAccessToken(HttpServletRequest request) {
        // request에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        // Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        // Bearer 부분 제거 후 순수 액세스 토큰만 획득
        return authorization.split(" ")[1];
    }

    /**
     * HTTP 요청에서 Refresh Token을 가져옵니다.
     *
     * @param request HTTP 요청
     * @return Refresh Token 문자열
     * @throws ComponentNotFoundException Refresh Token이 없는 경우 예외 발생
     */
    public String getRefreshToken(HttpServletRequest request) {
        String refreshToken = null;

        try {
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refreshToken = cookie.getValue();
                }
            }
        } catch (NullPointerException e) {
            String accessToken = getAccessToken(request);
            // Refresh 토큰 Redis에서 제거
            redisUtil.deleteData(accessToken);
            throw new ComponentNotFoundException("REFRESH_TOKEN_NOT_FOUND");
        }

        return refreshToken;
    }

    /**
     * Access Token으로 인증 객체를 생성합니다.
     *
     * @param token Access Token
     * @return Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

        String userId = claims.get("userId", String.class);
        log.info("Request By {}", userId);

        // 권한 정보를 포함하지 않음. 권한이 필요한 경우 기본 ROLE_USER로 설정
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        User principal = userRepository.findById(userId).orElseThrow(() -> new ComponentNotFoundException("ID:" + userId + " USER_NOT_FOUND"));

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }
}
