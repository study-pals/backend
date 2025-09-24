package com.studypals.domain.studyManage.worker;

import com.studypals.domain.studyManage.dto.UpdateCategoryDto;
import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.global.annotations.Worker;

/**
 * StudyCategory 에 대한 쓰기 전용 워커 클래스. 갱신 및 저장에 대한 로직을 담고 있다.
 * 갱신의 경우 update 메서드를 통해 임의의 빌더 패턴을 구성하였다.
 *
 * @author jack8
 * @see StudyCategoryRepository
 * @since 2025-08-01
 */
@Worker
@RequiredArgsConstructor
public class StudyCategoryWriter {

    private final StudyCategoryRepository studyCategoryRepository;

    /**
     * 새로운 studyCategory 를 저장합니다.
     * @param studyCategory 저장할 엔티티
     */
    public void save(StudyCategory studyCategory) {
        studyCategoryRepository.save(studyCategory);
    }

    /**
     * 갱신을 위한 빌더 패턴의 시작점 메서드입니다. Updater 내부 클래스를 호출하여 체이닝을 시작합니다.
     * @param category 갱신하고자 할 카테고리
     * @param req 사용자가 변경을 원하는 데이터
     * @return 갱신을 도와줄 내부 헬퍼 클래스
     */
    public void update(StudyCategory category, UpdateCategoryDto req) {
        category.update(req);
    }

    /**
     * 카테고리를 삭제합니다. 단, 실제로 삭제하는 것이 아닌  {@link com.studypals.domain.studyManage.entity.StudyType StudyType}
     * 을 {@code StudyType.REMOVED} 혹은 {@code StdudyType.GROUP_REMOVED}로 변경합니다.
     * @param studyCategory 삭제하고자 할 studyCategory
     */
    public void remove(StudyCategory studyCategory) {
        studyCategory.setAsRemoved();
    }
}
