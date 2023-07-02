package com.example.hello_there.board.dto;

import com.example.hello_there.board.BoardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Validated
public class PostBoardReq {
    // build.gradle의 dependencies에
    // implementation 'org.springframework.boot:spring-boot-starter-validation'
    // 추가해야 Not Blank와 Size사용 가능
    private BoardType boardType;
    @NotBlank(message = "제목을 입력하세요.")
    @Size(min=2, max=20, message = "제목의 길이는 2~20글자까지 입력 가능합니다.")
    private String title;
    @NotBlank(message = "본문을 입력하세요.")
    @Size(min=2, max=100, message = "본문의 길이는 2~100글자까지 입력 가능합니다.")
    private String content;
}