package com.studypals.domain.groupManage.dto;

import java.time.LocalDate;

import com.studypals.domain.memberManage.dto.MemberProfileDto;

public record GroupEntryRequestDto(long requestId, MemberProfileDto member, LocalDate requestedDate) {}
