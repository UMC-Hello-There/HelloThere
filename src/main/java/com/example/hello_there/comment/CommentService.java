package com.example.hello_there.comment;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.BoardRepository;
import com.example.hello_there.comment.dto.*;
import com.example.hello_there.comment.likecomment.LikeComment;
import com.example.hello_there.comment.likecomment.LikeCommentRepository;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.report.Report;
import com.example.hello_there.report.ReportRepository;
import com.example.hello_there.report.ReportService;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.user.UserStatus;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.hello_there.exception.BaseResponseStatus.*;
import static com.example.hello_there.report.ReportCount.ADD_REPORT_FOR_COMMENT;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final LikeCommentRepository likeCommentRepository;
    private final ReportRepository reportRepository;
    private final ReportService reportService;
    private final UtilService utilService;


    /**
     * 댓글 생성
     */
    @Transactional
    public PostCommentRes addComment(Long boardId, Long userId, Long parentId,PostCommentReq postCommentReq) throws BaseException {
        // comment 에대한 black 유저 검증
        reportService.checkBlackUser("comment", userId);

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
        if (parentId == null) {
            comment.addGroupId(groupId + 1L);
            Comment savedParentComment = commentRepository.save(comment);
            // 부모 댓글 저장
            return new PostCommentRes(savedParentComment);
        } else {
            parentComment = commentRepository.findById(parentId)
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
        List<Long> likes = likeCommentRepository.findByUserIdAndBoardId(userId, boardId);

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

        // 댓글 원작자 회원 ID
        Long originUserId = updateRequestComment.getUser().getId();

        // 수정 요청한 댓글이 자신의 댓글이 맞는지 검증
        isOriginalWriter(requestUserId,originUserId);

        // 댓글 수정
        updateRequestComment.updateComment(patchCommentReq.getContent());

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

    /**
     * 댓글 신고
     */
    @Transactional
    public String reportComment(Long reporterId, Long boardId, Long commentId, String reason) throws BaseException {
        // 게시글 검증
        utilService.findByBoardIdWithValidation(boardId);
        // 댓글 검증
        Comment comment = utilService.findByCommentIdWithValidation(commentId);
        // 신고 당한 회원
        User reported = comment.getUser();
        // 신고자
        User reporter = utilService.findByUserIdWithValidation(reporterId);
        Report report = new Report();

        // 한 명의 유저가 중복으로 신고할 수 없도록 예외를 호출
        reportService.isDuplicateReport(reporterId,reported.getId(),0L,commentId,0L);

        // 자기 자신을 신고할 수 없도록 예외를 호출
        reportService.isSelfReport(reported.getId(),reporterId);


        // 댓글 신고
        reportRepository.save(report.createReport(reason, null, commentId, null, reporter, reported));

        // 게시글 누적 신고 횟수에 따른 처리
        reportService.updateReport(ADD_REPORT_FOR_COMMENT, reported);

        int cumulativeReportCount = reportService.findCumulativeReportCount(reported,3);

        LocalDateTime now = LocalDateTime.now(); // 현재 시간
        String prefix = "comment";
        switch (cumulativeReportCount) {
            case 4 -> // 누적 신고 횟수 4
                    reportService.setReportExpiration(prefix,reported, now.plus(3, ChronoUnit.DAYS), UNABLE_TO_COMMENT_THREE.name());
            case 8 -> // 누적 신고 횟수 8
                    reportService.setReportExpiration(prefix,reported, now.plus(5, ChronoUnit.DAYS), UNABLE_TO_COMMENT_FIVE.name());
            case 12 -> // 누적 신고 횟수 12
                    reportService.setReportExpiration(prefix,reported, now.plus(7, ChronoUnit.DAYS), UNABLE_TO_COMMENT_SEVEN.name());
            case 16 -> // 누적 신고 횟수 16
                    reportService.setReportExpiration(prefix,reported, now.plus(14, ChronoUnit.DAYS), UNABLE_TO_COMMENT_FOURTEEN.name());
            case 20 -> // 누적 신고 횟수 20
                    reportService.setReportExpiration(prefix,reported, now.plus(30, ChronoUnit.DAYS), UNABLE_TO_COMMENT_MONTH.name());
            case 21 -> // 누적 신고 횟수 21
                    reported.setStatus(UserStatus.INACTIVE); // 영구 정지
        }
        return "댓글 작성자에 대한 신고 처리가 완료되었습니다.";
    }

    // 댓글 원작자 검증
    private void isOriginalWriter(Long requestUserId, Long originUserId) throws BaseException {
        if (!requestUserId.equals(originUserId))
            throw new BaseException(INVALID_UPDATE_DELETE_REQUEST);
    }

    // 부모 댓글 삭제
    private void deleteParentComment(Comment deleteRequestComment, Long commentId) {
        // 남아있는 자식 댓글이 있는 경우 DB 에서 바로 삭제하지않고 내용만 변경
        if (existsChildComment(deleteRequestComment)) {
            deleteRequestComment.updateComment("작성자에 의해 삭제된 댓글입니다.");
            deleteRequestComment.changeIsDeleted();
            //부모 댓글에 눌린 좋아요 모두 삭제
            likeCommentRepository.deleteByCommentId(deleteRequestComment.getCommentId());
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
        if (!existsChildComment(parentComment) && parentComment.isDeleted())
            commentRepository.deleteById(parentComment.getCommentId());
    }

    // 자식 댓글이 남아있는지 검증
    private Boolean existsChildComment(Comment deleteRequestComment) {
        return commentRepository.existsByParentCommentId(deleteRequestComment.getCommentId());
    }
}