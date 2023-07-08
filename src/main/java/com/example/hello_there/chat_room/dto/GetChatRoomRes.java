package com.example.hello_there.chat_room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter // 해당 클래스에 대한 접근자 생성
@Setter // 해당 클래스에 대한 설정자 생성
@AllArgsConstructor
public class GetChatRoomRes {
    String chatRoomId;
    String roomName;
    int userCount;
}
