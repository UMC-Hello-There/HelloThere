package com.example.hello_there.utils;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.BoardRepository;
import com.example.hello_there.board.comment.Comment;
import com.example.hello_there.board.comment.CommentRepository;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponseStatus;
import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.login.jwt.TokenRepository;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UtilService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final TokenRepository tokenRepository;

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
}
