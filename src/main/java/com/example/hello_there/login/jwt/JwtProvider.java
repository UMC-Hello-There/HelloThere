package com.example.hello_there.login.jwt;

import com.example.hello_there.login.dto.JwtResponseDTO;
import com.example.hello_there.utils.Secret;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1 * 24 * 60 * 60 * 1000L; //refreshToken 유효기간 1일
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1 * 60 * 60 * 1000L; //accessToken 유효기간 1시간

    // private static final long REFRESH_TOKEN_EXPIRE_TIME = 60 * 1000L; //refreshToken 유효기간 1분,  refrshToken 테스트를 위해 사용
    // private static final long ACCESS_TOKEN_EXPIRE_TIME = 10 * 1000L; //유효기간 10초, refrshToken 테스트를 위해 사용
    private static final String BEARER_TYPE = "Bearer";

    private Key key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(Secret.JWT_SECRET_KEY));

    //==토큰 생성 메소드==//
    public String createToken(Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME); // 만료기간 6시간

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // (1)
                .claim("userId", userId)
                .setIssuer("test") // 토큰발급자(iss)
                .setIssuedAt(now) // 발급시간(iat)
                .setExpiration(expiration) // 만료시간(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME); // 만료기간 14일

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // (1)
                .claim("userId", userId)
                .setIssuer("test") // 토큰발급자(iss)
                .setIssuedAt(now) // 발급시간(iat)
                .setExpiration(expiration) // 만료시간(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 유저 정보를 가지고 AccessToken, RefreshToken 을 생성하는 메서드
    public JwtResponseDTO.TokenInfo generateToken(Long userId) {
        long now = (new Date()).getTime();

        // Access Token 생성
        String accessToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // (1)
                .claim("userId", userId)
                .setExpiration(new Date(now + ACCESS_TOKEN_EXPIRE_TIME)) // 만료시간
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // (1)
                .claim("userId", userId)
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtResponseDTO.TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
    public Long getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        String memberId = claims.get("userId").toString();

        return Long.valueOf(memberId);

    }

    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    //==토큰 앞 부분('Bearer') 제거 메소드==//
    public String BearerRemove(String token) {
        return token.substring("Bearer ".length());
    }

    public Long getExpiration(String accessToken) {
        // accessToken 남은 유효시간
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody()
                .getExpiration();
        // 현재 시간
        Long now = System.currentTimeMillis();
        return (expiration.getTime() - now);
    }
}
