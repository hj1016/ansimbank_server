CREATE TABLE IF NOT EXISTS stt_segment (
                                           segment_id     BIGINT AUTO_INCREMENT,
                                           transcript_id  BIGINT NOT NULL,

                                           start_ms       INT NOT NULL,
                                           end_ms         INT NOT NULL,
                                           text           TEXT NOT NULL,
                                           confidence     DECIMAL(4,3),
    speaker_label  VARCHAR(20),

    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_stt_segment PRIMARY KEY (segment_id),
    CONSTRAINT fk_stt_segment_transcript
    FOREIGN KEY (transcript_id) REFERENCES stt_transcript(transcript_id),

    INDEX idx_segment_transcript_time (transcript_id, start_ms)
    );
