package com.grandma.ansimbank.application.service;

import com.grandma.ansimbank.nlp.dto.NlpResultDTO;
import com.grandma.ansimbank.nlp.service.NlpService;
import com.grandma.ansimbank.tts.service.TtsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final NlpService nlpService;
    private final TtsService ttsService;

    public ApplicationService(NlpService nlpService, TtsService ttsService) {
        this.nlpService = nlpService;
        this.ttsService = ttsService;
    }

    public byte[] processVoiceCommand(String recognizedText, HttpSession session) {
        // STT가 뭐라고 알아들었는지 확인
        System.out.println("[STT 결과 확인] recognizedText: '" + recognizedText + "'");
        NlpResultDTO nlpResult = nlpService.analyze(recognizedText);

        // NLP가 금액을 제대로 알아들었는지 확인
        System.out.println("[NLP 결과 확인] nlpResult: " + nlpResult.toString());
        String intent = nlpResult.getIntent();
        String confirmationText;

        switch (intent) {
            case "송금":
                List<String> targets = nlpResult.getEntities().get("대상");
                List<String> amounts = nlpResult.getEntities().get("금액");
                String targetName = (targets != null && !targets.isEmpty()) ? targets.get(0) : "알 수 없는 대상";
                String amount = (amounts != null && !amounts.isEmpty()) ? amounts.get(0) : "알 수 없는 금액";

                session.setAttribute("pendingIntent", "송금");
                session.setAttribute("transferTarget", targetName);
                session.setAttribute("transferAmount", amount);

                confirmationText = String.format("%s님에게 %s만원 송금이 맞습니까? 맞으면 '네'라고 말씀해주세요.", targetName, amount);
                break;
            case "조회":
                session.setAttribute("pendingIntent", "조회");
                confirmationText = "계좌 잔액 조회가 맞습니까? 맞으면 '네'라고 말씀해주세요.";
                break;
            default:
                confirmationText = "알 수 없는 요청입니다. 다시 말씀해주세요.";
                break;
        }
        return ttsService.synthesize(confirmationText);
    }

    /**
     * ✨ [최종 수정] "네/아니요"와 같은 확인 답변을 처리하는 로직을 훨씬 더 안정적으로 변경했습니다.
     * 어떤 상황에서도 세션 정보를 확실하게 삭제하고, 다양한 답변에 유연하게 대처합니다.
     * @param recognizedText "네" 또는 "아니요" 등의 텍스트
     * @param session        이전에 기억해 둔 송금 정보를 가져오기 위한 세션 객체
     * @return 최종 결과(송금 완료/취소) 음성(mp3)의 byte 배열
     */
    public byte[] processConfirmation(String recognizedText, HttpSession session) {
        String finalText;
        try {
            // 1. STT 결과를 로그로 찍고, 모든 군더더기를 제거합니다.
            System.out.println("[LOG] STT 원본 텍스트: '" + recognizedText + "'");
            String normalizedText = recognizedText.replaceAll("[\\s.,?!]", "");
            System.out.println("[LOG] 정제된 텍스트: '" + normalizedText + "'");

            // 2. "아니요", "취소" 같은 명확한 부정 의사를 먼저 확인합니다.
            if (normalizedText.contains("아니") || normalizedText.contains("취소") || normalizedText.contains("잘못")) {
                finalText = "요청을 취소했습니다. 처음부터 다시 말씀해주세요.";
                return ttsService.synthesize(finalText); // 여기서 대화 즉시 종료
            }

            // 3. 세션에서 사용자의 이전 의도(송금, 조회 등)를 가져옵니다.
            String pendingIntent = (String) session.getAttribute("pendingIntent");

            // 4. 만약 서버가 기억을 잃었을 경우 (pendingIntent가 없을 때)
            if (pendingIntent == null) {
                if (normalizedText.contains("송금")) {
                    finalText = "송금 정보가 사라진 것 같아요. 죄송하지만, 누구에게 얼마를 보낼지 처음부터 다시 말씀해주시겠어요?";
                } else {
                    finalText = "무엇을 하시려던 건지 정보가 사라졌어요. 죄송하지만 처음부터 다시 시도해주세요.";
                }
                return ttsService.synthesize(finalText); // 여기서 대화 즉시 종료
            }

            // 5. 사용자의 의도를 기억하고 있을 때, 긍정 답변인지 확인합니다.
            boolean isConfirmed = normalizedText.contains("네") ||
                    normalizedText.contains("예") ||
                    normalizedText.contains("응") ||
                    normalizedText.contains("맞아") ||
                    normalizedText.contains(pendingIntent); // ex: 답변에 또 "송금"이 포함된 경우

            if (isConfirmed) {
                if ("송금".equals(pendingIntent)) {
                    String targetName = (String) session.getAttribute("transferTarget");
                    String amount = (String) session.getAttribute("transferAmount");
                    System.out.printf("[LOG] 실제 송금 실행: %s에게 %s만원%n", targetName, amount);
                    finalText = String.format("%s님에게 %s만원 송금이 완료되었습니다.", targetName, amount);
                } else if ("조회".equals(pendingIntent)) {
                    finalText = "계좌 잔액은 123,456원 입니다.";
                } else {
                    finalText = "알 수 없는 요청을 확인했습니다. 다시 시도해주세요.";
                }
            } else {
                // 긍정도, 부정도 아닌 애매한 답변일 경우
                finalText = "요청을 취소했습니다. 다시 말씀해주세요.";
            }
        } finally {
            // ✨ [안전장치] 어떤 경우에도 대화가 끝나면 세션에 저장된 정보를 깨끗하게 지웁니다.
            session.removeAttribute("pendingIntent");
            session.removeAttribute("transferTarget");
            session.removeAttribute("transferAmount");
        }

        // 최종 결과 텍스트를 음성으로 변환하여 반환
        return ttsService.synthesize(finalText);
    }
}

