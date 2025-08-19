package com.studypals.domain.studyManage.worker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.entity.DateType;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * {@link StudyCategoryWriter} 에 대한 단위 테스트입니다.
 *
 *
 * @author jack8
 * @see StudyCategoryWriter
 * @since 2025-08-15
 */
@ExtendWith(MockitoExtension.class)
class StudyCategoryWriterTest {

    @Mock
    private StudyCategoryRepository studyCategoryRepository;

    @InjectMocks
    private StudyCategoryWriter studyCategoryWriter;

    @Test
    void update_sccess() {
        // given
        StudyCategory studyCategory = StudyCategory.builder()
                .id(1L)
                .color("#FFFFF")
                .dayBelong(123)
                .name("name")
                .description("description")
                .dateType(DateType.DAILY)
                .goal(6600L)
                .studyType(StudyType.PERSONAL)
                .build();

        String newColor = "#11111";
        int newDayBelong = 1;

        // when
        StudyCategory newCategory = studyCategoryWriter
                .update(studyCategory)
                .color(newColor)
                .dayBelong(newDayBelong)
                .description(null)
                .build();

        // then
        assertThat(newCategory.getName()).isEqualTo("name");
        assertThat(newCategory.getColor()).isEqualTo(newColor);
        assertThat(newCategory.getDayBelong()).isEqualTo(newDayBelong);
        assertThat(newCategory.getDescription()).isEqualTo("no content");
    }
}
