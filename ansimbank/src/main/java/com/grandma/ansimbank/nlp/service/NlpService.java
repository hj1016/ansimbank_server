package com.grandma.ansimbank.nlp.service;

import com.grandma.ansimbank.common.constants.ErrorCode;
import com.grandma.ansimbank.common.exception.CustomException;
import com.grandma.ansimbank.nlp.dto.NlpResultDTO;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Service
public class NlpService {

    private final Komoran komoran;

    public NlpService() {
        this.komoran = new Komoran(DEFAULT_MODEL.FULL);
    }

    public NlpResultDTO analyze(String text) {
        KomoranResult analyzeResultList = komoran.analyze(text);

        String intent = getIntentFromText(analyzeResultList);
        Map<String, List<String>> entities = getEntitiesFromText(analyzeResultList);

        // 예외 처리 로직
        if ("알 수 없음".equals(intent)) {
            throw new CustomException(ErrorCode.INVALID_COMMAND);
        }
        if ("송금".equals(intent) && !entities.containsKey("대상")) {
            throw new CustomException(ErrorCode.TARGET_NOT_FOUND);
        }
        // TODO: 나중에 '조회'에 필요한 정보(예: '어떤 계좌'인지)가 없다면 예외를 추가할 수 있습니다.

        return new NlpResultDTO(text, intent, entities);
    }

    /**
     * 분석 결과에서 키워드를 찾아 의도를 결정합니다.
     */
    private String getIntentFromText(KomoranResult result) {
        // Keywords는 겹치지 않게 추가
        // -> Keywords가 겹치면 원하는 업무 명확하게 처리가 어려움
        // 지금 음성으로 가능한 기능은 송금 + 잔액 조회

        List<String> transferKeywords = Arrays.asList("송금", "보내", "이체");
        List<String> inquiryKeywords = Arrays.asList("조회", "얼마", "잔액", "계좌", "알려줘");

        for (Token token : result.getTokenList()) {
            String morph = token.getMorph();
            if (transferKeywords.contains(morph)) {
                return "송금";
            }
            if (inquiryKeywords.contains(morph)) {
                return "조회";
            }
        }
        return "알 수 없음";
    }

    /**
     * 분석 결과에서 '대상'과 '금액' 정보를 추출합니다.
     */
    private Map<String, List<String>> getEntitiesFromText(KomoranResult result) {
        Map<String, List<String>> entities = new HashMap<>();
        List<String> targets = new ArrayList<>();
        List<String> amounts = new ArrayList<>();

        for (Token token : result.getTokenList()) {
            String pos = token.getPos();

            if ("NNP".equals(pos) || "NNG".equals(pos)) {
                // '송금'이나 '조회' 같은 행동 명사는 대상에서 제외
                if (!token.getMorph().equals("송금") && !token.getMorph().equals("조회")) {
                    targets.add(token.getMorph());
                }
            }
            else if ("SN".equals(pos)) {
                amounts.add(token.getMorph());
            }
        }

        if (!targets.isEmpty()) {
            entities.put("대상", targets);
        }
        if (!amounts.isEmpty()) {
            entities.put("금액", amounts);
        }

        return entities;
    }
}

