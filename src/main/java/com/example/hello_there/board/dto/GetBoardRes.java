package com.example.hello_there.board.dto;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.BoardType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetBoardRes {
    private Long boardId;
    private BoardType boardType;
    private String nickName;
    private String title;
    private String content;
}
