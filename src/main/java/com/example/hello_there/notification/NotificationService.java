package com.example.hello_there.notification;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponseStatus;
import com.example.hello_there.notification.dto.PostNotificationReq;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.utils.UtilService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.hello_there.exception.BaseResponseStatus.*;
import static com.example.hello_there.notification.Notification.*;

@RequiredArgsConstructor
@Service
public class NotificationService {
    private final FirebaseMessaging firebaseMessaging;
    private final NotificationRepository notificationRepository;
    private final UtilService utilService;

    public String sendNotification(PostNotificationReq postNotificationReq) {
        User user = utilService.findByUserIdWithValidation(postNotificationReq.getUserId());

        // 해당 알림에 대한 UserSetting이 false인 경우
        if(!chkUserSetting(postNotificationReq.getNotificationType(), user)) {
            return null;
        }
        if (user.getDevice().getToken() != null) {
            Notification.Builder notificationBuilder = Notification.builder()
                    .setTitle(postNotificationReq.getTitle())
                    .setBody(postNotificationReq.getBody());
            if (postNotificationReq.getImgUrl() != null) {
                notificationBuilder.setImage(postNotificationReq.getImgUrl());
            }
            Notification notification = notificationBuilder.build();

            // 이 Message는 파이어베이스에서 사용하는 Message 클래스이다.
            Message message = Message.builder()
                    .setToken(user.getDevice().getToken())
                    .setNotification(notification)
                    .build();
            try {
                firebaseMessaging.send(message);
                notificationRepository.save(toEntity(postNotificationReq, user));
                return "User ID가 " + postNotificationReq.getUserId()
                        + "번인 사용자에게 알림 발신을 완료했습니다.";
            } catch (FirebaseMessagingException exception) {
                exception.printStackTrace();
                throw new BaseException(FAIL_TO_PUSH);
            }
        }
        else {
            throw new BaseException(INVALID_DEVICE_TOKEN);
        }
    }

    private Boolean chkUserSetting(NotificationType notificationType, User user) {
        switch (notificationType) {
            case COMMENT_CHECK:
                return user.getUserSettings().getCommentCheck();
            case RECOMMENT_CHECK:
                return user.getUserSettings().isRecommentCheck();
            case MESSAGE_CHECK:
                return user.getUserSettings().isMessageCheck();
            case BEST_BOARD_CHECK:
                return user.getUserSettings().isBestBoardCheck();
            case MESSAGE_RECEPTION:
                return user.getUserSettings().isMessageReceptionBlock();
            default:
                return null;
        }
    }
}
