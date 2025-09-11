package com.grandma.ansimbank.stt.dto;

// Lombok 어노테이션을 사용하면 getter, setter, 생성자 등을 자동으로 만들어줍니다.
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // 파라미터 없는 기본 생성자
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자
public class CommandResultDTO {

    // STT API로부터 변환된 텍스트 결과를 담을 필드입니다
    private String recognizedText;

}
