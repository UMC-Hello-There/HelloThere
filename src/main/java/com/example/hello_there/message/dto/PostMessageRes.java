package com.example.hello_there.message.dto;

import com.example.hello_there.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import reactor.netty.udp.UdpServer;

import java.time.LocalDateTime;

@Getter // 해당 클래스에 대한 접근자 생성
@Setter // 해당 클래스에 대한 설정자 생성
@AllArgsConstructor // 해당 클래스의 모든 멤버 변수를 받는 생성자를 생성
@NoArgsConstructor
public class PostMessageRes {
    private User sender;
    private User receiver;
    private String message;
    private LocalDateTime sendTime;
}
