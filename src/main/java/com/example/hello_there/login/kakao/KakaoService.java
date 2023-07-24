package com.example.hello_there.login.kakao;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.login.dto.AssertionDTO;
import com.example.hello_there.login.dto.JwtResponseDTO;
import com.example.hello_there.login.jwt.JwtProvider;
import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.login.jwt.TokenRepository;
import com.example.hello_there.login.kakao.dto.GetKakaoUserRes;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.google.gson.Gson;
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

import java.util.Map;
import java.util.Optional;

import static com.example.hello_there.exception.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class KakaoService {
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String Kakao_Client_Id;

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;

    /**
     * 카카오 콜백 메서드
     */
    public BaseResponse<?> kakaoCallBack(String accessToken) throws BaseException {
        GetKakaoUserRes getKakaoUserRes = getUserInfo(accessToken);
        String email = getKakaoUserRes.getEmail();
        String nickName = getKakaoUserRes.getNickName();
        Optional<User> findUser = userRepository.findByEmail(email);
        if (!findUser.isPresent()) { // 회원가입인 경우
            User kakaoUser = new User();
            kakaoUser.createUser(nickName, email, null, null);
            userRepository.save(kakaoUser);
            JwtResponseDTO.TokenInfo tokenInfo = jwtProvider.generateToken(kakaoUser.getId());
            Token token = Token.builder()
                    .accessToken(tokenInfo.getAccessToken())
                    .refreshToken(tokenInfo.getRefreshToken())
                    .user(kakaoUser)
                    .build();
            tokenRepository.save(token);
            if(kakaoUser.getEmail() == "" || kakaoUser.getNickName() == "") {
                String message = "마이페이지에서 본인의 정보를 알맞게 수정 후 이용해주세요.";
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
    }

    /**
     * 액세스 토큰 발급받기
     * 프론트에서 액세스 토큰을 받아주지 않는 경우 사용
     */
    public String getAccessToken(String code){
        //HttpHeaders 생성00
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", Kakao_Client_Id);
        body.add("redirect_uri" , "http://localhost:8080/oauth/kakao");
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, httpHeaders);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        return responseEntity.getBody();
    }

    /**
     * 카카오 유저의 정보 가져오기
     */
    public GetKakaoUserRes getUserInfo(String accessToken) throws BaseException{
        // HttpHeader 생성
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + accessToken);
        httpHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpHeader와 HttpBody를 하나의 객체에 담기(body 정보는 생략 가능)
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        // RestTemplate를 이용하여 HTTP 요청 처리
        RestTemplate restTemplate = new RestTemplate();

        // Http 요청을 GET 방식으로 실행하여 멤버 정보를 가져옴
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        // 카카오 인증 서버가 반환한 사용자 정보
        String userInfo = responseEntity.getBody();

        // JSON 데이터에서 필요한 정보 추출
        Gson gsonObj = new Gson();
        Map<?, ?> data = gsonObj.fromJson(userInfo, Map.class);

        // 이메일 동의 여부 확인
        boolean emailAgreement = (boolean) ((Map<?, ?>) (data.get("kakao_account"))).get("email_needs_agreement");
        String email;
        if (emailAgreement) { // 사용자가 이메일 동의를 하지 않은 경우
            email = ""; // 대체값 설정
        } else { // 사용자가 이메일 제공에 동의한 경우
            // 이메일 정보 가져오기
            email = (String) ((Map<?, ?>) (data.get("kakao_account"))).get("email");
        }
        if(userRepository.findByEmailCount(email) >= 1 && email != "") {
            throw new BaseException(POST_USERS_EXISTS_EMAIL);
        }
        // 닉네임 동의 여부 확인
        boolean nickNameAgreement = (boolean) ((Map<?, ?>) (data.get("kakao_account"))).get("profile_nickname_needs_agreement");
        String nickName;
        if (nickNameAgreement) { // 사용자가 닉네임 동의를 하지 않은 경우
            nickName = ""; // 대체값 설정
        } else { // 사용자가 닉네임 제공에 동의한 경우
            // 닉네임 정보 가져오기
            nickName = (String) ((Map<?, ?>) ((Map<?, ?>) data.get("properties"))).get("nickname");
        }
        return new GetKakaoUserRes(email, nickName);
    }
}
