package com.example.hello_there.notification;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.notification.dto.PostNotificationReq;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/notification")
    public BaseResponse<String> sendNotification(@RequestBody PostNotificationReq postNotificationReq) {
        try {
            return new BaseResponse<>(notificationService.sendNotification(postNotificationReq));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
