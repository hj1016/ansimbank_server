package com.grandma.ansimbank.application.service;

import com.grandma.ansimbank.nlp.dto.NlpResultDTO;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 사용자의 대화 상태(컨텍스트)를 임시로 저장하는 서비스
 * (실제 서비스에서는 Redis나 다른 저장소 사용을 권장)
 */
@Service
public class ConversationContextService {

    // 임시 저장소 (Key: userId, Value: NLP 분석 결과)
    private final Map<Long, NlpResultDTO> contextMap = new ConcurrentHashMap<>();

    public void save(Long userId, NlpResultDTO context) {
        contextMap.put(userId, context);
    }

    public Optional<NlpResultDTO> get(Long userId) {
        return Optional.ofNullable(contextMap.get(userId));
    }

    public void clear(Long userId) {
        contextMap.remove(userId);
    }
}

