package com.example.hello_there.chat_room.dto;

import com.example.hello_there.chat_room.ChatRoom;
import com.example.hello_there.text_message.TextMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter // 해당 클래스에 대한 접근자 생성
@Setter // 해당 클래스에 대한 설정자 생성
@AllArgsConstructor
@NoArgsConstructor
public class GetChatRoomRes {
    private String chatRoomId;
    private String roomName;
    private String latestMessage; // 가장 최근 메시지
    private String latestDate; // 가장 최근 메시지의 날짜
    private String latestTime; // 가장 최근 메시지의 시간
    private int unreadCount; // 읽지 않은 메시지 수

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetChatRoomRes that = (GetChatRoomRes) o;
        return Objects.equals(chatRoomId, that.chatRoomId) &&
                Objects.equals(roomName, that.roomName) &&
                Objects.equals(latestMessage, that.latestMessage) &&
                Objects.equals(latestDate, that.latestDate) &&
                Objects.equals(latestTime, that.latestTime) &&
                Objects.equals(unreadCount, that.unreadCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatRoomId, roomName, latestMessage, latestDate, latestTime, unreadCount);
    }
}
