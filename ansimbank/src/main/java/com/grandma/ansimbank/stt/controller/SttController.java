package com.grandma.ansimbank.stt.controller;

import com.grandma.ansimbank.application.service.ApplicationService;
import com.grandma.ansimbank.stt.dto.CommandResultDTO;
import com.grandma.ansimbank.stt.service.SttService;
import jakarta.servlet.http.HttpServletRequest; // ✨ 세션을 사용하기 위해 import
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
    private final ApplicationService applicationService;

    public SttController(SttService sttService, ApplicationService applicationService) {
        this.sttService = sttService;
        this.applicationService = applicationService;
    }

    /**
     * 첫 번째 음성 명령을 처리합니다. (예: "아들에게 10만원 보내줘")
     */
    @PostMapping(value = "/command", produces = "audio/mpeg")
    public ResponseEntity<byte[]> processVoiceCommand(
            @RequestParam("audio") MultipartFile audioFile,
            HttpServletRequest request // ✨ 세션을 사용하기 위해 request 객체를 받습니다.
    ) {
        CommandResultDTO sttResult = sttService.recognize(audioFile);
        String recognizedText = sttResult.getRecognizedText();

        // ✨ 이제 ApplicationService에게 세션(기억 장치)도 함께 넘겨줍니다.
        byte[] audioContent = applicationService.processVoiceCommand(recognizedText, request.getSession());

        if (audioContent == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        headers.setContentLength(audioContent.length);

        return new ResponseEntity<>(audioContent, headers, HttpStatus.OK);
    }

    /**
     * ✨ [새로 추가된 부분]
     * 두 번째 확인 음성("네" 또는 "아니요")을 처리합니다.
     */
    @PostMapping(value = "/confirm", produces = "audio/mpeg")
    public ResponseEntity<byte[]> processVoiceConfirmation(
            @RequestParam("audio") MultipartFile audioFile,
            HttpServletRequest request // ✨ 기억해 둔 정보를 사용하기 위해 세션이 필요합니다.
    ) {
        CommandResultDTO sttResult = sttService.recognize(audioFile);
        String recognizedText = sttResult.getRecognizedText();

        // ✨ "네/아니요" 답변을 처리할 새로운 서비스 메서드를 호출합니다.
        byte[] audioContent = applicationService.processConfirmation(recognizedText, request.getSession());

        if (audioContent == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        headers.setContentLength(audioContent.length);

        return new ResponseEntity<>(audioContent, headers, HttpStatus.OK);
    }
}
