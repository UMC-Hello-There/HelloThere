package com.example.hello_there.house;

import com.example.hello_there.house.dto.PatchHouseReq;
import com.example.hello_there.house.dto.PostHouseReq;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.house.vo.GeoPoint;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.utils.KakaoApiConfig;
import com.example.hello_there.utils.UtilService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseService {
    private final HouseRepository houseRepository;
    private final UtilService utilService;
    private final KakaoApiConfig kakaoApiConfig;

    public List<House> findByRadius(GeoPoint coords, Double radius) {
        List<House> houseList = houseRepository.findByRadius(coords.getLat(), coords.getLng(), radius);

        return houseList;
    }

    //House 등록 서비스
    public House createHouse(PostHouseReq postHouseReq) {

            if (postHouseReq.getCity().isEmpty() || postHouseReq.getDistrict().isEmpty()
                    || postHouseReq.getStreetAddress().isEmpty() || postHouseReq.getNumberAddress().isEmpty()
                    || postHouseReq.getHouseName().isEmpty()
                    || Optional.ofNullable(postHouseReq.getLocation()).isEmpty()
                    || ObjectUtils.defaultIfNull(postHouseReq.getLocation().getLat(),0.0).equals(0.0)
                    || ObjectUtils.defaultIfNull(postHouseReq.getLocation().getLng(),0.0).equals(0.0)) {
                return null; // null 필드가 있는 아파트는 등록하지 않는다.(데이터의 유효성과 NPE 발생 예방을 위함)
            }

            House house = House.builder()
                    .city(postHouseReq.getCity())
                    .district(postHouseReq.getDistrict())
                    .streetAddress(postHouseReq.getStreetAddress())
                    .numberAddress(postHouseReq.getNumberAddress())
                    .houseName(postHouseReq.getHouseName())
                    .location(GeoPoint.toPoint(postHouseReq.getLocation()))
                    .build();

        houseRepository.save(house);
        return house;
    }

    @Transactional
    public House setHouseInfo(Long userId, Long houseId) throws BaseException {
        House house = utilService.findByHouseIdWithValidation(houseId);
        User user = utilService.findByUserIdWithValidation(userId);
        user.setHouse(house);
        return house;
    }

    public List<PostHouseReq> getHouseInfoByRegion(String city, String district, String url) throws UnsupportedEncodingException {
        WebClient webClient = WebClient.create();
        String decodedUrl = URLDecoder.decode(url, "UTF-8");

        Mono<String> responseMono = webClient.get()
                .uri(decodedUrl)
                .retrieve()
                .bodyToMono(String.class);

        String houseInfo = responseMono.block();

        Gson gsonObj = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> data = gsonObj.fromJson(houseInfo, type);

        List<Map<String, Object>> houseList = (List<Map<String, Object>>) data.get("data");
        List<PostHouseReq> houseInfos = houseList.stream()
                .map(house -> {
                    String entireStreetAddress = "";
                    String entireNumberAddress = "";
                    String streetAddress = "";
                    String numberAddress = "";
                    String houseName = house.containsKey("아파트명") ? (String) house.get("아파트명") : "";
                    GeoPoint location = new GeoPoint(1.0, 1.0);

                    if (city.equals("인천광역시") && district.equals("부평구")) {
                        entireStreetAddress = house.containsKey("소재지도로명주소") ? (String) house.get("소재지도로명주소") : "";
                        entireNumberAddress = house.containsKey("소재지지번주소") ? (String) house.get("소재지지번주소") : "";
                    } else if (city.equals("서울특별시") && district.equals("동작구")) {
                        streetAddress = house.containsKey("도로명주소") ? (String) house.get("도로명주소") : "";
                        numberAddress = (house.containsKey("행정동") ? house.get("행정동") : "") + " " + (house.containsKey("번지") ? house.get("번지") : "");
                    } else if (city.equals("서울특별시") && district.equals("관악구")) {
                        entireStreetAddress = house.containsKey("새주소") ? (String) house.get("새주소") : "";
                        numberAddress = (house.containsKey("행정동명") ? (String) house.get("행정동명") : "")
                                + " " + (house.containsKey("지번") ? (String) house.get("지번") : "");
                    } else if (city.equals("서울특별시") && district.equals("송파구")) {
                        entireStreetAddress = house.containsKey("도로명주소(송파구)") ? (String) house.get("도로명주소(송파구)") : "";
                        entireNumberAddress = house.containsKey("지번주소") ? (String) house.get("지번주소") : "";
                    }

                    if (!entireStreetAddress.isEmpty()) {
                        String[] streetAddressSplit = entireStreetAddress.split(" ");
                        if (streetAddressSplit.length >= 4) {
                            streetAddress = streetAddressSplit[2] + " " + streetAddressSplit[3];
                        }
                    }

                    if (!entireNumberAddress.isEmpty()) {
                        String[] numberAddressSplit = entireNumberAddress.split(" ");
                        if (numberAddressSplit.length >= 4) {
                            numberAddress = numberAddressSplit[2] + " " + numberAddressSplit[3];
                        }
                    }

                    try {
                        location = kakaoApiConfig.getGpsCoords(city+" "+district+" "+streetAddress);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    return new PostHouseReq(city, district, streetAddress, numberAddress, houseName, location);
                })
                .collect(Collectors.toList());
        return houseInfos;
    }
}
