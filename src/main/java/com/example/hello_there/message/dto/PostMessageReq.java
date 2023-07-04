package com.example.hello_there.message.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter // 해당 클래스에 대한 접근자 생성
@Setter // 해당 클래스에 대한 설정자 생성
@AllArgsConstructor // 해당 클래스의 모든 멤버 변수를 받는 생성자를 생성
public class PostMessageReq {
    private String message;
    private Long senderId;
    private Long receiverId;
    private Long chatRoomId;
}
