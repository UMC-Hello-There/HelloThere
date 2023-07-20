package com.example.hello_there.user.dto;

import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter // 해당 클래스에 대한 접근자 생성
@Setter // 해당 클래스에 대한 설정자 생성
@AllArgsConstructor // 해당 클래스의 모든 멤버 변수(userIdx, jwt)를 받는 생성자를 생성
@NoArgsConstructor
public class PostLoginRes {
    private Long userId;
    private String accessToken;
    private String refreshToken;

    public PostLoginRes(User user, Token token) {
        this.userId = user.getId();
        this.accessToken = token.getAccessToken();
        this.refreshToken = token.getRefreshToken();
    }
}
