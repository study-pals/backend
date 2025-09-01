package com.studypals.domain.studyManage.worker;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.entity.DateType;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.global.annotations.Worker;
import com.studypals.global.utils.ImageUtils;

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
     * @param studyCategory 갱신하고자 할 카테고리
     * @return 갱신을 도와줄 내부 헬퍼 클래스
     */
    public Updater update(StudyCategory studyCategory) {
        return new Updater(studyCategory);
    }

    /**
     * 카테고리를 삭제합니다. 단, 실제로 삭제하는 것이 아닌  {@link com.studypals.domain.studyManage.entity.StudyType StudyType}
     * 을 {@code StudyType.REMOVED} 혹은 {@code StdudyType.GROUP_REMOVED}로 변경합니다.
     * @param studyCategory 삭제하고자 할 studyCategory
     */
    public void remove(StudyCategory studyCategory) {
        studyCategory.setAsRemoved();
    }

    /**
     * 갱신을 위한 내부 헬퍼 클래스입니다. 체이닝을 통해 명시된 값에 대한 내부 값을 변경합니다.
     *
     */
    @AllArgsConstructor
    public static class Updater {
        private final StudyCategory target;

        // 이름을 변경합니다. 만약 null 이거나 blank 이면 넘어갑니다.
        public Updater name(String name) {
            if (name == null || name.isBlank()) return this;

            target.setName(name);
            return this;
        }

        // dateType 을 변경합니다. 만약 null 이면 넘어갑니다.
        public Updater dateType(DateType dateType) {
            if (dateType == null) return this;

            target.setDateType(dateType);
            return this;
        }

        // 목표 시간을 변경합니다.
        public Updater goal(Long goal) {
            target.setGoal(goal);

            return this;
        }

        // 요일 포함 정보를 변경합니다. null 이거나 올바르지 않은 값으면 넘어갑니다.
        public Updater dayBelong(Integer dayBelong) {
            if (dayBelong == null || dayBelong < -1) return this;
            target.setDayBelong(dayBelong);
            return this;
        }

        // 색을 변경합니다. 만약 null 이면 임의의 숫자로 변경됩니다.
        public Updater color(String color) {
            if (color == null) target.setColor(ImageUtils.randomHexColor());
            else target.setColor(color);

            return this;
        }

        // description 을 변경합니다. 만약 null 이면 "no content"로 변경합니다.
        public Updater description(String description) {
            if (description == null) target.setDescription("no content");
            else target.setDescription(description);

            return this;
        }

        // updater의 종점입니다. 변경 완료된 객체를 반환합니다.
        public StudyCategory build() {
            return target;
        }
    }
}
