package com.example.hello_there.notice.dto;

import com.example.hello_there.board.BoardType;
import com.example.hello_there.notice.Notice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.example.hello_there.utils.UtilService.convertLocalDateTimeToTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PostNoticeRes {
    private Long noticeId;
    private String title;
    private String body;
    private BoardType boardType;

    public PostNoticeRes(Notice notice, BoardType boardType) {
        this.noticeId = notice.getNoticeId();
        this.title = notice.getTitle();
        this.body = notice.getBody();
        this.boardType = boardType;
    }
}
