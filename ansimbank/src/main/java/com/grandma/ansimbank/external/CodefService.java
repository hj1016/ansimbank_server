package com.grandma.ansimbank.external;

import com.grandma.ansimbank.config.CodefConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodefService {
    
    private final CodefConfig codefConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile String accessToken;
    private volatile long tokenExpiresAt = 0;

    /**
     * 비밀번호 RSA 암호화
     */
    private String encryptPassword(String password) {
        try {
            // CODEF 공개키에서 BEGIN/END 부분 제거 후 Base64 디코딩
            String publicKeyPEM = codefConfig.getPublicKey()
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
            
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);
            
            // RSA 암호화
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            
            return Base64.getEncoder().encodeToString(encryptedBytes);
            
        } catch (Exception e) {
            log.error("비밀번호 RSA 암호화 실패: {}", e.getMessage());
            throw new RuntimeException("비밀번호 암호화 실패", e);
        }
    }

    /**
     * CODEF Access Token 발급
     */
    private String getAccessToken() {
        // 토큰이 유효하면 기존 토큰 반환
        if (accessToken != null && System.currentTimeMillis() < tokenExpiresAt) {
            return accessToken;
        }
        
        try {
            String tokenUrl = codefConfig.getOauthDomain() + CodefConfig.TOKEN_ENDPOINT;
            
            // OAuth 요청 헤더 (Basic Authentication 사용)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(codefConfig.getClientId(), codefConfig.getClientSecret());
            
            // OAuth 요청 파라미터
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "client_credentials");
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            log.info("CODEF 토큰 발급 요청: {}", tokenUrl);
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String token = (String) body.get("access_token");
                Integer expiresIn = (Integer) body.get("expires_in");
                
                this.accessToken = token;
                this.tokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000L);
                
                log.info("CODEF Access Token 발급 성공");
                return token;
            }
            
            throw new RuntimeException("토큰 발급 실패: " + response.getBody());
            
        } catch (Exception e) {
            log.error("CODEF 토큰 발급 실패: {}", e.getMessage());
            throw new RuntimeException("CODEF 토큰 발급 실패", e);
        }
    }

    /**
     * ConnectedId 생성 (실제 CODEF API)
     */
    public String createConnectedId(String bankCode, String userId, String userPassword) {
        try {
            log.info("CODEF ConnectedId 생성 요청: 은행코드={}, 사용자ID={}", bankCode, userId);
            
            String token = getAccessToken();
            String apiUrl = codefConfig.getDemoDomain() + CodefConfig.CREATE_CONNECTED_ID_ENDPOINT;
            
            // API 요청 헤더
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            // API 요청 바디 - CODEF API 명세에 맞게 필수 파라미터 추가
            Map<String, Object> accountInfo = new HashMap<>();
            accountInfo.put("countryCode", "KR");
            accountInfo.put("businessType", "BK");
            accountInfo.put("clientType", "P"); 
            accountInfo.put("organization", bankCode);
            accountInfo.put("loginType", "1");
            accountInfo.put("id", userId);
            accountInfo.put("password", encryptPassword(userPassword));
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("accountList", List.of(accountInfo));
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            log.info("CODEF ConnectedId 생성 API 호출: {}", apiUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String responseBody = response.getBody();
                log.info("CODEF API 응답: {}", responseBody);
                
                // URL 디코딩 후 JSON 파싱
                String decodedResponse = URLDecoder.decode(responseBody, StandardCharsets.UTF_8);
                Map<String, Object> body = objectMapper.readValue(decodedResponse, Map.class);
                
                // CODEF API 응답 구조: { "result": {...}, "data": { "connectedId": "..." } }
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                if (data != null) {
                    String connectedId = (String) data.get("connectedId");
                    if (connectedId != null) {
                        log.info("ConnectedId 생성 성공: {}", connectedId);
                        return connectedId;
                    }
                }
                
                // 오류 정보 출력
                Map<String, Object> result = (Map<String, Object>) body.get("result");
                if (result != null) {
                    String errorMessage = (String) result.get("message");
                    log.error("CODEF API 오류: {}", errorMessage);
                    throw new RuntimeException("CODEF API 오류: " + errorMessage);
                }
            }
            
            log.error("ConnectedId 생성 실패: {}", response.getBody());
            throw new RuntimeException("ConnectedId 생성 실패: " + response.getBody());
            
        } catch (Exception e) {
            log.error("ConnectedId 생성 실패: {}", e.getMessage());
            throw new RuntimeException("ConnectedId 생성 실패", e);
        }
    }

    /**
     * 계좌 목록 조회 (실제 CODEF API)
     */
    public String getAccountList(String connectedId, String bankCode) {
        try {
            log.info("계좌 목록 조회 요청: connectedId={}, bankCode={}", connectedId, bankCode);
            
            String token = getAccessToken();
            String apiUrl = codefConfig.getDemoDomain() + CodefConfig.ACCOUNT_LIST_ENDPOINT;
            
            // API 요청 헤더
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            // API 요청 바디
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("connectedId", connectedId);
            requestBody.put("organization", bankCode);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            log.info("CODEF 계좌 목록 조회 API 호출: {}", apiUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String responseBody = response.getBody();
                log.info("CODEF 계좌 목록 조회 API 응답: {}", responseBody);
                
                // URL 디코딩 후 반환
                String decodedResponse = URLDecoder.decode(responseBody, StandardCharsets.UTF_8);
                log.info("계좌 목록 조회 성공");
                return decodedResponse;
            }
            
            log.error("계좌 목록 조회 실패: {}", response.getBody());
            throw new RuntimeException("계좌 목록 조회 실패: " + response.getBody());
            
        } catch (Exception e) {
            log.error("계좌 목록 조회 실패: {}", e.getMessage());
            throw new RuntimeException("계좌 목록 조회 실패", e);
        }
    }
}