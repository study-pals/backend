package com.studypals.domain.groupManage.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.*;

/**
 * Group 에 대한 엔티티입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code builder} <br>
 * 빌더 패턴을 사용하여 생성합니다. <br>
 *
 * @author s0o0bn
 * @since 2025-04-12
 */
@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "`group`")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "tag", nullable = false)
    private String tag;

    @Builder.Default
    @Column(name = "max_member", nullable = false, columnDefinition = "INTEGER DEFAULT 100")
    private Integer maxMember = 100;

    @Builder.Default
    @Column(name = "total_member", nullable = false, columnDefinition = "INTEGER DEFAULT 1")
    private Integer totalMember = 1;

    @Builder.Default
    @Column(name = "is_open", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isOpen = false;

    @Builder.Default
    @Column(name = "is_approval_required", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isApprovalRequired = false;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDate createdDate;

    public boolean isFullMember() {
        return totalMember.equals(maxMember);
    }
}
