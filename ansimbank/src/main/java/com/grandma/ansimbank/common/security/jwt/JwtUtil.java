package com.grandma.ansimbank.common.security.jwt;

import com.grandma.ansimbank.common.security.services.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders; // 이거 추가
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct; // 이거 추가 (javax -> jakarta)
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // 1. application.yml 에서 정의한 시크릿 키 값을 주입받을 필드
    @Value("${jwt.secret}")
    private String jwtSecret;

    // 2. 주입받은 문자열을 기반으로 SecretKey 객체를 담을 필드
    private SecretKey key;

    // 3. 의존성 주입 후, 초기화를 위해 실행되는 메서드
    @PostConstruct
    public void init() {
        // yml에서 주입받은 String 형태의 시크릿 키를 Base64로 디코딩
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        // 디코딩된 바이트 배열을 사용하여 SecretKey 객체 생성
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 아래 메서드들은 수정할 필요 없이 그대로 두면 돼
    public String generateJwtToken(UserPrincipal userPrincipal) {
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("userType", userPrincipal.getUserType())
                .claim("userId", userPrincipal.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + 86400000L)) // 기존 변수 대신 직접 값 사용
                .signWith(key) // 여기서 위에서 생성된 key를 사용
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) // 여기서도 동일한 key를 사용
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key) // 여기서도 동일한 key를 사용
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("JWT token is unsupported: ".concat(e.getMessage()));
        } catch (IllegalArgumentException e) {
            System.err.println("JWT claims string is empty: ".concat(e.getMessage()));
        }
        return false;
    }
}