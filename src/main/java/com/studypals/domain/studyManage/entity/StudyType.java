package com.studypals.domain.studyManage.entity;

/**
 *
 * {@link StudyTime} 에서 FK 를 대신하여, 다른 엔티티에 대한 느슨한 연관관계를 위한 엔티티 타입을 명시합니다.
 * <p>
 * PERSONAL,GROUP, TEMPORARY 등을 정의합니다.
 *
 * @author jack8
 * @since 2025-05-05
 */
public enum StudyType {
    PERSONAL,
    GROUP,
    TEMPORARY
}
