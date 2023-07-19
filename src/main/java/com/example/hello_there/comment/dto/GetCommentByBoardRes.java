package com.example.hello_there.comment.dto;

import com.example.hello_there.board.BoardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetCommentByBoardRes {
    private String nickName;
    private String imgUrl;
    private String fileName;
    private String reply;
    private String createDateTime;
}