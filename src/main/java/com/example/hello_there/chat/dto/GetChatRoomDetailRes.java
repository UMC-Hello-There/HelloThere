package com.example.hello_there.chat.dto;

import com.example.hello_there.message.Message;
import com.example.hello_there.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter // 해당 클래스에 대한 접근자 생성
@Setter // 해당 클래스에 대한 설정자 생성
@AllArgsConstructor // 해당 클래스의 모든 멤버 변수를 받는 생성자를 생성
public class GetChatRoomDetailRes {
    private Long chatRoomId;
    private User participant;
    private User inviter;
    private List<Message> messages;
}
