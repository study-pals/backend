package com.studypals.domain.studyManage.entity;

import jakarta.persistence.*;

import lombok.*;

/**
 * 공부 카테고리에 대한 엔티티입니다.
 * <p>
 * - 약한 연관관계를 사용하여, 직접 참조하는 관계가 아닌, {@link StudyType} 으로 연관된 테이블 타입을 정의하고 {@code typeId} 로
 * 해당 테이블에서의 id 를 정의합니다. <br>
 * - 카테고리가 삭제되는 경우, 해당 데이터를 삭제하는 것이 아닌, {@link StudyType} 을 REMOVED 로 변경합니다. <br>
 * - StudyType 에 따라 읽기/쓰기 권한 및 검색 전략이 달라집니다. 이는 전략 패턴으로 구성되어 있습니다. <br>
 * - dataType 은 일반적으로 Daily로 사용하며, Weekly 는 존재하나 그 작동 방식이 현재로선 구현되어 있지 않습니다. <br>
 *
 * @author jack8
 * @see StudyType
 * @see com.studypals.domain.studyManage.worker.categoryStrategy.CategoryStrategyFactory CategoryStrategyFactory
 * @see DateType
 * @see com.studypals.domain.studyManage.dao.StudyCategoryRepository StudyCategoryRepository
 * @since 2025-07-29
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "study_category")
public class StudyCategory {

    // Auto Increment type id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    // 해당 카테고리의 이름을 정의합니다.
    @Setter
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    // 해당 카테고리가 개인/그룹/삭제됨 등 인지를 정의합니다. 연관된 테이블이 전략 패턴에서 명시하여 관리됩니다.
    @Column(name = "study_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private StudyType studyType;

    // daily 를 사용합니다. 구현되지 않았습니다.
    @Setter
    @Column(name = "date_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DateType dateType;

    // 연관된 테이블에서의 id 를 가집니다.
    @Column(name = "type_id", nullable = false)
    private Long typeId;

    // 목표 시간에 대한 정보입니다. nullable 하므로 사용자는 목표 시간을 설정하지 않아도 됩니다.
    @Setter
    @Column(name = "goal")
    private Long goal;

    // 어느 요일에 속하는지를 반환합니다.
    @Setter
    @Column(name = "day_belong", nullable = false, columnDefinition = "INTEGER")
    private Integer dayBelong;

    // 해당 카테고리를 표시할 색상을 정의합니다. null 시 매번 다르게 표시됩니다.
    @Setter
    @Column(name = "color", nullable = true, length = 9)
    private String color;

    // 해당 카테고리의 설명을 추가합니다. nullable 합니다.
    @Setter
    @Column(name = "description", nullable = true, columnDefinition = "TEXT")
    private String description;

    // 해당 엔티티를 영속화 하기 전, 검사합니다. REMOVED 타입은 새롭게 만들어질 수 없습니다(갱신을 통해서만 존재합니다)
    @PrePersist
    public void validateType() {
        if (studyType == StudyType.REMOVED || studyType == StudyType.GROUP_REMOVED) {
            throw new IllegalArgumentException("studyType " + studyType + " is not allowed");
        }
    }

    // 해당 엔티티를 REMOVED로 변경하는 메서드
    public void setAsRemoved() {
        if (this.studyType == StudyType.PERSONAL) this.studyType = StudyType.REMOVED;
        else if (this.studyType == StudyType.GROUP) this.studyType = StudyType.GROUP_REMOVED;
    }
}
