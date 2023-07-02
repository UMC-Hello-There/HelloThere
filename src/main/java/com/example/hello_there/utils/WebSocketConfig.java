package com.example.hello_there.utils;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.login.jwt.JwtProvider;
import com.example.hello_there.login.jwt.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration // 스프링의 설정 클래스임을 나타낸다.
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커를 활성화하는 어노테이션이다.
@RequiredArgsConstructor
// WebSocket 메시지 브로커를 구성하기 위한 인터페이스에 대한 구체 클래스
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtProvider jwtProvider;
    private final JwtService jwtService;

    @Override
    // STOMP 엔드포인트를 등록하는 역할을 하는 registerStompEndpoints를 오버라이딩
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // STOMP의 엔드포인트를 /ws 경로로 등록
                .setAllowedOrigins("*"); // WebSocket 연결을 시도하는 모든 IP(Origin)를 허용
        // 보안을 위해서는 특정 origin만 등록하는 걸 권장
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // prefix가 /sub인 클라이언트에게 서버가 메시지를 보낼 수 있게 된다.
        // 즉, 서버에서 클라이언트로 메시지를 push하기 위해 사용된다.
        registry.enableSimpleBroker("/sub");

        // 클라이언트에서 서버로 메시지를 보낼 때 사용할 prefix를 지정하는 메서드
        // 클라이언트는 /pub 경로를 이용해 서버로 메시지를 전송할 수 있다.
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new FilterChannelInterceptor());
    }

    @Order(Ordered.HIGHEST_PRECEDENCE + 99)
    public class FilterChannelInterceptor implements ChannelInterceptor {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            assert headerAccessor != null;

            if (headerAccessor.getCommand() == StompCommand.CONNECT) { // 연결 시에한 header 확인
                String token = String.valueOf(headerAccessor.getNativeHeader("Authorization").get(0));
                String accessToken = jwtProvider.BearerRemove(token);

                try {
                    Long memberId = jwtService.getUserIdx(accessToken);
                    headerAccessor.addNativeHeader("Member", String.valueOf(memberId));
                } catch (ExpiredJwtException e) {
                    e.printStackTrace();
                } catch (BaseException e) {
                    e.printStackTrace();
                } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
                    e.printStackTrace();
                } catch (UnsupportedJwtException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            return message;
        }
    }
}