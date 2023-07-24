package com.example.hello_there.login.naver;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.dto.AssertionDTO;
import com.example.hello_there.login.dto.JwtResponseDTO;
import com.example.hello_there.login.google.dto.GetGoogleUserRes;
import com.example.hello_there.login.jwt.JwtProvider;
import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.login.jwt.TokenRepository;
import com.example.hello_there.login.kakao.dto.GetKakaoUserRes;
import com.example.hello_there.login.naver.dto.GetNaverUserRes;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.user.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.example.hello_there.exception.BaseResponseStatus.POST_USERS_EXISTS_EMAIL;

@Service
@RequiredArgsConstructor
public class NaverService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;

    /**
     * 네이버 콜백 메서드
     */
    public BaseResponse<?> naverCallBack(String accessToken) throws BaseException {
        GetNaverUserRes getNaverUserRes = getUserInfo(accessToken);
        String email = getNaverUserRes.getEmail();
        String nickName = getNaverUserRes.getNickName();
        Optional<User> findUser = userRepository.findByEmail(email);
        Token token;
        JwtResponseDTO.TokenInfo tokenInfo;
        if (!findUser.isPresent()) { // 회원가입인 경우
            User kakaoUser = new User();
            kakaoUser.createUser(userService.generateUniqueNickName(nickName), email, null, null);
            userRepository.save(kakaoUser);
            tokenInfo = jwtProvider.generateToken(kakaoUser.getId());
            token = Token.builder()
                    .accessToken(tokenInfo.getAccessToken())
                    .refreshToken(tokenInfo.getRefreshToken())
                    .user(kakaoUser)
                    .build();
        }
        else { // 기존 회원이 로그인하는 경우
            User user = findUser.get();
            tokenInfo = jwtProvider.generateToken(user.getId());
            token = Token.builder()
                    .accessToken(tokenInfo.getAccessToken())
                    .refreshToken(tokenInfo.getRefreshToken())
                    .user(user)
                    .build();
        }
        tokenRepository.save(token);
        return new BaseResponse<>(tokenInfo);
    }

    /**
     * 네이버 유저의 정보 가져오기
     */
    public GetNaverUserRes getUserInfo(String accessToken) throws BaseException {
        // HttpHeader 생성
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + accessToken);
        httpHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpHeader와 HttpBody를 하나의 객체에 담기
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        // Http 요청을 GET 방식으로 실행하여 멤버 정보를 가져옴
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        // 네이버 인증 서버가 반환한 사용자 정보
        String userInfo = responseEntity.getBody();

        // JSON 데이터에서 필요한 정보 추출
        Gson gsonObj = new Gson();
        Map<?, ?> data = gsonObj.fromJson(userInfo, Map.class);
        // 유저의 이메일 정보 가져오기
        String email = (String) ((Map<?, ?>) (data.get("response"))).get("email");
        if(userRepository.findByEmailCount(email) >= 1 && email != "") {
            throw new BaseException(POST_USERS_EXISTS_EMAIL);
        }
        // 유저의 닉네임 정보 가져오기
        String nickName = (String) ((Map<?, ?>) (data.get("response"))).get("nickname");
        return new GetNaverUserRes(email, nickName);
    }
}
