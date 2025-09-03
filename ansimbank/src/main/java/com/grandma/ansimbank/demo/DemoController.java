package com.grandma.ansimbank.demo;

import com.grandma.ansimbank.common.constants.ErrorCode;
import com.grandma.ansimbank.common.exception.CustomException;
import com.grandma.ansimbank.common.response.ApiCommonResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo")
@Validated
@RequiredArgsConstructor
public class DemoController {

    private final SimpMessagingTemplate template;

    @PostMapping("/echo")
    public ResponseEntity<ApiCommonResponse<?>> echo(@RequestBody @Validated EchoRequest req) {
        if ("boom".equalsIgnoreCase(req.text())) {
            throw new CustomException(ErrorCode.CONFLICT);
        }
        return ResponseEntity.ok(ApiCommonResponse.success(req));
    }

    public record EchoRequest(
        @NotBlank(message = "text는 비어 있을 수 없습니다.") String  text,
        @Min(value = 1, message = "count는 1 이상이어야 합니다.") int count
    ) {}

    @GetMapping("/send/{userId}")
    public ResponseEntity<ApiCommonResponse<?>> sendToUser(@PathVariable String userId) {
        String payload = "hello " + userId;
        template.convertAndSendToUser(userId, "/queue/test", payload);
        return ResponseEntity.ok(ApiCommonResponse.success(payload));
    }

}
