package com.studypals.domain.groupManage.worker;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.studypals.domain.groupManage.dto.*;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.studyManage.dto.GetStudyOfMemberDto;
import com.studypals.global.annotations.Worker;

/**
 * 그룹의 공부 시간에 대한 통계 관련 로직을 담당하는 Worker 입니다.
 *
 * @author s0o0bn
 * @since 2025-05-10
 */
@Worker
public class GroupStudyStatisticManager {

    /**
     * 그룹의 공부 카테고리(루틴)마다 각 그룹원 별로 공부 누적 시간 총합을 계산합니다.
     *
     * @param members 그룹의 전체 그룹원의 프로필 DTO {@link GroupMemberProfileDto}
     * @param studies 그룹원의 공부 정보에 대한 DTO {@link GetStudyOfMemberDto}
     * @return 그룹 카테고리마다 그룹원 별 공부 시간 총합 DTO {@link GroupTotalStudyDto}
     */
    public GroupTotalStudyDto sumTotalTimeOfCategory(
            List<GroupMemberProfileDto> members, List<GetStudyOfMemberDto> studies) {
        Map<Long, GroupMemberProfileDto> memberMap =
                members.stream().collect(Collectors.toMap(GroupMemberProfileDto::id, Function.identity()));

        Map<Long, Map<GroupMemberProfileDto, Long>> totalStudyMap = new HashMap<>();

        for (GetStudyOfMemberDto study : studies) {
            Long categoryId = study.study().typeId();
            GroupMemberProfileDto member = memberMap.get(study.member().getId());

            totalStudyMap
                    .computeIfAbsent(categoryId, k -> new HashMap<>())
                    .merge(member, study.study().time(), Long::sum);
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
    public List<DailySuccessRateDto> getDailySuccessRate(
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
    private DailySuccessRateDto getCategorySuccessRate(
            GroupStudyCategory category, GroupTotalStudyDto totalStudy, int totalMember) {
        Map<GroupMemberProfileDto, Long> memberStudiedTime =
                totalStudy.memberTotalStudiedTimePerCategory().getOrDefault(category.getId(), Collections.emptyMap());

        List<GroupMemberProfileImageDto> succeedMembers = memberStudiedTime.entrySet().stream()
                .filter(e -> e.getValue() >= category.getGoalTime())
                .map(e -> new GroupMemberProfileImageDto(
                        e.getKey().imageUrl(), e.getKey().role()))
                .toList();

        double successRate = (double) succeedMembers.size() / totalMember;

        return DailySuccessRateDto.of(category, successRate, succeedMembers);
    }
}
