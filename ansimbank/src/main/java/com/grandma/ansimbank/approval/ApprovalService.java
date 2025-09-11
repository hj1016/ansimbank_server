package com.grandma.ansimbank.approval;

import com.grandma.ansimbank.approval.Approval;
import com.grandma.ansimbank.approval.ApprovalRepository;
import com.grandma.ansimbank.approval.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service @RequiredArgsConstructor
public class ApprovalService {
    private final ApprovalRepository repo;
    private final SimpMessagingTemplate template;

    @Transactional
    public Long requestApproval(Long childId, Long parentId, Long amount, String memo) {
        var approval = Approval.create(childId, parentId, amount, memo);
        repo.save(approval);

        var view = Map.of(
            "approvalId", approval.getId(), "childId", childId,
            "parentId", parentId, "amount", approval.getAmount(),
            "memo", approval.getMemo(), "status", approval.getStatus().name()
        );
        template.convertAndSendToUser(String.valueOf(parentId), "/queue/approvals", view);
        return approval.getId();
    }

    @Transactional
    public void decide(Long parentId, Long approvalId, boolean approve) {
        var approval = repo.findByIdForUpdate(approvalId)
            .orElseThrow(() -> new IllegalArgumentException("approval not found"));
        approval.decide(parentId, approve);
        repo.save(approval);

        var result = Map.of(
            "approvalId", approval.getId(), "childId", approval.getChildId(),
            "parentId", approval.getParentId(), "result", approval.getStatus().name()
        );
        template.convertAndSendToUser(String.valueOf(approval.getChildId()), "/queue/approval-result", result);
        template.convertAndSendToUser(String.valueOf(approval.getParentId()), "/queue/approvals", result);
    }
}
