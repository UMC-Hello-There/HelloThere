package com.example.hello_there.chat_room;

import com.example.hello_there.user_chatroom.UserChatRoom;
import com.example.hello_there.text_message.TextMessage;
import com.example.hello_there.utils.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom extends BaseTimeEntity {
    @Id
    private String chatRoomId;

    @Column(nullable = false)
    private String roomName; // 채팅방 이름

    @Column(nullable = false)
    private int userCount; // 채팅방 인원 수

    @Column(nullable = false)
    private int maxUserCount; // 채팅방 인원 제한

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean secretChk; // 채팅방 잠금 여부, default 값 false

    @Column(nullable = true)
    private String roomPassword; // 채팅방 비밀번호

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserChatRoom> userChatRooms = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TextMessage> textMessageList = new ArrayList<>();

    public void updateUserCount(int userCount){
        this.userCount = userCount;
    }
}

