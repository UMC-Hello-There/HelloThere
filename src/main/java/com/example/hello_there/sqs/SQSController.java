package com.example.hello_there.sqs;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponse;
import com.example.hello_there.sqs.dto.PostInquiryReq;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class SQSController {
    private final SQSService sqsService;

    /**
     * 광고 문의하기
     */
    @PostMapping("/inquiry")
    public BaseResponse<String> sendMessage(@RequestBody PostInquiryReq postInquiryReq) {
        try {
            return new BaseResponse<>(sqsService.sendInquiry(postInquiryReq));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}

