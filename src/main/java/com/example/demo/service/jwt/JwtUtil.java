package com.example.demo.service.jwt;

import com.example.demo.exception.ComponentNotFoundException;
import com.example.demo.service.RedisUtil;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import com.example.demo.repository.users.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
public class JwtUtil {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisUtil redisUtil;
    private SecretKey secretKey;

    public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
        // 시크릿 키 생성
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    /**
     * jwt 검증 후 토큰의 category 가져오기
     */
    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }


    /**
     * jwt 검증 후 email 가져오기
     */
    public String getUserId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userId", String.class);
    }

    /**
     * jwt 검증 후 role 가져오기
     */
    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    /**
     * jwt가 만료됐는지 확인
     */
    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    /**
     * Token 생성
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
     * Access Token 가져오는 메서드
     */
    public String getAccessToken(HttpServletRequest request) {
        //request에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        //Bearer 부분 제거 후 순수 액세스 토큰만 획득
        return authorization.split(" ")[1];
    }

    /**
     * Refresh Token 가져오는 메서드
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
            //Refresh 토큰 redis에서 제거
            redisUtil.deleteData(accessToken);

            throw new ComponentNotFoundException("REFRESH_TOKEN_NOT_FOUND");

        }

        return refreshToken;
    }

}
