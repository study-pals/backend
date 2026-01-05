package com.studypals.domain.groupManage.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.GroupMemberRankingDto;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;
import com.studypals.domain.groupManage.worker.GroupAuthorityValidator;
import com.studypals.domain.groupManage.worker.GroupMemberReader;
import com.studypals.domain.groupManage.worker.GroupRankingWorker;

/**
 * groupRankingService 구현 클래스입니다.
 *
 * <p>group 랭킹에 대한 전반적인 로직을 수행합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link GroupRankingService} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * Service
 *
 * @author sleepyhoon
 * @see GroupRankingService
 * @since 2025-12-27
 */
@Service
@RequiredArgsConstructor
public class GroupRankingServiceImpl implements GroupRankingService {
    private final GroupRankingWorker groupRankingWorker;
    private final GroupMemberReader groupMemberReader;
    private final GroupAuthorityValidator validator;

    @Override
    public List<GroupMemberRankingDto> getGroupRanking(Long userId, Long groupId, GroupRankingPeriod period) {
        // 해당 유저가 속한 그룹인가?
        validator.isMemberOfGroup(userId, groupId);

        List<GroupMember> groupMembers = groupMemberReader.getAllMemberProfiles(groupId);

        Map<String, String> groupRanking = groupRankingWorker.getGroupRanking(groupMembers, period);

        return groupMembers.stream()
                .map(groupMember -> {
                    // Redis에 값이 없으면 0으로 처리
                    String timeStr = groupRanking.getOrDefault(
                            String.valueOf(groupMember.getMember().getId()), "0");
                    long studySeconds = Long.parseLong(timeStr);

                    return new GroupMemberRankingDto(
                            groupMember.getMember().getId(),
                            groupMember.getMember().getNickname(),
                            groupMember.getMember().getImageUrl(),
                            studySeconds,
                            groupMember.getRole());
                })
                .toList();
    }
}
