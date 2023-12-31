package com.example.hello_there.login.google;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GoogleController {
    private final GoogleService googleService;

    /**
     * 구글 소셜로그인
     */
    @ResponseBody
    @GetMapping("/oauth/google")
    public BaseResponse<?> googleCallback(@RequestParam String token) {
        try {
            return googleService.googleCallBack(token);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
