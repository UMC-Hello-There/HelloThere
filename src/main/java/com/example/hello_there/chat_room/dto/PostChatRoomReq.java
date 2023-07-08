package com.example.hello_there.chat_room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter // 해당 클래스에 대한 접근자 생성
@Setter // 해당 클래스에 대한 설정자 생성
@AllArgsConstructor // 해당 클래스의 모든 멤버 변수(userIdx, nickname)를 받는 생성자를 생성
@NoArgsConstructor
public class PostChatRoomReq {
    private String roomName;
    private int maxUserCount;
    private boolean secretChk;
    private String roomPassword;
}
