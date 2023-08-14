package com.example.hello_there.board;

import com.example.hello_there.board.dto.*;
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
@RequestMapping("/board")
public class BoardController {
    // 생성자 주입 방법을 통해 의존성 주입
    private final BoardService boardService;
    private final JwtService jwtService;

    /** 게시글 생성하기 **/
    @PostMapping
    public BaseResponse<String> createBoard(@RequestPart(value = "image", required = false) List<MultipartFile> multipartFiles,
                                            @Validated @RequestPart(value = "postBoardReq") PostBoardReq postBoardReq) {
        try {
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(boardService.createBoard(userId, postBoardReq, multipartFiles));
        }
        catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 게시글을 category로 조회하기(최신순) **/
    @GetMapping("/{category}")
    public BaseResponse<List<GetBoardRes>> getBoardsByCategory(@PathVariable BoardType category) {
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(boardService.getBoardsByCategory(userId, category));
        }catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }


     /** 게시글을 boardId로 조회하기 **/
    @GetMapping("/one/{boardId}")
    public BaseResponse<GetBoardDetailRes> getBoardByBoardId(@PathVariable Long boardId) {
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(boardService.getBoardByBoardId(userId, boardId));
        } catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 게시글을 멤버Id로 조회하기 **/
    @GetMapping
    public BaseResponse<List<GetBoardRes>> getBoardByUserId() {
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(boardService.getBoardById(userId));
        } catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 게시글 수정하기 **/
    @PatchMapping
    public BaseResponse<String> modifyBoard(@RequestPart(value = "image", required = false) List<MultipartFile> multipartFiles,
                                            @Validated @RequestPart(value = "patchBoardReq") PatchBoardReq patchBoardReq) {
        try {
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(boardService.modifyBoard(userId, patchBoardReq, multipartFiles));
        }
        catch (BaseException exception) {

            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 게시글 작성자 신고하기 **/
    @PatchMapping("/report/{boardId}")
    public BaseResponse<String> reportWriter(
            @PathVariable Long boardId,
            @RequestParam(required = false) String reason) {
        try {
            Long reporterId = jwtService.getUserIdx();
            return new BaseResponse<>(boardService.reportWriter(reporterId, boardId, reason));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 게시글 좋아요 및 좋아요 취소 **/
    @PostMapping("/{boardId}/like")
    public BaseResponse<String> likeOrUnlikeBoard(@PathVariable Long boardId){
        try{
            Long userId=jwtService.getUserIdx();
            return new BaseResponse<>(boardService.likeOrUnlikeBoard(userId, boardId));
        }
        catch (BaseException exception) {

            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 게시글을 Id로 삭제하기 **/
    @DeleteMapping("/{board-id}")
    public BaseResponse<String> deleteBoard(@PathVariable(name = "board-id") Long boardId){
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(boardService.deleteBoard(userId, boardId));
        } catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 게시글을 내용 or 제목으로 검색하기 **/
    @GetMapping("/search")
    public BaseResponse<List<GetBoardRes>> getBoardsByTitleOrContent(@RequestParam(name="keyword") String keyword) {
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(boardService.getBoardsByTitleOrContent(userId, keyword));
        }catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 게시글을 boardType별로 가장 최신글 하나씩 조회 **/
    @GetMapping("/new")
    public BaseResponse<List<GetBoardEachOneRes>> getBoardsByCategoryOne() {
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(boardService.getBoardsByCategoryOne(userId));
        }catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }


    /** 메인화면 인기 게시글 4개 조회 (좋아요 10개 이상, 최신순 정렬) **/
    @GetMapping("/hot/main")
    public BaseResponse<List<GetBoardEachOneRes>> getBoardsByLikeMain() {
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(boardService.getBoardsByLikeMain(userId));
        }catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 인기 게시글 전체 조회 (좋아요 10개 이상, 최신순 정렬) **/
    @GetMapping("/hot")
    public BaseResponse<List<GetBoardRes>> getBoardsByLike() {
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(boardService.getBoardsByLike(userId));
        }catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }


    /** category별 좋아요 top 게시글, 댓글 top 게시글 조회하기(최신순) **/
    @GetMapping("/{category}/top")
    public BaseResponse<List<GetTopBoardRes>> getTopBoardsByCategory(@PathVariable BoardType category) {
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(boardService.getTopBoardsByCategory(userId, category));
        }catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 오늘의 홈테리어 조회 (3개/메인화면) **/
    @GetMapping("/hometerrier/main")
    public BaseResponse<List<GetBoardMainRes>> getboardsHometerrier() {
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(boardService.getboardsHometerrier(userId));
        }catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /** 오늘의 홈테리어 조회 (3개/메인화면) **/
    @GetMapping("/market/main")
    public BaseResponse<List<GetBoardMainRes>> getboardsMarket() {
        try{
            Long userId = jwtService.getUserIdx();
            return new BaseResponse<>(boardService.getboardsMarket(userId));
        }catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

}