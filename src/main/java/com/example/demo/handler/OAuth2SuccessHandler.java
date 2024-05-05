package com.example.demo.handler;

import com.example.demo.service.JwtUtil;
import com.example.demo.service.RedisUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.demo.dto.oauth2.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String googleId = customOAuth2User.getName();

        log.info("googleId {}",googleId);

        OAuth2AuthorizedClient client = oAuth2AuthorizedClientService.loadAuthorizedClient(
                "google", authentication.getName());
        String googleAccessToken = client.getAccessToken().getTokenValue();

        redisUtil.setData(googleId, googleAccessToken);


        // jwt 처리
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String accessToken = jwtUtil.createToken("access", googleId, role, 1000*60*10L); // access token 생성 유효기간 10분
        String refreshToken = jwtUtil.createToken("refresh", googleId, role, 1000*60*60*24L); // refresh token 생성 유효기간 24시간

        redisUtil.setData(accessToken, refreshToken); // 레디스에 리프레시 토큰 저장

        response.addHeader("Authorization", "Bearer " + accessToken); // access token은 Authorization 헤더에
        response.addCookie(createCookie(refreshToken)); // refresh token은 쿠키에
        response.setStatus(HttpStatus.OK.value());
        response.getWriter().write("Social_LOGIN_SUCCESS");
    }

    private Cookie createCookie(String value) {

        Cookie cookie = new Cookie("refresh", value);
        cookie.setMaxAge(24*60*60); // 쿠키 유효시간 24시간으로 설정
        cookie.setHttpOnly(true); // XSS 공격 방어

        return cookie;
    }
}
