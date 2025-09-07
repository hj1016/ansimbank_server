package com.grandma.ansimbank.application.service;

import com.grandma.ansimbank.application.dto.AppResponseDTO;
import com.grandma.ansimbank.nlp.dto.NlpResultDTO;
import com.grandma.ansimbank.nlp.service.NlpService;
import com.grandma.ansimbank.tts.service.TtsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final NlpService nlpService;
    private final TtsService ttsService;
    // TODO: 나중에 TransferService, InquiryService 등도 여기에 추가됩니다.

    public ApplicationService(NlpService nlpService, TtsService ttsService) {
        this.nlpService = nlpService;
        this.ttsService = ttsService;
    }

    /**
     * STT로부터 받은 텍스트를 받아, 전체 비즈니스 로직을 총괄하고,
     * 최종 결과(재확인 음성)를 생성합니다.
     * @param recognizedText STT로 변환된 텍스트
     * @return 재확인 음성(mp3)의 byte 배열
     */
    public byte[] processVoiceCommand(String recognizedText) {
        // 1. NLP 서비스를 호출하여 텍스트를 분석합니다.
        NlpResultDTO nlpResult = nlpService.analyze(recognizedText);

        String intent = nlpResult.getIntent();
        String confirmationText; // TTS로 변환할 최종 텍스트

        switch (intent) {
            case "송금":
                // 2-1. 송금에 필요한 정보를 추출합니다.
                List<String> targets = nlpResult.getEntities().get("대상");
                List<String> amounts = nlpResult.getEntities().get("금액");

                String targetName = (targets != null && !targets.isEmpty()) ? targets.get(0) : "알 수 없는 대상";
                String amount = (amounts != null && !amounts.isEmpty()) ? amounts.get(0) : "알 수 없는 금액";

                // 2-2. 재확인 질문 텍스트를 생성합니다.
                // TODO: '10' -> '10만원'으로 변환하는 로직을 추가하면 더 자연스러워집니다.
                confirmationText = String.format("%s님에게 %s만원 송금이 맞습니까? 맞으면 '네'라고 말씀해주세요.", targetName, amount);
                break;

            case "조회":
                confirmationText = "계좌 잔액 조회가 맞습니까? 맞으면 '네'라고 말씀해주세요.";
                break;

            default:
                // NlpService에서 이미 예외처리 했지만, 만약을 위한 방어 코드
                confirmationText = "알 수 없는 요청입니다. 다시 말씀해주세요.";
                break;
        }

        // 3. TtsService를 호출하여 최종 텍스트를 음성으로 변환합니다.
        return ttsService.synthesize(confirmationText);
    }
}

