package com.grandma.ansimbank.approval;

import com.grandma.ansimbank.approval.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller @RequiredArgsConstructor
public class ApprovalStompController {
    private final ApprovalService service;

    public record ApprovalRequestCmd(Long parentId, Long amount, String memo) {}
    public record ApprovalDecisionCmd(Long approvalId, String decision) {} // APPROVE|REJECT

    @MessageMapping("/approval.request") // 클라 → /app/approval.request
    public void request(@Payload ApprovalRequestCmd cmd, Principal p) {
        Long childId = Long.valueOf(p.getName());
        service.requestApproval(childId, cmd.parentId(), cmd.amount(), cmd.memo());
    }

    @MessageMapping("/approval.decide")  // 클라 → /app/approval.decide
    public void decide(@Payload ApprovalDecisionCmd cmd, Principal p) {
        Long parentId = Long.valueOf(p.getName());
        service.decide(parentId, cmd.approvalId(), "APPROVE".equalsIgnoreCase(cmd.decision()));
    }
}
