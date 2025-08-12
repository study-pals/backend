package com.studypals.domain.studyManage.entity;

import jakarta.persistence.*;

import lombok.*;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-07-29
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class StudyCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Setter
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "study_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private StudyType studyType;

    @Setter
    @Column(name = "date_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DateType dateType;

    @Column(name = "type_id", nullable = false)
    private Long typeId;

    @Setter
    @Column(name = "goal")
    private Long goal;

    @Setter
    @Column(name = "day_belong", nullable = false, columnDefinition = "INTEGER")
    private Integer dayBelong;

    @Setter
    @Column(name = "color", nullable = true, length = 9)
    private String color;

    @Setter
    @Column(name = "description", nullable = true, columnDefinition = "TEXT")
    private String description;

    @PrePersist
    public void validateType() {
        if (studyType == StudyType.REMOVED) {
            throw new IllegalArgumentException("studyType " + studyType + " is not allowed");
        }
    }

    public void setAsRemoved() {
        this.studyType = StudyType.REMOVED;
    }
}
