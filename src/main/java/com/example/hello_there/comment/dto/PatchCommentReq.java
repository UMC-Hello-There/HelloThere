package com.example.hello_there.comment.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Validated
public class PatchCommentReq {
    @NotBlank(message = "댓글을 입력하세요.")
    @Size(min = 1, max = 100, message = "댓글의 길이는 1~50글자까지 입력 가능합니다.")
    private String content;
}
