package com.example.demo.filter;

import com.example.demo.exception.TokenInvalidException;
import com.example.demo.service.jwt.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends GenericFilter {

    private final JwtUtil jwtUtil;
    private static final Set<Pattern> noAuthUrl= Set.of(
            Pattern.compile("/oauth2/authorization/.*"),
            Pattern.compile("/users/logout/.*"),
            Pattern.compile("/users/reissue")
    );

    /**
     * JWT 필터 추가
     * @param request  The request to process
     * @param response The response associated with the request
     * @param chain    Provides access to the next filter in the chain for this
     *                 filter to pass the request and response to for further
     *                 processing
     *
     * @throws TokenInvalidException 토큰이 유효하지 않은 예외
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException{
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String jwtToken = jwtUtil.getAccessToken(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();

        if(noAuthUrl.stream().anyMatch(pattern -> pattern.matcher(requestURI).matches())) {
            log.info("Skipping authentication for URI: {}", requestURI);
            chain.doFilter(request, response);
            return;
        }

        if(StringUtils.hasText(jwtToken) && !jwtUtil.isExpired(jwtToken)) {
            //토큰 값에서 Authentication 값으로 가공해서 반환 후 저장
            Authentication authentication = jwtUtil.getAuthentication(jwtToken);
            log.info("authentication : {}", authentication.getPrincipal());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", SecurityContextHolder.getContext().getAuthentication().getPrincipal(), requestURI);
            //다음 필터로 넘기기
            chain.doFilter(request, response);
        } else {
            log.info("There Is Not Valid Jwt. requestURI : {}", requestURI);
            throw new TokenInvalidException(jwtToken);
        }
    }
}