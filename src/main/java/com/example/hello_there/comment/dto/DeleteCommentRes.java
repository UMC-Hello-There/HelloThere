package com.example.hello_there.comment.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter // 해당 클래스에 대한 접근자 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeleteCommentRes {
    private Long commentId;

    public DeleteCommentRes(Long commentId){
        this.commentId = commentId;
    }
}

