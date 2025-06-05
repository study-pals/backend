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

public class GroupEntryRequestCustomMapper {
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
