package com.example.hello_there.apratment;

import com.example.hello_there.apratment.dto.PatchApartmentReq;
import com.example.hello_there.apratment.dto.PostApartmentReq;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.utils.UtilService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApartmentService {
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final UtilService utilService;

    public Apartment createApartment(PostApartmentReq postApartmentReq) {
        if (postApartmentReq.getCity().isEmpty() || postApartmentReq.getDistrict().isEmpty()
                || postApartmentReq.getStreetAddress().isEmpty() || postApartmentReq.getNumberAddress().isEmpty()
                || postApartmentReq.getApartmentName().isEmpty()) {
            return null; // null 필드가 있는 아파트는 등록하지 않는다.(데이터의 유효성과 NPE 발생 예방을 위함)
        }

        Apartment apartment = Apartment.builder()
                .city(postApartmentReq.getCity())
                .district(postApartmentReq.getDistrict())
                .streetAddress(postApartmentReq.getStreetAddress())
                .numberAddress(postApartmentReq.getNumberAddress())
                .apartmentName(postApartmentReq.getApartmentName())
                .build();
        apartmentRepository.save(apartment);
        return apartment;
    }

    @Transactional
    public String modifyApartment(PatchApartmentReq patchApartmentReq, Long userId) throws BaseException {
        Apartment apartment = utilService.findApartmentWithValidation(patchApartmentReq.getCity(),
                patchApartmentReq.getDistrict(), patchApartmentReq.getApartmentName());
        User user = utilService.findByUserIdWithValidation(userId);
        user.setApartment(apartment);
        return "아파트 등록이 완료되었습니다";
    }

    // 인천시 부평구
    public List<PostApartmentReq> getApartmentInfoBupyeong() throws UnsupportedEncodingException {
        WebClient webClient = WebClient.create();
        String url = URLDecoder.decode("https://api.odcloud.kr/api/3078697/v1/uddi:422b753c-1bea-48d8-942d-bfb2306db2f9?page=1&perPage=50&serviceKey=3bOb7HStMQDC44spijePkGaD6QUjfK02jgBW2JVhYWPbqMQkdmHpUX5RnR94WyF4YdnbIBauhir7yZ%2FsaAoBAg%3D%3D", "UTF-8");

        Mono<String> responseMono = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class);

        String apartmentInfo = responseMono.block();

        Gson gsonObj = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> data = gsonObj.fromJson(apartmentInfo, type);

        List<Map<String, Object>> apartmentList = (List<Map<String, Object>>) data.get("data");
        List<PostApartmentReq> apartmentInfos = apartmentList.stream()
                .map(apartment -> {
                    String entireStreetAddress = apartment.containsKey("소재지도로명주소") ? (String) apartment.get("소재지도로명주소") : "";
                    String entireNumberAddress = apartment.containsKey("소재지지번주소") ? (String) apartment.get("소재지도로명주소") : "";
                    String city = "인천광역시";
                    String district = "부평구";
                    String streetAddress = "";
                    String numberAddress = "";
                    String apartmentName = apartment.containsKey("아파트명") ? (String) apartment.get("아파트명") : "";

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

                    return new PostApartmentReq(city, district, streetAddress, numberAddress, apartmentName);
                })
                .collect(Collectors.toList());
        return apartmentInfos;
    }

    // 서울시 동작구
    public List<PostApartmentReq> getApartmentInfoDongjak() throws UnsupportedEncodingException {
        WebClient webClient = WebClient.create();
        String url = URLDecoder.decode("https://api.odcloud.kr/api/15006108/v1/uddi:c95160d2-a482-45a0-b16d-bd2e10df4e53?page=1&perPage=50&serviceKey=3bOb7HStMQDC44spijePkGaD6QUjfK02jgBW2JVhYWPbqMQkdmHpUX5RnR94WyF4YdnbIBauhir7yZ%2FsaAoBAg%3D%3D", "UTF-8");

        Mono<String> responseMono = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class);

        String apartmentInfo = responseMono.block();

        Gson gsonObj = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> data = gsonObj.fromJson(apartmentInfo, type);

        List<Map<String, Object>> apartmentList = (List<Map<String, Object>>) data.get("data");
        List<PostApartmentReq> apartmentInfos = apartmentList.stream()
                .map(apartment -> {
                    String city = "서울특별시";
                    String district = "동작구";
                    String streetAddress = apartment.containsKey("도로명주소") ? (String) apartment.get("도로명주소") : "";
                    String numberAddress = (apartment.containsKey("행정동") ? apartment.get("행정동") : "") + " " + (apartment.containsKey("번지") ? apartment.get("번지") : "");
                    String apartmentName = apartment.containsKey("아파트명") ? (String) apartment.get("아파트명") : "";
                    return new PostApartmentReq(city, district, streetAddress, numberAddress, apartmentName);
                })
                .collect(Collectors.toList());
        return apartmentInfos;
    }

    // 서울시 관악구
    public List<PostApartmentReq> getApartmentInfoGwanak() throws UnsupportedEncodingException {
        WebClient webClient = WebClient.create();
        String url = URLDecoder.decode("https://api.odcloud.kr/api/15039534/v1/uddi:cfde5a6d-dda3-4b18-aaad-ba8fb7722b95?page=1&perPage=50&serviceKey=3bOb7HStMQDC44spijePkGaD6QUjfK02jgBW2JVhYWPbqMQkdmHpUX5RnR94WyF4YdnbIBauhir7yZ%2FsaAoBAg%3D%3D", "UTF-8");

        Mono<String> responseMono = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class);

        String apartmentInfo = responseMono.block();

        Gson gsonObj = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> data = gsonObj.fromJson(apartmentInfo, type);

        List<Map<String, Object>> apartmentList = (List<Map<String, Object>>) data.get("data");
        List<PostApartmentReq> apartmentInfos = apartmentList.stream()
                .map(apartment -> {
                    String entireStreetAddress = apartment.containsKey("새주소") ? (String) apartment.get("새주소") : "";
                    String city = "서울특별시";
                    String district = "관악구";
                    String streetAddress = "";
                    String apartmentName = apartment.containsKey("아파트명") ? (String) apartment.get("아파트명") : "";

                    if (!entireStreetAddress.isEmpty()) {
                        String[] streetAddressSplit = entireStreetAddress.split(" ");
                        if (streetAddressSplit.length >= 4) {
                            streetAddress = streetAddressSplit[2] + " " + streetAddressSplit[3];
                        }
                    }

                    String numberAddress = (apartment.containsKey("행정동명") ? (String) apartment.get("행정동명") : "")
                            + " " + (apartment.containsKey("지번") ? (String) apartment.get("지번") : "");

                    return new PostApartmentReq(city, district, streetAddress, numberAddress, apartmentName);
                })
                .collect(Collectors.toList());

        return apartmentInfos;
    }

    // 서울시 송파구
    public List<PostApartmentReq> getApartmentInfoSongpa() throws UnsupportedEncodingException {
        WebClient webClient = WebClient.create();
        String url = URLDecoder.decode("https://api.odcloud.kr/api/15044851/v1/uddi:a253c2fd-db9f-4e93-8352-ea34b32ce251_201910310934?page=1&perPage=50&serviceKey=3bOb7HStMQDC44spijePkGaD6QUjfK02jgBW2JVhYWPbqMQkdmHpUX5RnR94WyF4YdnbIBauhir7yZ%2FsaAoBAg%3D%3D", "UTF-8");

        Mono<String> responseMono = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class);

        String apartmentInfo = responseMono.block();

        Gson gsonObj = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> data = gsonObj.fromJson(apartmentInfo, type);

        List<Map<String, Object>> apartmentList = (List<Map<String, Object>>) data.get("data");
        List<PostApartmentReq> apartmentInfos = apartmentList.stream()
                .map(apartment -> {
                    String entireStreetAddress = apartment.containsKey("도로명주소(송파구)") ? (String) apartment.get("도로명주소(송파구)") : "";
                    String entireNumberAddress = apartment.containsKey("지번주소") ? (String) apartment.get("지번주소") : "";
                    String city = "서울특별시";
                    String district = "송파구";
                    String streetAddress = "";
                    String numberAddress = "";
                    String apartmentName = apartment.containsKey("아파트명") ? (String) apartment.get("아파트명") : "";

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

                    return new PostApartmentReq(city, district, streetAddress, numberAddress, apartmentName);
                })
                .collect(Collectors.toList());
        return apartmentInfos;
    }
}
