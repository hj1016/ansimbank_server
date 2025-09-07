package com.grandma.ansimbank.tts.service;

import com.grandma.ansimbank.config.ClovaTtsProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class TtsService {

    private final RestTemplate restTemplate;
    private final ClovaTtsProperties clovaTtsProperties;

    public TtsService(RestTemplate restTemplate, ClovaTtsProperties clovaTtsProperties) {
        this.restTemplate = restTemplate;
        this.clovaTtsProperties = clovaTtsProperties;
    }

    /**
     * 입력된 텍스트를 음성(mp3) 데이터로 변환합니다.
     * @param textToSpeak 변환할 텍스트
     * @return mp3 파일의 byte 배열
     */
    public byte[] synthesize(String textToSpeak) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clovaTtsProperties.getClientId());
        headers.set("X-NCP-APIGW-API-KEY", clovaTtsProperties.getClientSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("speaker", "nara"); // 여성 목소리 'nara'
        body.add("volume", "0");
        body.add("speed", "0");
        body.add("pitch", "0");
        body.add("format", "mp3");
        body.add("text", textToSpeak);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.postForEntity(
                    clovaTtsProperties.getUrl(),
                    requestEntity,
                    byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (Exception e) {
            System.err.println("Clova TTS API 호출 중 오류 발생: " + e.getMessage());
        }

        return null; // 오류 발생 시 null 반환
    }
}
