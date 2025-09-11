package com.grandma.ansimbank.approval;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.LocalDateTime; import java.util.Objects;

@Entity @Table(name="approval")
@Getter @Setter
public class Approval {
    public enum Status { PENDING, APPROVED, REJECTED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false) private Long childId;
    @Column(nullable=false) private Long parentId;
    @Column(nullable=false) private Long amount;
    @Column(length=200) private String memo;

    @Enumerated(EnumType.STRING) @Column(nullable=false, length=10)
    private Status status = Status.PENDING;

    @Column(nullable=false, updatable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static Approval create(Long childId, Long parentId, Long amount, String memo) {
        var a = new Approval(); a.childId=childId; a.parentId=parentId; a.amount=amount; a.memo=memo;
        a.status = Status.PENDING; return a;
    }
    public void decide(Long actorParentId, boolean approve) {
        if (!Objects.equals(actorParentId, this.parentId)) throw new IllegalStateException("권한 없음");
        if (this.status != Status.PENDING) throw new IllegalStateException("이미 처리됨");
        this.status = approve ? Status.APPROVED : Status.REJECTED;
    }
}
