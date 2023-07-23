package com.example.hello_there.board.dto;

import com.example.hello_there.board.BoardType;
import com.example.hello_there.board.photo.dto.GetS3Res;
import com.example.hello_there.comment.dto.GetCommentByBoardRes;
import com.example.hello_there.comment.dto.GetCommentRes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetBoardDetailRes {
    private Long boardId;
    private BoardType boardType;
    private String createDate; // ex) 2023-07-04
    private String createTime; // ex) 3분 전
    private String nickName;
    private GetS3Res profile; // 작성자 프로필 이미지
    private String title;
    private String content;
    private Long view;
    private Long CommentCount; // 댓글 수
    private Long LikeCount; // 좋아요 수
    private List<GetS3Res> getS3Res;
    private List<GetCommentRes> getCommentRes;
}