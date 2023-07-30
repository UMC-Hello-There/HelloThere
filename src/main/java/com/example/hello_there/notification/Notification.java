package com.example.hello_there.notification;

import com.example.hello_there.board.BoardType;
import com.example.hello_there.notification.dto.PostNotificationReq;
import com.example.hello_there.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType; // 알림의 종류

    @Column(nullable = false)
    private String title; // 알림의 제목

    @Column(nullable = false)
    private String body; // 알림의 내용

    @Column(nullable = false)
    private String imgUrl; // 푸쉬 알림에 이미지를 사용하기 위해 추가

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 알림을 받을 사용자

    public static Notification toEntity(PostNotificationReq dto, User user) {
        return Notification.builder()
                .title(dto.getTitle())
                .body(dto.getBody())
                .imgUrl(dto.getImgUrl())
                .user(user)
                .build();
    }
}
