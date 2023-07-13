package com.example.hello_there.comment;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.BoardRepository;
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
                .findCommentsByBoardIdForPage(boardId, pageable)
                .map(GetCommentRes::new);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public PatchCommentRes updateComment(Long requestUserId, Long commentId, PatchCommentReq patchCommentReq, Long boardId) throws BaseException {
        // 게시판 존재 검증
        utilService.findByBoardIdWithValidation(boardId);

        // 수정 요청한 댓글 엔티티, 존재하지않으면 예외발생
        Comment updateRequestComment = commentRepository
                .findCommentByIdWithUser(commentId)
                .orElseThrow(() -> new BaseException(NONE_EXIST_COMMENT));

        // 조회한 댓글의 회원 ID
        Long originUserId = updateRequestComment.getUser().getId();

        // 수정 요청한 댓글이 자신의 댓글이 맞는지 검증
        if (requestUserId.equals(originUserId)) {
            // 작성자의 요청
            updateRequestComment.updateComment(patchCommentReq.getContent());
        } else {
            // 작성자가 아닌 요청으로 예외발생
            throw new BaseException(INVALID_UPDATE_REQUEST);
        }
        return new PatchCommentRes(updateRequestComment);
    }

    @Transactional
    public DeleteCommentRes deleteComment(Long requestUserId, Long boardId, Long commentId)throws BaseException {
        // 게시판 존재 검증
        utilService.findByBoardIdWithValidation(boardId);

        // 삭제 요청한 댓글 엔티티, 존재하지않으면 예외발생
        Comment deleteRequestComment = commentRepository
                .findCommentByIdWithUser(commentId)
                .orElseThrow(() -> new BaseException(NONE_EXIST_COMMENT));

        // 조회한 댓글의 회원 ID
        Long originUserId = deleteRequestComment.getUser().getId();

        // 수정 요청한 댓글이 자신의 댓글이 맞는지 검증
        if (requestUserId.equals(originUserId)) {
            // 작성자의 요청
            commentRepository.deleteById(commentId);
        } else {
            // 작성자가 아닌 요청으로 예외발생
            throw new BaseException(INVALID_DELETE_REQUEST);
        }

        return new DeleteCommentRes(commentId);
    }
}