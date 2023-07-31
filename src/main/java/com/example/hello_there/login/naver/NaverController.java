package com.example.hello_there.login.naver;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NaverController {

    private final NaverService naverService;

    /**
     * 네이버 소셜로그인
     */
    @ResponseBody
    @PostMapping("/oauth/naver")
    public BaseResponse<?> naverCallback(@RequestParam("token") String accessToken) {
        try {
            return naverService.naverCallBack(accessToken);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
