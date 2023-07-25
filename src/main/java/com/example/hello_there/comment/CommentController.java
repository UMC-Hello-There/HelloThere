package com.example.hello_there.comment;

import com.example.hello_there.comment.dto.*;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/boards/{boardId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final UtilService utilService;
    private final JwtService jwtService;

    /** 댓글 생성 **/
    @PostMapping
    public BaseResponse<PostCommentRes> addComment(
            @RequestBody @Valid PostCommentReq postCommentReq,
            @RequestParam(required = false) Long parentId,
            @PathVariable Long boardId) {
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(commentService.addComment(boardId, userId, parentId, postCommentReq));
        }
        catch(BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 댓글 전체 조회 **/
    @GetMapping("/{userId}")
    public List<GetCommentRes> getCommentsByBoardId(
            @PathVariable Long boardId,
            @PathVariable Long userId) {
        return commentService.findComments(boardId,userId); // 단독으로 호츨될 일은 없다고 가정
    }

    /** 댓글 수정 **/
    @PatchMapping("/{commentId}")
    public BaseResponse<PatchCommentRes> updateComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @RequestBody @Valid PatchCommentReq patchCommentReq){
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(commentService
                    .updateComment(userId,commentId,patchCommentReq,boardId));
        }catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 댓글 삭제 **/
    @DeleteMapping("/{commentId}")
    public BaseResponse<Long> deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId
    ){
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(commentService.deleteComment(userId, boardId,commentId));
        }catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 댓글 좋아요 생성/취소**/
    @PostMapping("/{commentId}")
    public BaseResponse<Long> switchLikeComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId
    ){
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(commentService.switchLikeComment(userId,boardId,commentId));
        }catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 댓글 작성자 신고 **/
    @PostMapping("/report/{commentId}")
    public BaseResponse<String> reportWriter(
            @PathVariable Long commentId,
            @PathVariable Long boardId,
            @RequestParam(required = false) String reason){
        try {
            Long reporterId = jwtService.getUserIdx();
            return new BaseResponse<>(commentService.reportComment(reporterId, boardId, commentId, reason));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
