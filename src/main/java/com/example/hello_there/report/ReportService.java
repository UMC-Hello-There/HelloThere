package com.example.hello_there.report;

import com.example.hello_there.exception.BaseException;
import com.example.hello_there.exception.BaseResponseStatus;
import com.example.hello_there.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final RedisTemplate redisTemplate;

    /**
     * Redis 블랙 리스트 등록 여부 확인
     */
    public void checkBlackUser(String prefix, Long userId) throws BaseException{
        //prefix 로 comment, board, chat 신고를 구분해서 조회
        String value = (String) redisTemplate.opsForValue().get(prefix.concat(userId.toString()));
        if(value != null){
            throw new BaseException(BaseResponseStatus.valueOf(value));
        }
    }

    //prefix 로 comment, board, chat 신고를 구분해서 key-value 저장
    public void setReportExpiration(String prefix, User reported, LocalDateTime expiration, String value) {
        redisTemplate.opsForValue().set(prefix.concat(reported.getId().toString()), value,
                LocalDateTime.now().until(expiration, ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
    }

    public void updateReport(ReportCount count,User reported) {
        int cumulativeReportInt = Integer.parseInt(reported.getCumulativeReport()); // int형 정수로 변환
        int updatedCumulativeReportInt = cumulativeReportInt + count.getCount();
        String updateCumulativeReport = String.valueOf(updatedCumulativeReportInt); // 다시 String으로 형변환
        reported.setCumulativeReport(updateCumulativeReport); // 유저의 누적 신고횟수 업데이트
    }

    public int findCumulativeReportCount(User reported, int digit) {
        String cumulativeReport = reported.getCumulativeReport();
        int length = cumulativeReport.length();
        /**
         * 신고 수가 한 자리 수
         */
        if(length == digit){
            return Integer.parseInt(cumulativeReport.substring(0,1));
        }
        /**
         * 신고 수가 두 자리 수
         */
        return Integer.parseInt(cumulativeReport.substring(length-(digit+1),length-(digit-1)));
    }
}
