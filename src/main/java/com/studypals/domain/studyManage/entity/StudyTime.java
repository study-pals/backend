package com.studypals.domain.studyManage.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import org.springframework.dao.DataIntegrityViolationException;

import lombok.*;

import com.studypals.domain.memberManage.entity.Member;

/**
 * 공부 시간에 대한 엔티티입니다. JPA 에 의해 관리됩니다.
 * <p>
 * 다음과 같은 필드를 가지고 있습니다.
 * <pre>
 *     {@code
 * Long id;
 * Member member;
 * StudyCategory studyCategory;
 * LocalDate studiedDate;
 * Long time;
 *     }
 * </pre>
 * temporaryName 은 사용자가 category 에 포함되지 않은 내용을
 * 임시로 사용할 때 부여되는 이름입니다. 만약 category 가 null 인 경우, 해당
 * 이름을 반환합니다. 둘 중 하나의 값이 존재해야 한다는 강제성을 부여하였습니다.
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
        indexes = {@Index(name = "idx_member_studied_category", columnList = "member_id, studied_at, category_id")})
public class StudyTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private StudyCategory category;

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
        if (this.temporaryName == null && this.category == null) {
            throw new DataIntegrityViolationException("must have value temporary name or category");
        }
    }

    public void addTime(Long time) {
        this.time += time;
    }
}
