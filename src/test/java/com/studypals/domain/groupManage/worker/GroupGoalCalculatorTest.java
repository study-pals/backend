package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dto.GroupCategoryGoalDto;
import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.groupManage.dto.GroupTotalGoalDto;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.global.utils.TimeUtils;

@ExtendWith(MockitoExtension.class)
class GroupGoalCalculatorTest {

    @InjectMocks
    private GroupGoalCalculator groupGoalCalculator;

    @Mock
    private StudyTimeRepository studyTimeRepository;

    @Mock
    private StudyCategoryRepository studyCategoryRepository;

    @Mock
    private TimeUtils timeUtils;

    private final LocalDate TODAY = LocalDate.of(2025, 12, 12);
    private final Long GROUP_ID = 100L;

    // 테스트에 필요한 고정 데이터
    private List<GroupMemberProfileDto> profiles;
    private List<StudyCategory> categories;
    private List<Long> memberIds;
    private final Long categoryId1 = 1L;
    private final Long categoryId2 = 2L;
    private final Long categoryId3 = 3L;

    @BeforeEach
    void setUp() {
        when(timeUtils.getToday()).thenReturn(TODAY);

        profiles = List.of(
                new GroupMemberProfileDto(10L, "UserA", "url_a", GroupRole.MEMBER),
                new GroupMemberProfileDto(20L, "UserB", "url_b", GroupRole.MEMBER));
        memberIds = List.of(10L, 20L);

        // categoryId1: 목표 시간 60분
        StudyCategory category1 = StudyCategory.builder()
                .id(categoryId1)
                .name("CS Study")
                .goal(60L)
                .build();
        // categoryId2: 목표 시간 120분
        StudyCategory category2 = StudyCategory.builder()
                .id(categoryId2)
                .name("Algorithm")
                .goal(120L)
                .build();
        // categoryId3: 목표 시간 0분
        StudyCategory category3 = StudyCategory.builder()
                .id(categoryId3)
                .name("FailCategory")
                .goal(0L)
                .build();
        categories = List.of(category1, category2, category3);

        when(studyCategoryRepository.findByStudyTypeAndTypeId(StudyType.GROUP, GROUP_ID))
                .thenReturn(categories);
    }

    @Test
    @DisplayName("성공 케이스: 모든 StudyTime을 가져와 정확한 달성률을 계산하고 반환한다")
    void calculateGroupGoals_ShouldReturnAccuratePercentages() throws Exception {
        // Given - StudyTime 데이터 설정
        Member member1 = Member.builder()
                .id(10L)
                .username("username1")
                .password("password1")
                .nickname("nickname1")
                .build();
        Member member2 = Member.builder()
                .id(20L)
                .username("username2")
                .password("password2")
                .nickname("nickname2")
                .build();
        List<StudyTime> studyTimes = List.of(
                // Category 1 (CS Study)
                StudyTime.builder()
                        .id(1001L)
                        .member(member1)
                        .time(20L)
                        .studyCategory(categories.get(0))
                        .studiedDate(TODAY)
                        .build(),
                StudyTime.builder()
                        .id(1002L)
                        .member(member2)
                        .time(10L)
                        .studyCategory(categories.get(0))
                        .studiedDate(TODAY)
                        .build(),

                // Category 2 (Algorithm)
                StudyTime.builder()
                        .id(2001L)
                        .member(member1)
                        .time(100L)
                        .studyCategory(categories.get(1))
                        .studiedDate(TODAY)
                        .build(),
                StudyTime.builder()
                        .id(2002L)
                        .member(member2)
                        .time(100L)
                        .studyCategory(categories.get(1))
                        .studiedDate(TODAY)
                        .build());

        // Mocking StudyTimeRepository 호출
        when(studyTimeRepository.findByMemberIdInAndDateAndCategoryIdIn(eq(memberIds), eq(TODAY), any()))
                .thenReturn(studyTimes);

        // When
        // ✨ 반환 타입 변경: GroupTotalGoalDto
        GroupTotalGoalDto result = groupGoalCalculator.calculateGroupGoals(GROUP_ID, profiles);
        List<GroupCategoryGoalDto> results = result.categoryGoals();

        // Then
        assertThat(results).hasSize(3);

        // Category 1 검증 (25%)
        assertThat(results).contains(new GroupCategoryGoalDto(categoryId1, 60L, "CS Study", 25));

        // Category 2 검증 (83%)
        assertThat(results).contains(new GroupCategoryGoalDto(categoryId2, 120L, "Algorithm", 83));

        // Category 3 검증 (목표 0L, 달성 0L -> 0%)
        assertThat(results).contains(new GroupCategoryGoalDto(categoryId3, 0L, "FailCategory", 0));

        // ✨ 전체 평균 검증 (36%)
        assertThat(result.overallAveragePercent()).isEqualTo(36);
    }

    @Test
    @DisplayName("엣지 케이스: 달성률이 100% 초과하거나 목표 시간이 0일 때 올바르게 처리한다")
    void calculateGroupGoals_ShouldHandleOver100PercentAndZeroGoal() {
        Member member1 = Member.builder()
                .id(10L)
                .username("username1")
                .password("password1")
                .nickname("nickname1")
                .build();
        Member member2 = Member.builder()
                .id(20L)
                .username("username2")
                .password("password2")
                .nickname("nickname2")
                .build();

        // Given - StudyTime 데이터 설정
        List<StudyTime> studyTimes = List.of(
                // Category 1 (CS Study) - 100% 초과 상황 유도 (200%)
                StudyTime.builder()
                        .id(1001L)
                        .member(member1)
                        .time(120L)
                        .studyCategory(categories.get(0))
                        .studiedDate(TODAY)
                        .build(),
                StudyTime.builder()
                        .id(1002L)
                        .member(member2)
                        .time(120L)
                        .studyCategory(categories.get(0))
                        .studiedDate(TODAY)
                        .build(),

                // Category 2 (Algorithm) - 41% 달성 유도
                StudyTime.builder()
                        .id(2001L)
                        .member(member1)
                        .time(100L) // A: 100분
                        .studyCategory(categories.get(1)) // 카테고리 2
                        .studiedDate(TODAY)
                        .build(),

                // Category 3 (FailCategory, 목표 0L) - 분모 0 상황 유도
                StudyTime.builder()
                        .id(3001L)
                        .member(member1)
                        .time(100L) // 실제 공부 시간은 있으나 목표가 0
                        .studyCategory(categories.get(2)) // 카테고리 3
                        .studiedDate(TODAY)
                        .build());

        // Mocking StudyTimeRepository 호출
        when(studyTimeRepository.findByMemberIdInAndDateAndCategoryIdIn(eq(memberIds), eq(TODAY), any()))
                .thenReturn(studyTimes);

        // When
        // ✨ 반환 타입 변경: GroupTotalGoalDto
        GroupTotalGoalDto result = groupGoalCalculator.calculateGroupGoals(GROUP_ID, profiles);
        List<GroupCategoryGoalDto> results = result.categoryGoals();

        // Then
        assertThat(results).hasSize(3);

        // Category 1 검증 (240 / 60 * 2 = 200% -> 100% 제한)
        assertThat(results).contains(new GroupCategoryGoalDto(categoryId1, 60L, "CS Study", 100));

        // Category 2 검증 (100 / 240 * 100 = 41.66...% -> 41 가정)
        assertThat(results).contains(new GroupCategoryGoalDto(categoryId2, 120L, "Algorithm", 41));

        // Category 3 검증 (분모 0 -> 0% 반환)
        assertThat(results).contains(new GroupCategoryGoalDto(categoryId3, 0L, "FailCategory", 0));

        // ✨ 전체 평균 검증 (47%)
        assertThat(result.overallAveragePercent()).isEqualTo(47);
    }

    /**
     * Case 3: 공부 시간이 전혀 없을 때
     * 모든 카테고리에서 SUM = 0, 달성률 0%
     */
    @Test
    @DisplayName("공부 시간이 0일 때, 0%를 반환해야 한다")
    void calculateGroupGoals_ShouldReturnZeroWhenNoStudyTime() {
        // Given - StudyTime 데이터 없음
        when(studyTimeRepository.findByMemberIdInAndDateAndCategoryIdIn(eq(memberIds), eq(TODAY), any()))
                .thenReturn(Collections.emptyList());

        // When
        // ✨ 반환 타입 변경: GroupTotalGoalDto
        GroupTotalGoalDto result = groupGoalCalculator.calculateGroupGoals(GROUP_ID, profiles);
        List<GroupCategoryGoalDto> results = result.categoryGoals();

        // Then
        assertThat(results).hasSize(3);
        assertThat(results)
                .containsExactlyInAnyOrder( // 순서에 상관없이 포함 여부 확인 (혹시 모를 정렬 이슈 방지)
                        new GroupCategoryGoalDto(categoryId1, 60L, "CS Study", 0),
                        new GroupCategoryGoalDto(categoryId2, 120L, "Algorithm", 0),
                        new GroupCategoryGoalDto(categoryId3, 0L, "FailCategory", 0));

        // ✨ 전체 평균 검증 (0%)
        assertThat(result.overallAveragePercent()).isEqualTo(0);
    }
}
