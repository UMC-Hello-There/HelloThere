package com.example.hello_there.login.kakao;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.exception.BaseResponseStatus;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserStatus;
import com.google.gson.Gson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.example.hello_there.exception.BaseResponseStatus.*;
import static com.example.hello_there.user.UserStatus.*;

@Service
public class KakaoService {

    public String getAccessToken(String code){
        //HttpHeaders 생성00
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "AetzjOnyYuxithVHLRfr97pTGAjVdHXf");
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

    public String getUserEmail(String accessToken) {
        //Httpheader 생성
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + accessToken);
        // httpHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        //Httpheader와 HttpBody를 하나의 객체에 담기(body 정보는 생략 가능)
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        //RestTemplate를 이용하면 브라우저 없이 HTTP 요청을 처리할 수 있다.
        RestTemplate restTemplate = new RestTemplate();

        //Http 요청을 post(GET) 방식으로 실행 -> 문자열로 응답이 들어온다.
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        //카카오 인증 서버가 반환한 사용자 정보
        String userInfo = responseEntity.getBody();

        //JSON 데이터에서 추출한 정보로 User 객체 설정
        Gson gsonObj = new Gson();
        Map<?, ?> data = gsonObj.fromJson(userInfo, Map.class);

        Double id = (Double)(data.get("id"));
        String email = (String) ((Map<?, ?>)(data.get("kakao_account"))).get("email");

        return email;
    }

    public String getUserNickname(String accessToken) {
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

        // JSON 데이터에서 닉네임 추출
        Gson gsonObj = new Gson();
        Map<?, ?> data = gsonObj.fromJson(userInfo, Map.class);

        String nickname = (String) ((Map<?, ?>) (data.get("properties"))).get("nickname");

        return nickname;
    }

    public User getUserInfo(String accessToken) {
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

        // 닉네임 동의 여부 확인
        boolean nickNameAgreement = (boolean) ((Map<?, ?>) (data.get("kakao_account"))).get("profile_nickname_needs_agreement");
        String nickName;
        if (nickNameAgreement) { // 사용자가 닉네임 동의를 하지 않은 경우
            nickName = ""; // 대체값 설정
        } else { // 사용자가 닉네임 제공에 동의한 경우
            // 닉네임 정보 가져오기
            nickName = (String) ((Map<?, ?>) ((Map<?, ?>) data.get("properties"))).get("nickname");
        }

        // 성별 동의 여부 확인
        boolean genderAgreement = (boolean) ((Map<?, ?>) (data.get("kakao_account"))).get("gender_needs_agreement");
        String gender;
        if (genderAgreement) { // 사용자가 성별 동의를 하지 않은 경우
            gender = ""; // 대체값 설정
        } else { // 사용자가 성별 제공에 동의한 경우
            // 성별 정보 가져오기
            gender = (String) ((Map<?, ?>) ((Map<?, ?>) data.get("kakao_account"))).get("gender");
        }

        // 생일 동의 여부 확인
        boolean birthAgreement = (boolean) ((Map<?, ?>) (data.get("kakao_account"))).get("birthday_needs_agreement");
        String birth;
        if (birthAgreement) { // 사용자가 생일 동의를 하지 않은 경우
            birth = ""; // 대체값 설정
        } else { // 사용자가 생일 제공에 동의한 경우
            // 생일 정보 가져오기
            birth = (String) ((Map<?, ?>) ((Map<?, ?>) data.get("kakao_account"))).get("birthday");
        }

        User user = new User();
        if(!email.equals("")){
            user.updateEmail(email);
        }
        if(!nickName.equals("")){
            user.updateNickName(nickName);
        }
        if(!gender.equals("")){
            if(gender.equals("male")){
                user.updateGender(true);
            }
            else {
                user.updateGender(false);
            }
        } else {
            user.updateGender(true);
        }
        if(!birth.equals("")){
            int defaultYear = 2023; // 카카오 API에서 출생연도를 가져올 권한이 없어서 임의 값을 입력
            String month = birth.substring(0, 2); // 달 추출
            String day = birth.substring(2); // 일 추출
            String formattedDate = defaultYear + "-" + month + "-" + day;
            user.updateBirth(LocalDate.parse(formattedDate, DateTimeFormatter.ISO_DATE));
        }
        else {
            LocalDate defaultDate = LocalDate.now();
            user.updateBirth(defaultDate);
        }
        user.updateStatus(ACTIVE);
        return user;
    }
}
