package com.example.hello_there.login.jwt;

import com.example.hello_there.config.BaseException;
import com.example.hello_there.login.jwt.JwtProvider;
import com.example.hello_there.user.User;
import com.example.hello_there.user.UserRepository;
import com.example.hello_there.utils.Secret;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Key;

import static com.example.hello_there.config.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class JwtService {
    private Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(Secret.JWT_SECRET_KEY));
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RedisTemplate redisTemplate;

    /**
     * Header에서 Authorization 으로 JWT 추출
     */
    public String getJwt(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("Authorization");
    }

    public String getRefJwt(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("AuthorizationRef");
    }

    /**
     *   JWT에서 memberId 추출
     */
    public Long getMemberIdx() throws BaseException {
        // 1. JWT 추출
        String accessToken = getJwt();
        if (accessToken == null || accessToken.length() == 0) {
            throw new BaseException(EMPTY_JWT);
        }
        if (checkBlackToken(accessToken)) {
            throw new BaseException(LOG_OUT_MEMBER);
        }
        try {
            // 2. JWT parsing
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken);
            // 3. userId 추출
            return claims.getBody().get("memberId", Long.class);
        } catch (ExpiredJwtException e) {
            // access token이 만료된 경우
            User user = userRepository.findMemberByAccessToken(accessToken).orElse(null);
            if (user == null) {
                throw new BaseException(INVALID_JWT);
            }
            // 4. Refresh Token을 사용하여 새로운 Access Token 발급
            String refreshToken = user.getRefreshToken();
            if (refreshToken != null) {
                String newAccessToken = refreshAccessToken(user, refreshToken);
                // 새로운 Access Token으로 업데이트된 JWT를 사용하여 userId 추출
                Jws<Claims> newClaims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(newAccessToken);
                return newClaims.getBody().get("memberId", Long.class);
            } else {
                throw new BaseException(EMPTY_JWT);
            }
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new BaseException(INVALID_JWT);
        } catch (Exception ignored) {
            throw new BaseException(INVALID_JWT);
        }
    }

    /**
     * 로그아웃 전용 memberId 추출 메서드
     */
    // 로그아웃을 시도할 때는 accsee token과 refresh 토큰이 만료되었어도
    // 형식만 유효하다면 토큰 재발급 없이 로그아웃 할 수 있어야 함.
    public Long getLogoutMemberIdx() throws BaseException {

        // 1. JWT 추출
        String accessToken = getJwt();
        if (accessToken == null || accessToken.length() == 0) {
            throw new BaseException(EMPTY_JWT);
        }
        if (checkBlackToken(accessToken)) {
            throw new BaseException(LOG_OUT_MEMBER);
        }
        try {
            // 2. JWT parsing
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken);
            // 3. userId 추출
            return claims.getBody().get("memberId", Long.class);
        } catch (ExpiredJwtException e) {
            // access token이 만료된 경우
            return 0L;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new BaseException(INVALID_JWT);
        } catch (Exception ignored) {
            throw new BaseException(INVALID_JWT);
        }
    }

    /**
     * 액세스 토큰 재발급
     */
    private String refreshAccessToken(Member member, String refreshToken) throws BaseException {
        try {
            // 리프레시 토큰이 없는(로그인을 하지 않은) 경우
            if (refreshToken.equals("")) {
                throw new BaseException(INVALID_USER_JWT);
            }
            // 리프레시 토큰이 만료 등의 이유로 유효하지 않은 경우
            if (!jwtProvider.validateToken(refreshToken)) {
                throw new BaseException(EXPIRED_USER_JWT);
            }
            else { // 리프레시 토큰이 유효한 경우
                Long memberId = member.getId();
                String refreshedAccessToken = jwtProvider.createToken(memberId);
                // 액세스 토큰 재발급에 성공한 경우
                if (refreshedAccessToken != null) {
                    member.updateAccessToken(refreshedAccessToken);
                    userRepository.save(member);
                    return refreshedAccessToken;
                }
                throw new BaseException(FAILED_TO_REFRESH);
            }
        } catch (BaseException exception) {
            throw new BaseException(exception.getStatus());
        }
    }

    /**
     * Redis 블랙 리스트 등록 여부 확인
     */
    private boolean checkBlackToken(String accessToken) {
        // Redis에 있는 엑세스 토큰인 경우 로그아웃 처리된 엑세스 토큰이다.
        Object redisToken = redisTemplate.opsForValue().get(accessToken);
        if (redisToken != null) { // Redis에 저장된 토큰이면 블랙토큰
            return true;
        }
        return false;
    }

    public Long getMemberIdx(String accessToken) throws BaseException {
        // 1. JWT 추출
        if (accessToken == null || accessToken.length() == 0) {
            throw new BaseException(EMPTY_JWT);
        }
        if (checkBlackToken(accessToken)) {
            throw new BaseException(LOG_OUT_MEMBER);
        }
        try {
            // 2. JWT parsing
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken);
            // 3. userId 추출
            return claims.getBody().get("memberId", Long.class);
        } catch (ExpiredJwtException e) {
            // access token이 만료된 경우
            Member member = userRepository.findMemberByAccessToken(accessToken).orElse(null);
            if (member == null) {
                throw new BaseException(INVALID_JWT);
            }
            // 4. Refresh Token을 사용하여 새로운 Access Token 발급
            String refreshToken = member.getRefreshToken();
            if (refreshToken != null) {
                String newAccessToken = refreshAccessToken(member, refreshToken);
                // 새로운 Access Token으로 업데이트된 JWT를 사용하여 userId 추출
                Jws<Claims> newClaims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(newAccessToken);
                return newClaims.getBody().get("memberId", Long.class);
            } else {
                throw new BaseException(EMPTY_JWT);
            }
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new BaseException(INVALID_JWT);
        } catch (Exception ignored) {
            throw new BaseException(INVALID_JWT);
        }
    }
}
