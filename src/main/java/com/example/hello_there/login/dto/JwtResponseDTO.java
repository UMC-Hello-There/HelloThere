package com.example.hello_there.login.dto;

import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.user.User;
import com.example.hello_there.user.dto.PostUserRes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

public class JwtResponseDTO {
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenInfo {
        private String accessToken;
        private String refreshToken;
    }
}
