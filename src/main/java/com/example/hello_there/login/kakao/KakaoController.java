package com.example.hello_there.login.kakao;

import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.exception.BaseResponseStatus;
import com.example.hello_there.login.dto.JwtResponseDTO;
import com.example.hello_there.login.jwt.JwtProvider;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.login.jwt.TokenRepository;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static com.example.hello_there.exception.BaseResponseStatus.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kaKaoLoginService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;
    private final JwtService jwtService;

    //카카오 로그인 코드, 카카오 디벨로퍼스에서 성별이랑 생일 추가해야함.
    @ResponseBody
    @PostMapping("/oauth/kakao")
    public BaseResponse<?> kakaoCallback(@RequestParam("accToken") String accessToken) {
        String userEmail = kaKaoLoginService.getUserEmail(accessToken);
        String userNickName = kaKaoLoginService.getUserNickname(accessToken);
        Optional<User> findUser = userRepository.findByEmail(userEmail);
        if (!findUser.isPresent()) {
            User kakaoUser = new User();
            kakaoUser.updateEmail(userEmail);
            kakaoUser.updateNickName(userNickName);
            // kakaoUser.updateIsSocialLogin();
            JwtResponseDTO.TokenInfo tokenInfo = jwtProvider.generateToken(kakaoUser.getId());
            Token token = new Token();
            token.updateAccessToken(tokenInfo.getAccessToken());
            token.updateRefreshToken(tokenInfo.getRefreshToken());
            token.updateUser(kakaoUser);
            tokenRepository.save(token);
            userRepository.save(kakaoUser);
            return new BaseResponse<>(tokenInfo);
        }

        else {
            User user = findUser.get();
            JwtResponseDTO.TokenInfo tokenInfo = jwtProvider.generateToken(user.getId());
            Token token = Token.builder()
                    .accessToken(tokenInfo.getAccessToken())
                    .refreshToken(tokenInfo.getRefreshToken())
                    .user(user)
                    .build();
            tokenRepository.save(token);
            return new BaseResponse<>(tokenInfo);
        }
    }

    // 카카오 소셜 로그아웃
    // 하지만 실제로 사용할 일은 없다. 카카오에서 받은 접근 토큰과 재발급 토큰은 모두 우리의 방식으로 다시 generate하였기 때문에
    // 카카오에서 이를 해석할 수 없다. 따라서 소셜로그인의 경우에도 Member Controller의 로그아웃 API를 사용해야 한다.
    @PostMapping("/oauth/kakao-logout")
    @ResponseBody
    public BaseResponse<?> kakaoLogout()
    {
        try{
            String accessToken = jwtService.getJwt();
            String result = userService.socialLogout(accessToken);
            return new BaseResponse<>(result);
        } catch(Exception e){
            return new BaseResponse<>(KAKAO_ERROR);
        }
    }
}