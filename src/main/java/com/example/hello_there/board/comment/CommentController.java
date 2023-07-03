package com.example.hello_there.board.comment;

import com.example.hello_there.board.comment.dto.*;
import com.example.hello_there.board.dto.DeleteBoardReq;
import com.example.hello_there.board.dto.PatchBoardReq;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final UtilService utilService;
    private final JwtService jwtService;

    /** 댓글 작성하기 **/
    @PostMapping("")
    public BaseResponse<PostCommentRes> addComment(@RequestBody @Validated PostCommentReq postCommentReq) {
        try{
            Long memberId = jwtService.getUserIdx();
            return new BaseResponse<>(commentService.addComment(memberId, postCommentReq));
        }
        catch(BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
    /** 멤버의 댓글 기록 조회 **/
    @GetMapping("/list-up/member/{user-id}")
    public BaseResponse<List<GetCommentRes>> getComments(@PathVariable(name = "user-id") Long userId) {
        try{
            return new BaseResponse<>(commentService.getComments(userId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
    /** 게시글에 달린 댓글 전체 조회 **/
    @GetMapping("/list-up/board/{board-id}")
    public BaseResponse<List<GetCommentByBoardRes>> getCommentsByBoardId(@PathVariable(name = "board-id") Long boardId) {
        try{
            return new BaseResponse<>(commentService.getCommentsByBoard(boardId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 댓글을 Id로 삭제하기 **/
    @DeleteMapping("/delete/{comment-id}")
    public BaseResponse<String> deleteBoard(@PathVariable(name = "comment-id") Long commentId){
        try{
            Long userId = jwtService.getUserIdx();
            DeleteCommentReq deleteCommentReq = new DeleteCommentReq(userId, commentId);
            return new BaseResponse<>(commentService.deleteComment(deleteCommentReq));
        } catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 댓글 수정하기 **/
    @PatchMapping("/modify")
    public BaseResponse<String> modifyComment(@Validated @RequestBody PatchCommentReq patchCommentReq) {
        try {
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(commentService.modifyComment(userId, patchCommentReq));
        }
        catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
