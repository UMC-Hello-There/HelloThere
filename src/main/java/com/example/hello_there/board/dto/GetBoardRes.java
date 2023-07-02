package com.example.hello_there.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetBoardRes {
    private Long boardId;
    private String nickName;
    private String title;
    private String content;
}
