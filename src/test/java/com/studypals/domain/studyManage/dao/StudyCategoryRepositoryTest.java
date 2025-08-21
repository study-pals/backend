package com.studypals.domain.studyManage.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.studypals.domain.studyManage.entity.DateType;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.testModules.testSupport.DataJpaSupport;

/**
 * {@link  StudyCategoryRepository} 에 대한 테스트코드입니다.
 *
 * @author jack8
 * @since 2025-08-21
 */
@DisplayName("StudyCategoryRepository_JPA_test")
class StudyCategoryRepositoryTest extends DataJpaSupport {

    @Autowired
    private StudyCategoryRepository studyCategoryRepository;

    private void createCategory(StudyType studyType, Long typeId, String name) {
        em.persist(StudyCategory.builder()
                .studyType(studyType)
                .typeId(typeId)
                .name(name)
                .dayBelong(127)
                .color("#FFFFFF")
                .goal(3600L)
                .dateType(DateType.DAILY)
                .description("description")
                .build());
    }

    @Test
    void findByStudyTypeAndTypeId_success_returnSize0() {
        // given
        StudyType type = StudyType.PERSONAL;
        Long typeId = 1L;

        createCategory(type, 2L, "name");

        // when
        List<StudyCategory> result = studyCategoryRepository.findByStudyTypeAndTypeId(type, typeId);

        // then
        assertThat(result).hasSize(0);
    }

    @Test
    void findByStudyTypeAndTypeId_success() {
        StudyType type = StudyType.PERSONAL;
        Long typeId = 1L;

        createCategory(type, 1L, "name1");
        createCategory(type, 1L, "name2");
        createCategory(type, 2L, "ignore1");
        createCategory(StudyType.GROUP, 1L, "ignore2");

        // when
        List<StudyCategory> result = studyCategoryRepository.findByStudyTypeAndTypeId(type, typeId);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void findByTypeMap_success() {
        // given
        Map<StudyType, List<Long>> typeMap = Map.of(
                StudyType.GROUP, List.of(53L, 11L, 831L),
                StudyType.PERSONAL, List.of(1L),
                StudyType.REMOVED, List.of(1L));
        createCategory(StudyType.GROUP, 11L, "name1-1");
        createCategory(StudyType.GROUP, 11L, "name1-2");
        createCategory(StudyType.GROUP, 11L, "name1-3");
        createCategory(StudyType.GROUP, 53L, "name2-1");
        createCategory(StudyType.GROUP, 53L, "name2-2");
        createCategory(StudyType.GROUP, 831L, "name3-1");

        createCategory(StudyType.PERSONAL, 1L, "name4-1");
        createCategory(StudyType.PERSONAL, 1L, "name4-2");
        createCategory(StudyType.PERSONAL, 1L, "name4-3");
        createCategory(StudyType.REMOVED, 1L, "name5");

        // when
        List<StudyCategory> result = studyCategoryRepository.findByTypeMap(typeMap);

        // then
        assertThat(result).hasSize(10);
    }
}
