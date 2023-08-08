package com.example.hello_there.advertisement;

import com.example.hello_there.advertisement.dto.GetAdRes;
import com.example.hello_there.advertisement.dto.PostAdReq;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class AdController {
    private final AdService adService;
    //광고 생성
    @PostMapping(value = "/advertisement",consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public BaseResponse<String> saveAd(
            @RequestPart MultipartFile adImg,
            @RequestPart PostAdReq postAdReq
            ) {
        try{
            return new BaseResponse<>(adService.saveAd(adImg,postAdReq));
        }
        catch(BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    // API TEST
    @GetMapping("/advertisement")
    public BaseResponse<GetAdRes> adResponseTest(@RequestParam String district){
        try{
            return new BaseResponse<>(adService.findAd(district));
        }
        catch(BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
