package com.example.hello_there.comment.dto;

import com.example.hello_there.board.BoardType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetCommentByBoardRes {
    private String nickName;
    private String reply;
    private LocalDateTime createDate;
}