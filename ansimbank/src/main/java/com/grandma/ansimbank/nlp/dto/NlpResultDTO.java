package com.grandma.ansimbank.nlp.dto;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;

/**
 * KOMORAN 자연어 처리 분석 결과를 담는 DTO 입니다.
 * 이 DTO는 최종적으로 클라이언트에게 반환됩니다.
 */
@Getter
@ToString
public class NlpResultDTO {

    private final String originalText; // STT가 변환한 원본 텍스트
    private final String intent;       // 분석된 핵심 의도 (주로 동사)
    private final Map<String, List<String>> entities; // 분석된 핵심 정보 (주체, 대상, 금액 등)

    public NlpResultDTO(String originalText, String intent, Map<String, List<String>> entities) {
        this.originalText = originalText;
        this.intent = intent;
        this.entities = entities;
    }
}