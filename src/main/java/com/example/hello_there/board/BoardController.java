package com.example.hello_there.board;

import com.example.hello_there.board.dto.PostBoardReq;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.jwt.JwtProvider;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BoardController {
    // 생성자 주입 방법을 통해 의존성 주입
    private final BoardService boardService;
    private final JwtService jwtService;
    private final JwtProvider jwtProvider;
    private final UtilService utilService;

    /** 게시글 생성하기 **/
    @PostMapping("/board")
    public BaseResponse<String> createBoard(@RequestPart(value = "image", required = false) List<MultipartFile> multipartFiles,
                                            @RequestPart(value = "title") @Validated String title,
                                            @RequestPart(value = "content") @Validated String content) {
        try {
            Long memberId = jwtService.getUserIdx();
            PostBoardReq postBoardReq = new PostBoardReq(BoardType.SHARE_BOARD, title, content);
            return new BaseResponse<>(boardService.createBoard(memberId, postBoardReq, multipartFiles));
        }
        catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

//    /** 게시글을 Id로 조회하기 **/
//    @GetMapping("/board/{board-id}")
//    public BaseResponse<GetBoardRes> getBoard(@PathVariable(name = "board-id") Long boardId) {
//        try{
//            Board board = boardService.getBoard(boardId);
//            Member member = board.getMember();
//            GetBoardRes getBoardRes = new GetBoardRes(boardId, member.getNickName(), board.getTitle(), board.getContent());
//            return new BaseResponse<>(getBoardRes);
//        } catch(BaseException exception){
//            return new BaseResponse<>(exception.getStatus());
//        }
//    }
//
//    /** 게시글을 Id로 삭제하기 **/
//    @DeleteMapping("/delete/{board-id}")
//    public BaseResponse<String> deleteBoard(@PathVariable(name = "board-id") Long boardId){
//        try{
//            Long memberId = jwtService.getMemberIdx();
//            DeleteBoardReq deleteBoardReq = new DeleteBoardReq(memberId, boardId);
//            return new BaseResponse<>(boardService.deleteBoard(deleteBoardReq));
//        } catch(BaseException exception){
//            return new BaseResponse<>(exception.getStatus());
//        }
//    }
//
//    /** 게시글 수정하기 **/
//    @PatchMapping("/modify")
//    public BaseResponse<String> modifyBoard(@RequestParam Long boardId,
//                                            @RequestPart(value = "image", required = false) List<MultipartFile> multipartFiles,
//                                            @RequestPart(value = "title") @Validated String title,
//                                            @RequestPart(value = "content") @Validated String content) {
//        try {
//            Long memberId = jwtService.getMemberIdx();
//            PatchBoardReq patchBoardReq = new PatchBoardReq(title, content);
//            return new BaseResponse<>(boardService.modifyBoard(memberId, boardId, patchBoardReq, multipartFiles));
//        }
//        catch (BaseException exception) {
//            return new BaseResponse<>(exception.getStatus());
//        }
//    }
}