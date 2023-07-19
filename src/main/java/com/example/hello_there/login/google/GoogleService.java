package com.example.hello_there.login.google;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponseStatus;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
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

import static com.example.hello_there.exception.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class GoogleService {
//    @Value("${spring.security.oauth2.client.registration.google.client-id}")
//    private String Google_Client_Id;
//
//    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
//    private String Google_Secret_Password;

    private final UserRepository userRepository;
    public String getAccessToken(String code){
        //HttpHeaders 생성00
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "309410689177-bjmcjj568j8hrgo539p7fvm411d9op4c.apps.googleusercontent.com");
        body.add("client_secret", "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");
        body.add("redirect_uri" , "http://localhost:8080/oauth/google");
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

    public String getUserEmail(String accessToken) throws BaseException {
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
                return email;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUserName(String accessToken) {
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
                String name = element.getAsJsonObject().get("name").getAsString();

                return name;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
