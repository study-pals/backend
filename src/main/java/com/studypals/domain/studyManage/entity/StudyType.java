package com.studypals.domain.studyManage.entity;

/**
 *
 * {@link StudyTime} 에서 FK 를 대신하여, 다른 엔티티에 대한 느슨한 연관관계를 위한 엔티티 타입을 명시합니다.
 * <p>
 * <pre>
 * PERSONAL: 개인이 직접 설정한 공부 카테고리(토픽) - study_category table과의 연관
 * GROUP: 그룹이 설정한 공부 카테고리(토픽)
 * TEMPORARY: 임시-단 한번의 공부를 위한 카테고리 이름
 * REMOVED: 삭제된 카테고리 항목
 * </pre>
 * @author jack8
 * @since 2025-05-05
 */
public enum StudyType {
    PERSONAL,
    GROUP,
    REMOVED
}
