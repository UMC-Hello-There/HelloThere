package com.example.hello_there.user.dto;

import com.example.hello_there.board.BoardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetNoticeRes {
    private Long noticeId;
    private String title;
    private String body;
    private BoardType boardType;
    private String createTime;
}
