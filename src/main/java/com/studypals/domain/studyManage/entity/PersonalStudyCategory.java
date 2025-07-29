package com.studypals.domain.studyManage.entity;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.SuperBuilder;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;

/**
 * 공부 카테고리에 대한 엔티티입니다. JPA 에 의해 관리됩니다.
 * <p>
 * 다음과 같은 필드를 가지고 있습니다.
 * <pre>
 *     {@code
 * Long id;
 * Member member;
 * Integer dayBelong;
 * String color;
 * String description;
 *     }
 * </pre>
 *
 * <p><b>주요 생성자:</b><br>
 * {@code Builder}  <br>
 * builder 패턴을 통해 생성합니다. <br>
 *
 * @author jack8
 * @since 2025-04-10
 */
@Entity
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "personal_study_category")
public class PersonalStudyCategory extends StudyCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public void updateCategory(UpdateCategoryReq dto) {
        this.name = dto.name();
        this.dayBelong = dto.dayBelong();
        this.color = dto.color();
        this.description = dto.description();
    }

    public boolean isOwner(Long memberId) {
        return this.member.getId().equals(memberId);
    }
}
