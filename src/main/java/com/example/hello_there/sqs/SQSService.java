package com.example.hello_there.sqs;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponseStatus;
import com.example.hello_there.report.ReportRepository;
import com.example.hello_there.sqs.dto.PostEmailRes;
import com.example.hello_there.user.dto.PostInquiryReq;
import com.example.hello_there.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class SQSService {
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${sqs.url}")
    private String sqsUrl;

    private final QueueMessagingTemplate queueMessagingTemplate;
    private final ObjectMapper objectMapper;


    public SQSService(AmazonSQSAsync amazonSQS, ObjectMapper objectMapper, ReportRepository reportRepository) {
        this.queueMessagingTemplate = new QueueMessagingTemplate(amazonSQS);
        this.objectMapper = objectMapper;
    }

    /**
     * 누적 신고로 인한 제재 알림을 이메일로 전송
     */
    public String sendMessage(User receiver, int duration, String prohibition) {
        String receiverEmail = receiver.getEmail();
        // JSON 형식으로 변환
        String jsonMessage = createJsonMessage(receiverEmail, duration, prohibition);
        Message<String> newMessage = MessageBuilder.withPayload(jsonMessage).build();
        queueMessagingTemplate.send("UMCQueue", newMessage);
        return "이메일 알림이 발신되었습니다.";
    }

    /**
     * 광고 문의
     */
    public String sendInquiry(PostInquiryReq postInquiryReq) {
        // JSON 형식의 메시지 생성
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonMessage = null;
        try {
            jsonMessage = objectMapper.writeValueAsString(postInquiryReq);
        } catch (Exception e) {
            // JSON 변환 오류 처리
            throw new BaseException(BaseResponseStatus.REQUEST_ERROR);
        }
        // AWS 자격 증명을 BasicAWSCredentials로 생성
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(accessKey, secretKey)
        );
        AmazonSQS sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("ap-northeast-1")
                .build();
        String queueUrl = sqsUrl;
        // JSON 형식의 메시지를 SQS에 전송
        SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(jsonMessage);
        sqs.sendMessage(sendMessageRequest);
        return "광고 문의가 접수되었습니다.";
    }

    private String createJsonMessage(String receiverEmail, int duration, String prohibition) {
        try {
            // JSON 오브젝트 생성
            PostEmailRes postEmailRes = new PostEmailRes(receiverEmail, duration, prohibition);
            // ObjectMapper를 사용하여 JSON 문자열로 변환
            return objectMapper.writeValueAsString(postEmailRes);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.FAILED_TO_CONVERT);
        }
    }
}
