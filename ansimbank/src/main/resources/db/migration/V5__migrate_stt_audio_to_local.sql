-- 1) local_path 추가
ALTER TABLE stt_audio
    ADD COLUMN local_path VARCHAR(1024) NULL AFTER user_id;

-- 2.5) NULL 값을 빈 문자열로 채워주기
UPDATE stt_audio
SET local_path = ''
WHERE local_path IS NULL;

-- 3) local_path NOT NULL 로 변경
ALTER TABLE stt_audio
    MODIFY COLUMN local_path VARCHAR(1024) NOT NULL;