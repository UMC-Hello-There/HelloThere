package com.example.hello_there.utils;

import com.example.hello_there.house.vo.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class KakaoApiConfig {

    @Value("${kakaoApi.key}")
    private String kakaoApiKey;

    /**
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
