package com.example.demo.websocket.config;

import com.example.demo.service.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.messaging.simp.config.ChannelRegistration;

import java.util.Map;
import java.util.List;


/**
 * WebSocket 설정을 담당하는 클래스입니다.
 * STOMP 프로토콜을 사용한 메시지 브로커 구성과 WebSocket 연결을 위한 엔드포인트를 설정합니다.
 */
@Configuration
@EnableWebSocketMessageBroker // STOMP 프로토콜을 사용하기 위해 WebSocket 메시지 브로커를 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    // application.properties의 api.base-url 값 주입
    @Value("${api.base-url}")
    private String apiBaseUrl;

    /**
     * 메시지 브로커를 구성합니다.
     * 클라이언트가 구독하는 경로와 메시지 발송 경로를 설정합니다.
     *
     * @param config MessageBrokerRegistry 객체
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /sub 경로를 통해 클라이언트가 메시지를 구독할 수 있도록 설정
        config.enableSimpleBroker("/sub"); // 클라이언트 사용자의 구독경로: /sub/channel/채널아이디 /sub/channel/1
        // /pub 경로로 시작하는 메시지는 애플리케이션으로 라우팅됨
        config.setApplicationDestinationPrefixes("/pub"); // 메시지 발송할 때: public/hello (메시지에 채널아이디 포함해야 함)
    }

    /**
     * STOMP 엔드포인트를 등록합니다.
     * 클라이언트가 WebSocket 연결을 생성할 때 사용할 엔드포인트를 정의합니다.
     *
     * @param registry StompEndpointRegistry 객체
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // WebSocket 연결을 위한 엔드포인트 설정
                .setAllowedOrigins(apiBaseUrl) // 허용된 출처 설정
                .addInterceptors(new HttpSessionHandshakeInterceptor()) // HTTP 세션을 WebSocket 세션으로 연결
                .withSockJS(); // SockJS 지원
    }
}

/**
 * JWT 토큰을 핸드셰이크 과정에서 처리하는 인터셉터입니다.
 * WebSocket 연결 시 클라이언트가 전달한 JWT 토큰을 추출하여 연결 속성에 저장합니다.
 */
class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    JwtHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * WebSocket 연결이 이루어지기 전에 호출됩니다.
     * URI에 포함된 JWT 토큰을 추출하여 연결 속성에 저장합니다.
     *
     * @param request 서버로 들어오는 HTTP 요청
     * @param response 서버에서 클라이언트로 전송하는 HTTP 응답
     * @param wsHandler WebSocket 핸들러
     * @param attributes WebSocket 세션에 저장할 속성
     * @return 핸드셰이크 성공 여부
     * @throws Exception 핸드셰이크 중 발생할 수 있는 예외
     */
    @Override
    public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request,
                                   org.springframework.http.server.ServerHttpResponse response,
                                   org.springframework.web.socket.WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String token = query.substring(query.indexOf("token=") + 6);
            attributes.put("token", token);  // JWT 토큰을 속성에 저장
        }
        return true;
    }
    /**
     * WebSocket 연결이 완료된 후 호출됩니다.
     *
     * @param request 서버로 들어오는 HTTP 요청
     * @param response 서버에서 클라이언트로 전송하는 HTTP 응답
     * @param wsHandler WebSocket 핸들러
     * @param exception 핸드셰이크 중 발생한 예외
     */
    @Override
    public void afterHandshake(org.springframework.http.server.ServerHttpRequest request,
                               org.springframework.http.server.ServerHttpResponse response,
                               org.springframework.web.socket.WebSocketHandler wsHandler,
                               Exception exception) {
        // 핸드셰이크 후 추가 작업은 필요하지 않음
    }
}