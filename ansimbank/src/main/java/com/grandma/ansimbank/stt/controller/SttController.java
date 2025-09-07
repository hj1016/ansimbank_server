package com.grandma.ansimbank.stt.controller;

import com.grandma.ansimbank.application.service.ApplicationService;
import com.grandma.ansimbank.stt.dto.CommandResultDTO;
import com.grandma.ansimbank.stt.service.SttService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/stt")
public class SttController {

    private final SttService sttService;
    private final ApplicationService applicationService; // ✨ 이제 총지배인만 바라봅니다.

    public SttController(SttService sttService, ApplicationService applicationService) {
        this.sttService = sttService;
        this.applicationService = applicationService;
    }

    /**
     * 음성 파일을 받아, 최종 처리 결과(재확인 음성)를 mp3 파일로 반환합니다.
     * @param audioFile 사용자의 음성 파일
     * @return mp3 음성 파일
     */
    @PostMapping(value = "/command", produces = "audio/mpeg") // ✨ 반환 타입이 mp3임을 명시!
    public ResponseEntity<byte[]> processVoiceCommand(@RequestParam("audio") MultipartFile audioFile) {
        // 1. STT 서비스를 호출하여 음성을 텍스트로 변환합니다.
        CommandResultDTO sttResult = sttService.recognize(audioFile);
        String recognizedText = sttResult.getRecognizedText();

        // 2. ApplicationService(총지배인)에게 모든 처리를 위임하고, 최종 결과(음성 파일)를 받습니다.
        byte[] audioContent = applicationService.processVoiceCommand(recognizedText);

        if (audioContent == null) {
            // TTS 변환 실패 시
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // 3. mp3 파일을 클라이언트에게 성공적으로 반환합니다.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        headers.setContentLength(audioContent.length);

        return new ResponseEntity<>(audioContent, headers, HttpStatus.OK);
    }
}

