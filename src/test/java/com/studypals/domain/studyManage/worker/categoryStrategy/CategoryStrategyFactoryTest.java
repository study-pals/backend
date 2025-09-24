package com.studypals.domain.studyManage.worker.categoryStrategy;

import com.studypals.domain.studyManage.entity.StudyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * {@link CategoryStrategyFactory} 에 대한 테스트 코드 입니다.
 *
 * @author jack8
 */
@ExtendWith(MockitoExtension.class)
class CategoryStrategyFactoryTest {

    @Mock
    private PersonalCategoryStrategy personalSt;

    @Mock
    private GroupCategoryStrategy groupSt;

    @Mock
    private RemovedCategoryStrategy removeSt;

    private CategoryStrategyFactory categoryStrategyFactory;

    @BeforeEach
    void initInjection() {


        categoryStrategyFactory = new CategoryStrategyFactory(List.of(personalSt, groupSt, removeSt));
    }

    @Test
    void resolve_success() {
        //given
        given(personalSt.supports(StudyType.PERSONAL)).willReturn(true);

        //when
        CategoryStrategy strategy = categoryStrategyFactory.resolve(StudyType.PERSONAL);

        //then
        assertThat(strategy).isEqualTo(personalSt);
    }
    @Test
    void getTypeMap_success() {
        //given
        Long userId = 1L;
        Map<StudyType, List<Long>> personalMap = Map.of(StudyType.PERSONAL, List.of(userId));
        Map<StudyType, List<Long>> removedMap = Map.of(StudyType.REMOVED, List.of(userId));
        Map<StudyType, List<Long>> groupMap = Map.of(StudyType.GROUP, List.of(3L, 5L, 7L));

        Map<StudyType, List<Long>> expected = new HashMap<>();

        expected.putAll(personalMap);
        expected.putAll(removedMap);
        expected.putAll(groupMap);

        given(personalSt.getMapByUserId(userId)).willReturn(personalMap);
        given(removeSt.getMapByUserId(userId)).willReturn(removedMap);
        given(groupSt.getMapByUserId(userId)).willReturn(groupMap);

        //when
        Map<StudyType, List<Long>> res = categoryStrategyFactory.getTypeMap(userId);

        //then
        assertThat(res).isEqualTo(expected);
    }



    private void initStrategyValue() {
        given(groupSt.supports(StudyType.GROUP)).willReturn(true);
        given(personalSt.supports(StudyType.PERSONAL)).willReturn(true);
        given(removeSt.supports(StudyType.REMOVED)).willReturn(true);
    }
}