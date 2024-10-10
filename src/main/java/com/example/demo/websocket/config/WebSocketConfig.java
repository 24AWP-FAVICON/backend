package com.example.demo.websocket.config;

import com.example.demo.service.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

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