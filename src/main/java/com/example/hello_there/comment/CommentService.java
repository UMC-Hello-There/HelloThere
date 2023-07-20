package com.example.hello_there.comment;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.BoardRepository;
import com.example.hello_there.comment.dto.*;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.hello_there.exception.BaseResponseStatus.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final LikeCommentRepository likeCommentRepository;
    private final UtilService utilService;


    /**
     * 댓글 생성
     */
    @Transactional
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
     * 댓글 전체 조회
     **/
    public List<GetCommentRes> findComments(Long boardId,Long userId) {
        // 회원이 좋아요를 누른 댓글의 PK 리스트
        List<Long> likes = likeCommentRepository.findByUserIdAndBoardId(boardId, userId);

        // 게시물에 달린 댓글 리스트
        List<GetCommentRes> comments = commentRepository.findCommentsByBoardIdForList(boardId)
                .stream()
                .map(GetCommentRes::new)
                .collect(Collectors.toList());

        // 댓글 리스트 중 회원이 좋아요를 누른 댓글은 GetCommentRes 의 likeStatus 를 true 로 변경
        comments.forEach(getCommentRes -> {
                    if(likes.contains(getCommentRes.getCommentId())){
                        getCommentRes.changeLikeStatus(true);
                    }
                });

        return comments;
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public PatchCommentRes updateComment(Long requestUserId, Long commentId, PatchCommentReq patchCommentReq, Long boardId) throws BaseException {
        // 게시판 검증
        utilService.findByBoardIdWithValidation(boardId);

        // 댓글 검증
        Comment updateRequestComment = utilService.findByCommentIdWithValidation(commentId);

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

    /**
     * 댓글 삭제
     */
    @Transactional
    public Long deleteComment(Long requestUserId, Long boardId, Long commentId) throws BaseException {
        // 게시판 검증
        utilService.findByBoardIdWithValidation(boardId);

        // 댓글 검증
        Comment deleteRequestComment = utilService.findByCommentIdWithValidation(commentId);

        // 댓글 원작자 회원 ID
        Long originUserId = deleteRequestComment.getUser().getId();

        // 댓글 원작자 검증
        isOriginalWriter(requestUserId, originUserId);

        // 부모 댓글인 경우
        if (deleteRequestComment.getParent() == null) {
            deleteParentComment(deleteRequestComment, commentId);
        } else {
            Comment parentComment = deleteRequestComment.getParent();
            deleteChildComment(parentComment, commentId);
        }

        return commentId;
    }

    /**
     * 댓글 좋아요 생성/취소
     */
    @Transactional
    public Long switchLikeComment(Long userId, Long boardId, Long commentId) throws BaseException {
        // 게시판 검증
        utilService.findByBoardIdWithValidation(boardId);
        // 댓글 검증
        Comment comment = utilService.findByCommentIdWithValidation(commentId);
        // 회원 검증
        User user = utilService.findByUserIdWithValidation(userId);

        if (likeCommentRepository.existsByUserAndComment(user, comment)) {
            return likeCommentRepository.deleteByUserAndComment(user, comment);
        } else {
            return likeCommentRepository
                    .save(new LikeComment(user, comment))
                    .getId();
        }
    }

    // 댓글 원작자 검증
    private void isOriginalWriter(Long requestUserId, Long originUserId) throws BaseException {
        if (!requestUserId.equals(originUserId))
            throw new BaseException(INVALID_DELETE_REQUEST);
    }

    // 부모 댓글 삭제
    private void deleteParentComment(Comment deleteRequestComment, Long commentId) {
        // 남아있는 자식 댓글이 있는 경우 DB 에서 바로 삭제하지않고 내용만 변경
        if (existsChildComment(deleteRequestComment)) {
            deleteRequestComment.updateComment("작성자에 의해 삭제된 댓글입니다.");
            deleteRequestComment.changeIsDeleted();
            return;
        }
        // 자식 댓글이 없으면 DB 에서 바로 삭제
        commentRepository.deleteById(commentId);
    }

    // 자식 댓글 삭제
    private void deleteChildComment(Comment parentComment, Long commentId) {
        // 자식 댓글은 DB 에서 바로 삭제
        commentRepository.deleteById(commentId);
        // 남아있는 자식댓글이 없고 부모 댓글이 삭제상태인 경우 부모 댓글 DB 에서 삭제
        if (notExistsChildComment(parentComment) && parentComment.isDeleted())
            commentRepository.deleteById(parentComment.getCommentId());
    }

    // 자식 댓글이 남아있는지 검증
    private Boolean existsChildComment(Comment deleteRequestComment) {
        return commentRepository.existsByParentCommentId(deleteRequestComment.getCommentId());
    }

    // 자식 댓글이 없는지 검증
    private Boolean notExistsChildComment(Comment parentComment) {
        return !commentRepository.existsByParentCommentId(parentComment.getCommentId());
    }
}