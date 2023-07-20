package com.example.hello_there.utils;

import com.example.hello_there.board.Board;
import com.example.hello_there.board.BoardRepository;
import com.example.hello_there.comment.Comment;
import com.example.hello_there.comment.CommentRepository;
import com.example.hello_there.chat_room.ChatRoom;
import com.example.hello_there.chat_room.ChatRoomRepository;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponseStatus;
import com.example.hello_there.house.HouseRepository;
import com.example.hello_there.house.vo.GeoPoint;
import com.example.hello_there.login.jwt.Token;
import com.example.hello_there.login.jwt.TokenRepository;
import com.example.hello_there.house.House;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.example.hello_there.exception.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class UtilService {
    public static final int SEC = 60;
    public static final int MIN = 60;
    public static final int HOUR = 24;
    public static final int DAY = 30;
    public static final int MONTH = 12;


    @Value("${kakaoApi.key}")
    private String kakaoApiKey;

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final TokenRepository tokenRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final HouseRepository houseRepository;

    public User findByUserIdWithValidation(Long userId) throws BaseException {
        return userRepository.findUserById(userId)
                .orElseThrow(() -> new BaseException(NONE_EXIST_USER));
    }

    public User findByEmailWithValidation(String email) throws BaseException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(POST_USERS_NONE_EXISTS_EMAIL));
    }

    public House findHouseWithValidation(String city, String distrct, String houseName) throws BaseException {
        House house = houseRepository.findProperty(city, distrct, houseName).orElse(null);
        if(house == null) throw new BaseException(BaseResponseStatus.POST_USERS_NONE_EXISTS_HOUSE);
        return house;
    }

    public Board findByBoardIdWithValidation(Long boardId) throws BaseException {
        return boardRepository.findBoardById(boardId)
                .orElseThrow(() -> new BaseException(NONE_EXIST_BOARD));
    }

    public Comment findByCommentIdWithValidation(Long commentId) throws BaseException {
        return commentRepository.findById(commentId)
                .orElseThrow(()-> new BaseException(NONE_EXIST_COMMENT));
    }

    public Token findTokenByUserIdWithValidation(Long userId) throws BaseException {
        return tokenRepository.findTokenByUserId(userId)
                .orElseThrow(() -> new BaseException(INVALID_USER_JWT));
    }

    public ChatRoom findChatRoomByChatRoomIdWithValidation(String chatRoomId) throws BaseException {
        return chatRoomRepository.findChatRoomById(chatRoomId)
                .orElseThrow(() -> new BaseException(NONE_EXIST_ROOM));
    }

    public static String  convertLocalDateTimeToLocalDate(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    public static String convertLocalDateTimeToTime(LocalDateTime localDateTime) {
        LocalDateTime now = LocalDateTime.now();

        long diffTime = localDateTime.until(now, ChronoUnit.SECONDS); // now보다 이후면 +, 전이면 -

        if (diffTime < SEC){
            return diffTime + "초 전";
        }
        diffTime = diffTime / SEC;
        if (diffTime < MIN) {
            return diffTime + "분 전";
        }
        diffTime = diffTime / MIN;
        if (diffTime < HOUR) {
            return diffTime + "시간 전";
        }
        diffTime = diffTime / HOUR;
        if (diffTime < DAY) {
            return diffTime + "일 전";
        }
        diffTime = diffTime / DAY;
        if (diffTime < MONTH) {
            return diffTime + "개월 전";
        }
        diffTime = diffTime / MONTH;
        return diffTime + "년 전";
    }

    /*
     * [GeoPoint] 주소 텍스트를 입력 받아 GPS 좌표 값 반환
     */
    public GeoPoint getGpsCoords(String address) throws IOException {
        String REST_API_KEY = kakaoApiKey;
        String query = address;
        String xValue = "";
        String yValue = "";

        String apiUrl = "https://dapi.kakao.com/v2/local/search/address.json";
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(apiUrl + "?query=" + encodedQuery);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + REST_API_KEY);

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String responseBody = EntityUtils.toString(entity);

        if (responseBody != null) {
            JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
            JsonArray documents = jsonObject.getAsJsonArray("documents");

            if (documents.size() > 0) {
                JsonObject document = documents.get(0).getAsJsonObject();   //도로명 주소  기준
                xValue = document.get("x").getAsString();
                yValue = document.get("y").getAsString();

            } else {
                System.out.println("No documents found in the response.");
                return null;
            }

            return GeoPoint.fromString(xValue, yValue);
        }
        return null;
    }
}
