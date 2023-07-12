package com.example.hello_there.comment;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.BoardRepository;
import com.example.hello_there.board.BoardType;
import com.example.hello_there.comment.dto.*;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.hello_there.exception.BaseResponseStatus.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final UtilService utilService;

    @Transactional
    /** 댓글 작성하기 **/
    public PostCommentRes addComment(Long boardId, Long userId, PostCommentReq postCommentReq) throws BaseException {
        User user = utilService.findByUserIdWithValidation(userId);
        Board board = utilService.findByBoardIdWithValidation(boardId);
        Comment comment = postCommentReq.toEntity(postCommentReq, board, user);

        //groupId == null 이면 return 0L , groupId != null 이면 return max(groupId)
        Long groupId = commentRepository.findGroupIdByBoardId(boardId);
        Comment parentComment;

        /*
         * 댓글 생성 로직
         * parentId == null 부모댓글
         * parentId != null 자식댓글
         */
        if (postCommentReq.getParentId() == null) {
            comment.addGroupId(groupId + 1L);
            Comment savedParentComment = commentRepository.save(comment);
            // 부모 댓글 저장
            return new PostCommentRes(savedParentComment);
        } else {
            parentComment = commentRepository.findById(postCommentReq.getParentId())
                    .orElseThrow(() -> new BaseException(NONE_EXIST_PARENT_COMMENT));
            comment.addParentComment(parentComment);
            comment.addGroupId(parentComment.getGroupId());
        }
        Comment savedChildComment = commentRepository.save(comment);
        System.out.println(savedChildComment);
        // 자식 댓글 저장
        return new PostCommentRes(savedChildComment);
    }

    /**
     * 게시글에 달린 댓글 전체 조회
     **/
    public Page<GetCommentRes> findComments(Long boardId, Pageable pageable) throws BaseException {
        utilService.findByBoardIdWithValidation(boardId);
        return commentRepository
                .findCommentsByBoardIdForPage(boardId,pageable)
                .map(GetCommentRes::new);
    }
}