package com.example.hello_there.utils;

import com.example.hello_there.device.Device;
import com.example.hello_there.device.DeviceRepository;
import com.example.hello_there.house.House;
import com.example.hello_there.house.HouseRepository;
import com.example.hello_there.board.Board;
import com.example.hello_there.board.BoardRepository;
import com.example.hello_there.comment.Comment;
import com.example.hello_there.comment.CommentRepository;
import com.example.hello_there.chat_room.ChatRoom;
import com.example.hello_there.chat_room.ChatRoomRepository;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponseStatus;
import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.login.jwt.TokenRepository;
import com.example.hello_there.text_message.TextMessage;
import com.example.hello_there.text_message.TextMessageRepository;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.example.hello_there.exception.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class UtilService {
    public static final int SEC = 60;
    public static final int MIN = 60;
    public static final int HOUR = 24;
    public static final int DAY = 30;
    public static final int MONTH = 12;

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final TokenRepository tokenRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final HouseRepository houseRepository;
    private final TextMessageRepository textMessageRepository;
    private final DeviceRepository deviceRepository;

    public User findByUserIdWithValidation(Long userId) throws BaseException {
        return userRepository.findUserById(userId)
                .orElseThrow(() -> new BaseException(NONE_EXIST_USER));
    }

    public User findByEmailWithValidation(String email) throws BaseException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(POST_USERS_NONE_EXISTS_EMAIL));
    }

    public House findByHouseIdWithValidation(Long houseId) throws BaseException {
        House house = houseRepository.findHouseById(houseId).orElse(null);
        if(house == null) throw new BaseException(BaseResponseStatus.POST_USERS_NONE_EXISTS_HOUSE);
        return house;
    }

    public Board findByBoardIdWithValidation(Long boardId) throws BaseException {
        return boardRepository.findBoardById(boardId)
                .orElseThrow(() -> new BaseException(NONE_EXIST_BOARD));
    }

    public Comment findByCommentIdWithValidation(Long commentId) throws BaseException {
        return commentRepository.findById(commentId)
                .orElseThrow(()-> new BaseException(NONE_EXIST_COMMENT));
    }

    public Token findTokenByUserIdWithValidation(Long userId) throws BaseException {
        return tokenRepository.findTokenByUserId(userId)
                .orElseThrow(() -> new BaseException(INVALID_USER_JWT));
    }

    public ChatRoom findChatRoomByChatRoomIdWithValidation(String chatRoomId) throws BaseException {
        return chatRoomRepository.findChatRoomById(chatRoomId)
                .orElseThrow(() -> new BaseException(NONE_EXIST_ROOM));
    }

    public TextMessage findByTextMessageIdWithValidation(Long textMessageId) throws BaseException{
        return textMessageRepository.findByMessageId(textMessageId)
                .orElseThrow(() -> new BaseException(NONE_EXIST_MESSAGE));
    }

    public Device findDeviceByUserIdWithValidation(Long userId) throws BaseException {
        return deviceRepository.findDeviceByUserId(userId)
                .orElseThrow(() -> new BaseException(NONE_EXIST_DEVICE));
    }

    public static String convertLocalDateTimeToLocalDate(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    public static String convertLocalDateTimeToTime(LocalDateTime localDateTime) {
        LocalDateTime now = LocalDateTime.now();

        long diffTime = localDateTime.until(now, ChronoUnit.SECONDS); // now보다 이후면 +, 전이면 -

        if (diffTime < SEC){
            return diffTime + "초 전";
        }
        diffTime = diffTime / SEC;
        if (diffTime < MIN) {
            return diffTime + "분 전";
        }
        diffTime = diffTime / MIN;
        if (diffTime < HOUR) {
            return diffTime + "시간 전";
        }
        diffTime = diffTime / HOUR;
        if (diffTime < DAY) {
            return diffTime + "일 전";
        }
        diffTime = diffTime / DAY;
        if (diffTime < MONTH) {
            return diffTime + "개월 전";
        }
        diffTime = diffTime / MONTH;
        return diffTime + "년 전";
    }

    public static String formatTime(LocalTime time) {
        int hour = time.getHour();
        int min = time.getMinute();
        String meridiem = (hour >= 12) ? "오후" : "오전";
        if (hour >= 12) {
            hour -= 12;
        }
        return meridiem + " " + hour + ":" + String.format("%02d", min);
    }
}
