package com.example.hello_there.notice;

import com.example.hello_there.board.Board;
import com.example.hello_there.comment.Comment;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.notice.dto.PostNoticeRes;
import com.example.hello_there.text_message.TextMessage;
import com.example.hello_there.user.User;
import com.example.hello_there.user.user_setting.UserSetting;
import com.example.hello_there.user.user_setting.UserSettingRepository;
import com.example.hello_there.user_chatroom.UserChatRoom;
import com.example.hello_there.user_notice.UserNotice;
import com.example.hello_there.user_notice.UserNoticeRepository;
import com.example.hello_there.utils.UtilService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.hello_there.exception.BaseResponseStatus.*;

@RequiredArgsConstructor
@Service
public class NoticeService {
    private final FirebaseMessaging firebaseMessaging;
    private final NoticeRepository noticeRepository;
    private final UserSettingRepository userSettingRepository;
    private final UserNoticeRepository userNoticeRepository;
    private final UtilService utilService;

    /**
     * 댓글 알림
     */
    public PostNoticeRes sendCommentNotification(Long commentId) {
        Comment comment = utilService.findByCommentIdWithValidation(commentId);
        User user = comment.getBoard().getUser(); // 게시글 작성자에게 알림을 보냄
        Notice notice = Notice.builder()
                .title(comment.getBoard().getTitle())
                .body("새로운 댓글이 달렸어요! " + comment.getContent())
                .boardType(comment.getBoard().getBoardType())
                .build();
        createNotice(user, notice); // 알림 생성 및 저장

        UserSetting userSetting = userSettingRepository.findByUserId(user.getId());
        if(userSetting.isCommentCheck()) { // 해당 알림에 대한 UserSetting이 true인 경우
            sendNotification(user, notice); // 푸시 알림 전송
        }
        return new PostNoticeRes(notice, comment.getBoard().getBoardType());
    }

    /**
     * 대댓글 알림
     */
    public PostNoticeRes sendRecommentNotification(Long commentId) {
        Comment comment = utilService.findByCommentIdWithValidation(commentId);
        if(comment.getParent() == null) {
            throw new BaseException(NONE_EXIST_PARENT_COMMENT);
        }
        Comment parentComment = comment.getParent();
        User user = parentComment.getUser(); // 부모 댓글 작성자에게 알림을 보냄
        Notice notice = Notice.builder()
                .title(parentComment.getContent())
                .body("대댓글이 달렸어요! " + comment.getContent())
                .boardType(comment.getBoard().getBoardType())
                .build();
        createNotice(user, notice);

        UserSetting userSetting = userSettingRepository.findByUserId(user.getId());
        if(userSetting.isRecommentCheck()) {
            sendNotification(user, notice);
        }
        return new PostNoticeRes(notice, comment.getBoard().getBoardType());
    }

    /**
     * 인기 게시글 선정 알림
     */
    public PostNoticeRes sendBestBoardNotification(Long boardId) {
        Board board = utilService.findByBoardIdWithValidation(boardId);
        User user = board.getUser(); // 게시글 작성자에게 알림을 보냄
        Notice notice = Notice.builder()
                .title(board.getTitle())
                .body("게시글이 인기 게시물로 선정되었어요!")
                .boardType(board.getBoardType())
                .build();
        createNotice(user, notice);

        UserSetting userSetting = userSettingRepository.findByUserId(user.getId());
        if(userSetting.isBestBoardCheck()) {
            sendNotification(user, notice);
        }
        return new PostNoticeRes(notice, board.getBoardType());
    }

    /**
     * 쪽지 알림
     */
    public PostNoticeRes sendMessageNotification(Long messageId) {
        TextMessage textMessage = utilService.findByTextMessageIdWithValidation(messageId);
        List<UserChatRoom> userChatRooms = textMessage.getChatRoom().getUserChatRooms();
        List<User> userList = new ArrayList<>(); // 채팅방에 있는 모든 유저에게 알림을 보냄
        for (UserChatRoom userChatRoom : userChatRooms) {
            userList.add(userChatRoom.getUser());
        }

        Notice notice = Notice.builder()
                .title(textMessage.getSender().getNickName())
                .body(textMessage.getMessage())
                .boardType(null)
                .build();

        for (User user : userList) {
            createNotice(user, notice);
            UserSetting userSetting = userSettingRepository.findByUserId(user.getId());
            if(userSetting.isMessageCheck()) {
                sendNotification(user, notice);
            }
        }
        return new PostNoticeRes(notice, null);
    }

    /**
     * 푸시 알림 전송을 위한 공통 메서드
     */
    private void sendNotification(User user, Notice notice) {
        if (user.getDevice().getToken() != null) {
            Notification.Builder notificationBuilder = Notification.builder()
                    .setTitle(notice.getTitle())
                    .setBody(notice.getBody());
            Notification notification = notificationBuilder.build();

            Message message = Message.builder()
                    .setToken(user.getDevice().getToken())
                    .setNotification(notification)
                    .build();
            try {
                firebaseMessaging.send(message);
            } catch (FirebaseMessagingException exception) {
                exception.printStackTrace();
                throw new BaseException(FAIL_TO_PUSH);
            }
        }
        else {
            throw new BaseException(INVALID_DEVICE_TOKEN);
        }
    }

    /**
     * 알림 생성을 위한 공통 메서드
     */
    private void createNotice(User user, Notice notice) {
        UserNotice userNotice = new UserNotice();
        userNotice.setNotice(notice);
        userNotice.setUser(user);
        noticeRepository.save(notice);
        userNoticeRepository.save(userNotice);
    }
}
