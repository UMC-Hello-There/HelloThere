package com.example.hello_there.board.dto;

import com.example.hello_there.board.BoardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetBoardEachOneRes {
    private Long boardId;
    private BoardType boardType;
    private String title;
}
