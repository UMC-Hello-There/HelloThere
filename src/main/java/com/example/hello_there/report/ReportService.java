package com.example.hello_there.report;

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
    public boolean checkBlackUser(Long userId) {
        // Redis에 있는 유저는 제재 상태이다.
        Object redisUser = redisTemplate.opsForValue().get(userId.toString());
        if (redisUser != null) { // Redis에 저장된 유저면 블랙유저
            return true;
        }
        return false;
    }

    public void setReportExpiration(User reported, LocalDateTime expiration, String value) {
        redisTemplate.opsForValue().set(reported.getId().toString(), value,
                LocalDateTime.now().until(expiration, ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
    }
}
