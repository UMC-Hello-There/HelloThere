package com.example.hello_there.web_socket;

import com.example.hello_there.login.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {
    private final JwtProvider jwtProvider;

    @Override
    // 메시지가 전송되기 전에 호출되어 메시지를 가로채고 처리하는 역할을 수행
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        //  StompHeaderAccessor는 Stomp 메시지의 헤더 정보에 접근할 수 있도록 도와주는 유틸리티 클래스이다.
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if(accessor.getCommand() == StompCommand.CONNECT) { // Stomp 메시지의 command가 CONNECT인지 확인
            // Stomp 메시지의 native header 중 첫 번째 헤더 값을 반환, 즉 Authorization 헤더 값을 추출해 유효성 평가
            if(!jwtProvider.validateToken(accessor.getFirstNativeHeader("Authorization")))
                throw new AccessDeniedException("");
        }
        return message;
    }
}