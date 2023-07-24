package com.example.hello_there.login.google;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.dto.JwtResponseDTO;
import com.example.hello_there.login.google.dto.GetGoogleUserRes;
import com.example.hello_there.login.jwt.JwtProvider;
import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.login.jwt.TokenRepository;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.user.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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

import static com.example.hello_there.exception.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleService {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String Google_Client_Id;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String Google_Client_Secret;

    private UserService userService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;

    /**
     * 구글 콜백 메서드
     */
    public BaseResponse<?> googleCallBack(String accessToken) throws BaseException {
        GetGoogleUserRes getGoogleUserRes = getUserInfo(accessToken);
        if(getGoogleUserRes == null) {
            throw new BaseException(FAIL_TO_GET_INFO);
        }
        String email = getGoogleUserRes.getEmail();
        String nickName = getGoogleUserRes.getNickName();
        User findUser = userRepository.findByEmail(email).orElse(null);
        Token token;
        JwtResponseDTO.TokenInfo tokenInfo;
        if (findUser == null) { // 회원 가입
            User googleUser = new User();
            googleUser.createUser(userService.generateUniqueNickName(nickName), email, null, null);
            userRepository.save(googleUser);
            tokenInfo = jwtProvider.generateToken(googleUser.getId());
            token = Token.builder()
                    .accessToken(tokenInfo.getAccessToken())
                    .refreshToken(tokenInfo.getRefreshToken())
                    .user(googleUser)
                    .build();
        } else { // 기존 회원 로그인
            tokenInfo = jwtProvider.generateToken(findUser.getId());
            token = Token.builder()
                    .refreshToken(tokenInfo.getRefreshToken())
                    .user(findUser)
                    .build();
        }
        tokenRepository.save(token);
        return new BaseResponse<>(tokenInfo);
    }

    /**
     * 액세스 토큰 발급받기
     */
    public String getAccessToken(String code){
        //HttpHeaders 생성00
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", Google_Client_Id);
        body.add("client_secret", Google_Client_Secret);
        body.add("redirect_uri" , "http://localhost:8080/oauth/google"); // 프로덕션 환경에 교체
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, httpHeaders);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        return responseEntity.getBody();
    }

    /**
     * 구글 유저의 정보 가져오기
     */
    public GetGoogleUserRes getUserInfo(String accessToken) throws BaseException {
        //요청하는 클라이언트마다 가진 정보가 다를 수 있기에 HashMap타입으로 선언
        HashMap<String, Object> googleUserInfo = new HashMap<>();
        String reqURL = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken;
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //요청에 필요한 Header에 포함될 내용
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line = "";
                String result = "";

                while ((line = br.readLine()) != null) {
                    result += line;
                }
                JsonElement element = JsonParser.parseString(result);
                String email = element.getAsJsonObject().get("email").getAsString();
                if(userRepository.findByEmailCount(email) >= 1) {
                    throw new BaseException(POST_USERS_EXISTS_EMAIL);
                }
                String name = element.getAsJsonObject().get("name").getAsString();
                return new GetGoogleUserRes(email, name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
