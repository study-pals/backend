package com.studypals.domain.groupManage.entity;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.SuperBuilder;

import com.studypals.domain.studyManage.entity.StudyCategory;

/**
 * 그룹의 공부 카테고리에 대한 엔티티입니다. JPA 에 의해 관리됩니다.
 * <p>
 * 다음과 같은 필드를 가지고 있습니다.
 * <pre>
 *     {@code
 * Long id;
 * Group group;
 * Integer goalTime;
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
 * @author s0o0bm
 * @since 2025-05-08
 */
@Entity
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "group_study_category")
public class GroupStudyCategory extends StudyCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false)
    private GroupStudyCategoryType type;
}
