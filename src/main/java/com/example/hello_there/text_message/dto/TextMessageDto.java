package com.example.hello_there.text_message.dto;

import com.example.hello_there.text_message.TextMessage.TextMessageType;
import lombok.*;

@Getter // 해당 클래스에 대한 접근자 생성
@Setter // 해당 클래스에 대한 설정자 생성
@AllArgsConstructor // 해당 클래스의 모든 멤버 변수를 받는 생성자를 생성
@NoArgsConstructor
@Builder
public class TextMessageDto {
    private TextMessageType type; // 메시지 타입
    private String roomId; // 방 번호
    private Long senderId; // 채팅을 보낸 사람
    private String message; // 메시지
    private String sendDate; // 채팅 발송 날짜
    private String sendTime; // 채팅 발송 시간
}
