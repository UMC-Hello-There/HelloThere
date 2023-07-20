package com.example.hello_there.login.google;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.dto.AssertionDTO;
import com.example.hello_there.login.dto.JwtResponseDTO;
import com.example.hello_there.login.jwt.JwtProvider;
import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.login.jwt.TokenRepository;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.user.UserService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GoogleController {
    private final GoogleService googleService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;

    @ResponseBody
    @GetMapping("/oauth/google")
    public BaseResponse<?> googleCallback(@RequestParam String accessToken) { // 테스트 환경에서 사용해야 할 코드
        try {
            return googleService.googleCallBack(accessToken);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
