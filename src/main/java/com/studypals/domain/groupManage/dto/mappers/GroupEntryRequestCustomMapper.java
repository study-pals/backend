package com.studypals.domain.groupManage.dto.mappers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.studypals.domain.groupManage.dto.GroupEntryRequestDto;
import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.domain.memberManage.dto.MemberProfileDto;
import com.studypals.domain.memberManage.entity.Member;

/**
 * mapstruct 가 아닌 별도의 DTO 매핑 로직을 담당하는 유틸성 클래스입니다.
 * {@code GroupEntryRequest} 관련 DTO 커스텀 매핑 로직을 포함합니다.
 *
 * @author s0o0bn
 * @since 2025-06-05
 */
public class GroupEntryRequestCustomMapper {

    /**
     *
     * @param requests
     * @param members
     * @return
     */
    public static List<GroupEntryRequestDto> map(List<GroupEntryRequest> requests, List<Member> members) {
        Map<Long, Member> memberMap = members.stream().collect(Collectors.toMap(Member::getId, Function.identity()));

        return requests.stream()
                .map(request -> {
                    Member member = Optional.ofNullable(
                                    memberMap.get(request.getMember().getId()))
                            .orElseThrow(() ->
                                    new IllegalStateException("Member not found for request ID: " + request.getId()));
                    return new GroupEntryRequestDto(
                            request.getId(),
                            new MemberProfileDto(member.getId(), member.getNickname(), member.getImageUrl()),
                            request.getCreatedDate());
                })
                .toList();
    }
}
