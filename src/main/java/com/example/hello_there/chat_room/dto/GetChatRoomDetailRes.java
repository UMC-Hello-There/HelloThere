package com.example.hello_there.chat_room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter // 해당 클래스에 대한 접근자 생성
@Setter // 해당 클래스에 대한 설정자 생성
@AllArgsConstructor
@NoArgsConstructor
public class GetChatRoomDetailRes {
    Long userId;
    String nickName;
    String fileName;
    String imgUrl;
    List<String> message;
    String sendDate;
    String sendTime;
}
