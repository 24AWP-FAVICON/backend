package com.example.demo.handler;

import com.example.demo.service.jwt.JwtUtil;
import com.example.demo.service.RedisUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.demo.dto.users.user.CustomOAuth2User;
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

        String userId = customOAuth2User.getName();

        log.info("googleId {}",userId);

        OAuth2AuthorizedClient client = oAuth2AuthorizedClientService.loadAuthorizedClient(
                "google", authentication.getName());
        String googleAccessToken = client.getAccessToken().getTokenValue();

        redisUtil.setData(userId, googleAccessToken);


        // jwt 처리
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String accessToken = jwtUtil.createToken("access", userId, role, 1000*60*60L); // access token 생성 유효기간 1시간
        String refreshToken = jwtUtil.createToken("refresh", userId, role, 1000*60*60*24L); // refresh token 생성 유효기간 24시간

        redisUtil.setData(accessToken, refreshToken); // 레디스에 access token과 refresh token 저장

        response.addHeader("Authorization", "Bearer " + accessToken); // access token은 Authorization 헤더에
        response.addCookie(createCookie("access", accessToken)); // accessToken은 쿠키에
        response.addCookie(createCookie("refresh", refreshToken)); // refresh token은 쿠키에
        response.setStatus(HttpStatus.OK.value());
        response.getWriter().write("SOCIAL_LOGIN_SUCCESS");
        response.sendRedirect("http://localhost:3000/login/success");
    }

    private Cookie createCookie(String key ,String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60); // 쿠키 유효시간 24시간으로 설정
        cookie.setPath("/"); // 모든 곳에서 쿠키열람이 가능하도록 설정
        return cookie;
    }
}
