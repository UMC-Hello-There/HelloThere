package com.example.hello_there.comment;

import com.example.hello_there.comment.dto.*;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/boards/{boardId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final JwtService jwtService;

    /** 댓글 생성 **/
    @PostMapping
    public BaseResponse<PostCommentRes> addComment(
            @RequestBody @Valid PostCommentReq postCommentReq,
            @PathVariable Long boardId) {
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(commentService.addComment(boardId, userId, postCommentReq));
        }
        catch(BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 댓글 전체 조회 **/
    @GetMapping()
    public BaseResponse<Page<GetCommentRes>> commentList(
            @PathVariable(name = "boardId") Long boardId,
            Pageable pageable) {
        try{
            return new BaseResponse<>(commentService.findComments(boardId,pageable));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
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
    public BaseResponse<DeleteCommentRes> deleteComment(
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
}
