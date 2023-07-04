package com.example.hello_there.login.google;

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
    public BaseResponse<?> GoogleCallback(@RequestParam("accessToken") String accessToken) {
        try {
            Gson gsonObj = new Gson();
            String userEmail = googleService.getUserEmail(accessToken);
            String userName = googleService.getUserName(accessToken);
            User findUser = userRepository.findByEmail(userEmail).orElse(null);
            if (findUser == null) { // 회원 가입
                User googleUser = new User();
                googleUser.createUser(userEmail, null, userName, true, null);
                userRepository.save(googleUser);
                JwtResponseDTO.TokenInfo tokenInfo = jwtProvider.generateToken(googleUser.getId());
                Token token = new Token();
                token.updateAccessToken(tokenInfo.getAccessToken());
                token.updateRefreshToken(tokenInfo.getRefreshToken());
                token.updateUser(googleUser);
                tokenRepository.save(token);
                String message = "구글 정책 변경으로 인해 제공 받지 못한 정보에 대해 기본 값으로 가입되었습니다." +
                        " 마이페이지에서 본인의 정보를 알맞게 수정 후 이용해주세요.";
                AssertionDTO assertionDTO = new AssertionDTO(tokenInfo, message);
                return new BaseResponse<>(assertionDTO);
            } else {
                JwtResponseDTO.TokenInfo tokenInfo = jwtProvider.generateToken(findUser.getId());
                Token token = new Token();
                token.updateRefreshToken(tokenInfo.getRefreshToken());
                token.updateUser(findUser);
                tokenRepository.save(token);

                return new BaseResponse<>(tokenInfo);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
