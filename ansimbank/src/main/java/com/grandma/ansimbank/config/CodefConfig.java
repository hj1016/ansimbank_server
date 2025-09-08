package com.grandma.ansimbank.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class CodefConfig {
    
    @Value("${codef.client-id}")
    private String clientId;
    
    @Value("${codef.client-secret}")
    private String clientSecret;
    
    @Value("${codef.public-key}")
    private String publicKey;
    
    @Value("${codef.demo-domain:https://development.codef.io}")
    private String demoDomain;
    
    @Value("${codef.oauth-domain:https://oauth.codef.io}")
    private String oauthDomain;
    
    // CODEF API 엔드포인트
    public static final String TOKEN_ENDPOINT = "/oauth/token";
    public static final String CREATE_CONNECTED_ID_ENDPOINT = "/v1/account/create";
    public static final String ADD_CONNECTED_ID_ENDPOINT = "/v1/account/add-connected-id";
    public static final String ACCOUNT_LIST_ENDPOINT = "/v1/kr/bank/p/account/account-list";
    
    // CODEF 은행 코드 매핑
    public static final String BANK_CODE_KB = "0004";
    public static final String BANK_CODE_SHINHAN = "0088";
    public static final String BANK_CODE_WOORI = "0020";
    public static final String BANK_CODE_HANA = "0081";
    public static final String BANK_CODE_NH = "0011";
    
    public String getBankCode(String bankName) {
        return switch (bankName) {
            case "KB국민은행", "국민은행" -> BANK_CODE_KB;
            case "신한은행" -> BANK_CODE_SHINHAN;
            case "우리은행" -> BANK_CODE_WOORI;
            case "하나은행" -> BANK_CODE_HANA;
            case "NH농협은행", "농협은행" -> BANK_CODE_NH;
            default -> "0000"; // 기타 은행
        };
    }
}