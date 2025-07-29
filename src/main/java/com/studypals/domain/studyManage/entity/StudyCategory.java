package com.studypals.domain.studyManage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

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
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
@Getter
public abstract class StudyCategory {

    @Column(name = "name", nullable = false, length = 255)
    protected String name;

    @Column(name = "goal")
    protected Long goal;

    @Column(name = "day_belong", nullable = false, columnDefinition = "INTEGER")
    protected Integer dayBelong;

    @Column(name = "color", nullable = true, length = 9)
    protected String color;

    @Column(name = "description", nullable = true, columnDefinition = "TEXT")
    protected String description;
}
