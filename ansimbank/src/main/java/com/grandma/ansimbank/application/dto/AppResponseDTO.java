package com.grandma.ansimbank.application.dto;

import lombok.Getter;

@Getter
public class AppResponseDTO {
    private final String message;

    public AppResponseDTO(String message) {
        this.message = message;
    }
}
