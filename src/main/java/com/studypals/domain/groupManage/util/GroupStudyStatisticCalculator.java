package com.studypals.domain.groupManage.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.studypals.domain.groupManage.dto.*;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.studyManage.entity.StudyTime;

/**
 * 그룹의 공부 시간을 조합하고 통계 결과를 계산하는 유틸성 클래스입니다.
 *
 * @author s0o0bn
 * @since 2025-05-21
 */
public class GroupStudyStatisticCalculator {
    private static final int SECONDS_PER_MINUTE = 60;

    /**
     * 그룹의 공부 카테고리(루틴)마다 각 그룹원 별로 공부 누적 시간 총합을 계산합니다.
     *
     * @param members 그룹의 전체 그룹원의 프로필 DTO {@link GroupMemberProfileDto}
     * @param studies 그룹원의 공부 정보 엔티티 {@link StudyTime}
     * @return 그룹 카테고리마다 그룹원 별 공부 시간 총합 DTO {@link GroupTotalStudyDto}
     */
    public static GroupTotalStudyDto sumTotalTimeOfCategory(
            List<GroupMemberProfileDto> members, List<StudyTime> studies) {
        Map<Long, GroupMemberProfileDto> memberMap =
                members.stream().collect(Collectors.toMap(GroupMemberProfileDto::id, Function.identity()));

        Map<Long, Map<GroupMemberProfileDto, Long>> totalStudyMap = new HashMap<>();

        for (StudyTime study : studies) {
            Long categoryId = study.getTypeId();
            GroupMemberProfileDto member = memberMap.get(study.getMember().getId());

            totalStudyMap.computeIfAbsent(categoryId, k -> new HashMap<>()).merge(member, study.getTime(), Long::sum);
        }

        return new GroupTotalStudyDto(totalStudyMap);
    }

    /**
     * 각 카테고리의 목표 달성률을 계산합니다.
     * private 메서드인 {@code getCategorySuccessRate}를 통해 각 카테고리 별 달성률을 계산하며,
     * 해당 로직에서는 카테고리 별 누적 시간이 목표 시간 이상인 그룹원만 달성률 계산에 포함합니다.
     *
     * @param group 목표 달성률을 계산할 그룹 엔티티
     * @param totalStudy 그룹원 별 카테고리에 대한 공부 누적 시간 {@link GroupTotalStudyDto}
     * @param categories 그룹의 공부 카테고리 목록
     * @return 목표 달성률, 달성한 그룹원 프로필을 포함한 카테고리 정보 DTO 목록 {@link DailySuccessRateRes}
     */
    public static List<DailySuccessRateDto> getDailySuccessRate(
            Group group, GroupTotalStudyDto totalStudy, List<GroupStudyCategory> categories) {
        return categories.stream()
                .map(category -> getCategorySuccessRate(category, totalStudy, group.getTotalMember()))
                .toList();
    }

    /**
     * 카테고리의 목표 달성률을 계산합니다.
     *
     * @param category 계산할 카테고리 엔티티
     * @param totalStudy {@link GroupTotalStudyDto}
     * @param totalMember 전체 그룹원 수
     * @return 해당 카테고리의 {@link DailySuccessRateRes}
     */
    private static DailySuccessRateDto getCategorySuccessRate(
            GroupStudyCategory category, GroupTotalStudyDto totalStudy, int totalMember) {
        Map<GroupMemberProfileDto, Long> memberStudiedTime =
                totalStudy.memberTotalStudiedTimePerCategory().getOrDefault(category.getId(), Collections.emptyMap());

        /* 그룹원 별 목표 시간 대비 달성률 합산 */
        double totalRatio = memberStudiedTime.values().stream()
                .mapToDouble(aLong -> {
                    long studyTime = Math.min(aLong, category.getGoal() * SECONDS_PER_MINUTE);
                    return (double) studyTime / (category.getGoal() * SECONDS_PER_MINUTE) / totalMember;
                })
                .sum();

        /* 해당 카테고리를 공부한 그룹원 중 목표 시간 이상 공부한 그룹원만 필터링 */
        List<GroupMemberProfileImageDto> succeedMembers = memberStudiedTime.entrySet().stream()
                .filter(e -> e.getValue() >= category.getGoal() * SECONDS_PER_MINUTE) // 그룹 목표 시간이 분 단위이므로, 초 단위로 변환
                .map(e -> new GroupMemberProfileImageDto(
                        e.getKey().imageUrl(), e.getKey().role()))
                .toList();

        return DailySuccessRateDto.of(category, totalRatio, succeedMembers);
    }
}
