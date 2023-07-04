package com.example.hello_there.web_socket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커를 활성화하하여 WebSocket으로 메시지를 주고받을 수 있게 된다.
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompHandler stompHandler; // jwt 인증

    @Override
    // 메시지 브로커의 구성을 설정
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // "/topic/chat", "/topic/notify"를 브로커로 설정
        config.enableSimpleBroker("/topic/chat", "/topic/notify");
        config.setApplicationDestinationPrefixes("/app"); // 애플리케이션 메시지 처리를 위한 prefix
    }

    @Override
    // 웹 소켓 연결 엔드포인트를 등록하는 메서드
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // "/ws" 엔드포인트를 등록
                .setAllowedOriginPatterns("*") // 모든 출처에서의 WebSocket 연결을 허용한다.
                // .setAllowedOrigins("*") // 또는 이렇게 써도 된다. 하지만, 구식 기능이다.
                .withSockJS(); // SockJS를 활성화
        // SockJS는 WebSocket을 지원하지 않는 브라우저와도 통신할 수 있게 해주는 자바스크립트 라이브러리이다.
    }

    @Override
    // 클라이언트의 들어오는 채널을 구성하는 메서드
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // StompHandler를 클라이언트의 인바운드 채널(클라이언트로부터 메시지를 수신하는 채널)의 인터셉터로 등록한다.
        registration.interceptors(stompHandler);
    }
}
