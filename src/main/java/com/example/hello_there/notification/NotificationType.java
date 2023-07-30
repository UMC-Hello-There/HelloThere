package com.example.hello_there.notification;

import lombok.Getter;

@Getter
public enum NotificationType {
    COMMENT_CHECK(1), // 댓글 알림
    RECOMMENT_CHECK(2), // 대댓글 알림
    MESSAGE_CHECK(3), // 쪽지 알림
    BEST_BOARD_CHECK(4), // 인기 게시물 선정 알림
    MESSAGE_RECEPTION(5); // 쪽지 수신 및 발신 제한

    private int value;

    NotificationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
