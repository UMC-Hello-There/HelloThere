package com.example.hello_there.login.kakao;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.exception.BaseResponseStatus;
import com.example.hello_there.login.dto.AssertionDTO;
import com.example.hello_there.login.dto.JwtResponseDTO;
import com.example.hello_there.login.jwt.JwtProvider;
import com.example.hello_there.login.jwt.JwtService;
import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.login.jwt.TokenRepository;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.user.UserService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;
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

    @ResponseBody
    @PostMapping("/oauth/kakao")
    // public BaseResponse<?> kakaoCallback(String code) { // 실제 프로덕션 환경에서는 주석을 풀어야 함.
    public BaseResponse<?> kakaoCallback(@RequestParam("accToken") String accessToken) {
//        String accessToken = googleService.getAccessToken(code);
//        Gson gsonObj = new Gson();
//        Map<?, ?> data = gsonObj.fromJson(accessToken, Map.class);
//        String atoken = (String) data.get("access_token");
        try {
            String userEmail = kaKaoLoginService.getUserEmail(accessToken);
            Optional<User> findUser = userRepository.findByEmail(userEmail);
            if (!findUser.isPresent()) { // 회원가입인 경우
                User kakaoUser = kaKaoLoginService.getUserInfo(accessToken);
                userRepository.save(kakaoUser);
                JwtResponseDTO.TokenInfo tokenInfo = jwtProvider.generateToken(kakaoUser.getId());
                Token token = new Token();
                token.updateAccessToken(tokenInfo.getAccessToken());
                token.updateRefreshToken(tokenInfo.getRefreshToken());
                token.updateUser(kakaoUser);
                tokenRepository.save(token);
                if(kakaoUser.getEmail() == "" | kakaoUser.getNickName() == "") {
                    String message = "사용자 정보 제공에 동의하지 않아 기본 값으로 로그인 되었습니다. 마이페이지에서 본인의 정보를 알맞게 수정 후 이용해주세요.";
                    AssertionDTO assertionDTO = new AssertionDTO(tokenInfo, message);
                    return new BaseResponse<>(assertionDTO); // 이걸 예외처리하면 너무 복잡해질 거 같아, 그냥 기본값으로 세팅하고 로그인 처리하였다.
                }
                return new BaseResponse<>(tokenInfo);
            }
            else { // 기존 회원이 로그인하는 경우
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
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    // 카카오 소셜 로그아웃
    // 하지만 실제로 사용할 일은 없다. 카카오에서 받은 접근 토큰과 재발급 토큰은 모두 우리의 방식으로 다시 generate 하였기 때문에
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