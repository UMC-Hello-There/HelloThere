package com.example.hello_there.login.kakao;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static com.example.hello_there.exception.BaseResponseStatus.*;

@RestController
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kakaoService;
    private final JwtService jwtService;

    /**
     * 카카오 소셜로그인
     */
    @ResponseBody
    @PostMapping("/oauth/kakao")
    public BaseResponse<?> kakaoCallback(@RequestParam("token") String accessToken) {
        try {
            return kakaoService.kakaoCallBack(accessToken);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    // 카카오 소셜 로그아웃
    // 하지만 실제로 사용할 일은 없다. 카카오에서 받은 접근 토큰과 재발급 토큰은 모두 우리의 방식으로 다시 generate 하였기 때문에
    // 카카오에서 이를 해석할 수 없다. 따라서 소셜로그인의 경우에도 User Controller의 로그아웃 API를 사용해야 한다.
    @PostMapping("/oauth/kakao-logout")
    @ResponseBody
    public BaseResponse<?> kakaoLogout()
    {
        try{
            String accessToken = jwtService.getJwt();
            String result = kakaoService.socialLogout(accessToken);
            return new BaseResponse<>(result);
        } catch(Exception e){
            return new BaseResponse<>(KAKAO_ERROR);
        }
    }
}