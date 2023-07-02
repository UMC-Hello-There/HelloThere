package com.example.hello_there.utils;

import com.example.hello_there.config.BaseException;
import com.example.hello_there.config.BaseResponseStatus;
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
}
