package com.example.hello_there.notification.dto;

import com.example.hello_there.board.photo.dto.GetS3Res;
import com.example.hello_there.notification.Notification;
import com.example.hello_there.notification.NotificationType;
import com.example.hello_there.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostNotificationReq {
    private Long userId;
    private NotificationType notificationType;
    private String title;
    private String body;
    private String imgUrl;
}
