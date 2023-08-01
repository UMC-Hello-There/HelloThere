package com.example.hello_there.notice;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.notice.dto.PostNoticeRes;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/notice")
public class NoticeController {
    private final NoticeService noticeService;

    @PostMapping("/comment/{commentId}")
    public BaseResponse<PostNoticeRes> sendCommentNotification(@PathVariable Long commentId) {
        try {
            return new BaseResponse<>(noticeService.sendCommentNotification(commentId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @PostMapping("/recomment/{commentId}")
    public BaseResponse<PostNoticeRes> sendRecommentNotification(@PathVariable Long commentId) {
        try {
            return new BaseResponse<>(noticeService.sendRecommentNotification(commentId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @PostMapping("/message/{messageId}")
    public BaseResponse<PostNoticeRes> sendMessageNotification(@PathVariable Long messageId) {
        try {
            return new BaseResponse<>(noticeService.sendMessageNotification(messageId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @PostMapping("/best-board/{boardId}")
    public BaseResponse<PostNoticeRes> sendBestBoardNotification(@PathVariable Long boardId) {
        try {
            return new BaseResponse<>(noticeService.sendBestBoardNotification(boardId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
