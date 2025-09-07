package com.grandma.ansimbank.stt.service;

import com.grandma.ansimbank.config.ClovaSttProperties;
import com.grandma.ansimbank.stt.dto.CommandResultDTO;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class SttService {

    private final RestTemplate restTemplate;
    private final ClovaSttProperties clovaSttProperties;

    public SttService(RestTemplate restTemplate, ClovaSttProperties clovaSttProperties) {
        this.restTemplate = restTemplate;
        this.clovaSttProperties = clovaSttProperties;
    }

    public CommandResultDTO recognize(MultipartFile audioFile) {
        // Clova API로 보낼 HTTP 헤더를 설정합니다.
        HttpHeaders headers = new HttpHeaders();
        // 1. 콘텐츠 타입을 음성 파일 데이터 형식으로 설정합니다.
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        // 2. Client ID와 Client Secret을 헤더에 정확하게 추가합니다.
        headers.set("X-NCP-APIGW-API-KEY-ID", clovaSttProperties.getClientId());
        headers.set("X-NCP-APIGW-API-KEY", clovaSttProperties.getClientSecret());

        try {
            // 3. 음성 파일을 byte 배열로 변환하고, 헤더와 함께 요청 엔티티로 만듭니다.
            byte[] audioData = audioFile.getBytes();
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(audioData, headers);

            // 4. RestTemplate을 사용해 Clova STT API에 POST 요청을 보냅니다.
            ResponseEntity<String> response = restTemplate.postForEntity(
                    clovaSttProperties.getUrl(),
                    requestEntity,
                    String.class
            );

            // 5. 응답으로 온 JSON 문자열에서 'text' 값을 추출합니다.
            JSONObject jsonResponse = new JSONObject(response.getBody());
            String recognizedText = jsonResponse.getString("text");

            return new CommandResultDTO(recognizedText);

        } catch (IOException e) {
            e.printStackTrace();
            return new CommandResultDTO("오디오 파일 처리 중 오류가 발생했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return new CommandResultDTO("음성 인식 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}

