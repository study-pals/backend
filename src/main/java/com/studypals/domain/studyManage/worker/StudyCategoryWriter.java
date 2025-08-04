package com.studypals.domain.studyManage.worker;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.entity.DateType;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.global.annotations.Worker;
import com.studypals.global.utils.ImageUtils;

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
@Worker
@RequiredArgsConstructor
public class StudyCategoryWriter {

    private final StudyCategoryRepository studyCategoryRepository;

    public void save(StudyCategory studyCategory) {
        studyCategoryRepository.save(studyCategory);
    }

    public Updater update(StudyCategory studyCategory) {
        return new Updater(studyCategory);
    }

    public void delete(StudyCategory studyCategory) {
        studyCategoryRepository.delete(studyCategory);
    }

    @AllArgsConstructor
    public static class Updater {
        private final StudyCategory target;

        public Updater name(String name) {
            if (name == null) return this;

            target.setName(name);
            return this;
        }

        public Updater dateType(DateType dateType) {
            if (dateType == null) return this;

            target.setDateType(dateType);
            return this;
        }

        public Updater goal(Long goal) {
            if (goal == null) target.setGoal(-1L);
            else target.setGoal(goal);

            return this;
        }

        public Updater dayBelong(Integer dayBelong) {
            if (dayBelong == null || dayBelong < -1) return this;
            target.setDayBelong(dayBelong);
            return this;
        }

        public Updater color(String color) {
            if (color == null) target.setColor(ImageUtils.randomHexColor());
            else target.setColor(color);

            return this;
        }

        public Updater description(String description) {
            if (description == null) target.setDescription("no content");
            else target.setDescription(description);

            return this;
        }

        public StudyCategory build() {
            return target;
        }
    }
}
