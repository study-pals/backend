package com.studypals.domain.studyManage.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import org.springframework.dao.DataIntegrityViolationException;

import lombok.*;

import com.studypals.domain.memberManage.entity.Member;

/**
 * 공부 시간을 나타내는 JPA entity 클래스입니다.
 * study_time 테이블과 매핑됩니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code Builder}  <br>
 * builder 패턴을 통해 생성합니다. <br>
 *
 * @author jack8
 * @since 2025-04-10
 */
@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "study_time",
        indexes = {@Index(name = "idx_member_studied", columnList = "member_id, studied_at")})
public class StudyTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "study_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private StudyType studyType;

    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "temporary_name", nullable = true, length = 255)
    private String temporaryName;

    @Column(name = "studied_at", nullable = false)
    private LocalDate studiedDate;

    @Column(name = "time", nullable = false)
    @Builder.Default
    private Long time = 0L;

    @PrePersist
    @PreUpdate
    private void validateTemporaryOrCategory() {
        if (this.temporaryName == null && this.typeId == null) {
            throw new DataIntegrityViolationException("must have value temporary name or typeId");
        }
    }

    public void addTime(Long time) {
        this.time += time;
    }
}
