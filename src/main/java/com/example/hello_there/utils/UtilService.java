package com.example.hello_there.utils;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.BoardRepository;
import com.example.hello_there.board.comment.Comment;
import com.example.hello_there.board.comment.CommentRepository;
import com.example.hello_there.chat.ChatRoom;
import com.example.hello_there.chat.ChatRoomRepository;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponseStatus;
import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.login.jwt.TokenRepository;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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

    public User findByUserIdWithValidation(Long userId) throws BaseException {
        User user = userRepository.findUserById(userId).orElse(null);
        if(user == null) throw new BaseException(BaseResponseStatus.NONE_EXIST_MEMBER);
        return user;
    }

    public User findByEmailWithValidation(String email) throws BaseException {
        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null) throw new BaseException(BaseResponseStatus.POST_USERS_NONE_EXISTS_EMAIL);
        return user;
    }

    public Board findByBoardIdWithValidation(Long boardId) throws BaseException {
        Board board = boardRepository.findBoardById(boardId).orElse(null);
        if(board == null) throw new BaseException(BaseResponseStatus.NONE_EXIST_BOARD);
        return board;
    }

    public Comment findByCommentIdWithValidation(Long commentId) throws BaseException {
        Comment comment = commentRepository.findCommentById(commentId).orElse(null);
        if(comment == null) throw new BaseException(BaseResponseStatus.NONE_EXIST_COMMENT);
        return comment;
    }

    public Token findTokenByUserIdWithValidation(Long userId) throws BaseException {
        Token token = tokenRepository.findTokenByUserId(userId).orElse(null);
        if(token == null) throw new BaseException(BaseResponseStatus.INVALID_USER_JWT);
        return token;
    }

    public ChatRoom findChatRoomByChatRoomIdWithValidation(Long chatRoomId) throws BaseException {
        ChatRoom chatRoom = chatRoomRepository.findChatRoomById(chatRoomId).orElse(null);
        if(chatRoom == null) throw new BaseException(BaseResponseStatus.NONE_EXIST_ROOM);
        return chatRoom;
    }

    public static String convertLocalDateTimeToLocalDate(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    public static String convertLocalDateTimeToTime(LocalDateTime localDateTime) {
        LocalDateTime now = LocalDateTime.now();

        long diffTime = localDateTime.until(now, ChronoUnit.SECONDS); // now보다 이후면 +, 전이면 -

        if (diffTime < SEC){
            return diffTime + "초전";
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
}
