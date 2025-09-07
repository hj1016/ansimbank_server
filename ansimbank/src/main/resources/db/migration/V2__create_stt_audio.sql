CREATE TABLE IF NOT EXISTS stt_audio (

  audio_id BIGINT AUTO_INCREMENT,
  user_id BIGINT NULL,

  source ENUM('UPLOAD','MIC','CALL') NOT NULL DEFAULT 'UPLOAD',
  storage_provider ENUM('S3','LOCAL') NOT NULL DEFAULT 'S3',

  object_key VARCHAR(512) NOT NULL,          -- S3 오브젝트 키(런타임에 presigned URL 생성)
  content_type VARCHAR(100),                 -- audio/wav, audio/mpeg 등
  size_bytes BIGINT,
  duration_ms INT,
  sample_rate INT,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT pk_stt_audio PRIMARY KEY (audio_id),
  CONSTRAINT fk_stt_audio_user FOREIGN KEY (user_id) REFERENCES users(user_id),
  INDEX idx_stt_audio_user_created (user_id, created_at)
);
