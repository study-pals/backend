package com.studypals.domain.groupManage.entity;

import jakarta.persistence.*;

import lombok.*;

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
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "group_study_category")
public class GroupStudyCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "goal_time", nullable = false, columnDefinition = "INTEGER")
    private Integer goalTime;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false)
    private GroupStudyCategoryType type;

    @Column(name = "day_belong", nullable = false, columnDefinition = "INTEGER")
    private Integer dayBelong;

    @Column(name = "color", nullable = true, length = 9)
    private String color;

    @Column(name = "description", nullable = true, columnDefinition = "TEXT")
    private String description;
}
