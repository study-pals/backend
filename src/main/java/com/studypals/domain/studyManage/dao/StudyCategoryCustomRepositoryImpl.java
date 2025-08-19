package com.studypals.domain.studyManage.dao;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studypals.domain.studyManage.entity.QStudyCategory;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyType;

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
 * @since 2025-08-01
 */
@RequiredArgsConstructor
public class StudyCategoryCustomRepositoryImpl implements StudyCategoryCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<StudyCategory> findByTypeMap(Map<StudyType, List<Long>> typeListMap) {
        QStudyCategory studyCategory = QStudyCategory.studyCategory;

        BooleanBuilder builder = new BooleanBuilder();

        for (Map.Entry<StudyType, List<Long>> entry : typeListMap.entrySet()) {
            StudyType type = entry.getKey();
            List<Long> ids = entry.getValue();

            if (ids == null || ids.isEmpty()) continue;

            builder.or(studyCategory.studyType.eq(type).and(studyCategory.typeId.in(ids)));
        }
        return queryFactory.selectFrom(studyCategory).where(builder).fetch();
    }
}
