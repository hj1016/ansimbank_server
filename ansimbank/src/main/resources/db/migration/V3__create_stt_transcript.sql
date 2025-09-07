CREATE TABLE IF NOT EXISTS stt_transcript (
                                              transcript_id BIGINT AUTO_INCREMENT,
                                              audio_id      BIGINT NOT NULL,

                                              language      VARCHAR(10) NOT NULL DEFAULT 'ko',   -- ko, en 등
    engine        VARCHAR(30) NOT NULL DEFAULT 'CLOVA',
    text          LONGTEXT NOT NULL,                   -- 전체 본문
    confidence    DECIMAL(4,3),                        -- 0.000 ~ 1.000

    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_stt_transcript PRIMARY KEY (transcript_id),
    CONSTRAINT fk_stt_transcript_audio
    FOREIGN KEY (audio_id) REFERENCES stt_audio(audio_id),

    INDEX idx_stt_transcript_audio_created (audio_id, created_at)
    );
