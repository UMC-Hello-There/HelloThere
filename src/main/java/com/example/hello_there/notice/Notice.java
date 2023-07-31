package com.example.hello_there.notice;

import com.example.hello_there.board.BoardType;
import com.example.hello_there.user.User;
import com.example.hello_there.user_chatroom.UserChatRoom;
import com.example.hello_there.user_notice.UserNotice;
import com.example.hello_there.utils.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Notice extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    @Column(nullable = false)
    private String title; // 알림의 제목

    @Column(nullable = false)
    private String body; // 알림의 내용

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private BoardType boardType;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserNotice> userNotices = new ArrayList<>(); // 알림을 받을 사용자를 추적
}
