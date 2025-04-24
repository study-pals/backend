package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.GroupEntryRequestRepository;
import com.studypals.domain.groupManage.dto.mappers.GroupEntryRequestMapper;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * {@link GroupEntryRequestWorker} 에 대한 단위 테스트입니다.
 *
 * <p>성공 케이스와 예외 케이스에 대한 테스트입니다.
 *
 * @author s0o0bn
 * @see GroupEntryRequestWorker
 * @since 2025-04-25
 */
@ExtendWith(MockitoExtension.class)
public class GroupEntryRequestWorkerTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GroupEntryRequestRepository entryRequestRepository;

    @Mock
    private GroupEntryRequestMapper entryRequestMapper;

    @Mock
    private Member mockMember;

    @Mock
    private Group mockGroup;

    @Mock
    private GroupEntryRequest mockGroupEntryRequest;

    @InjectMocks
    private GroupEntryRequestWorker entryRequestWorker;

    @Test
    void createRequest_success() {
        // given
        Long userId = 1L;

        given(memberRepository.getReferenceById(userId)).willReturn(mockMember);
        given(entryRequestMapper.toEntity(mockMember, mockGroup)).willReturn(mockGroupEntryRequest);

        // when
        GroupEntryRequest actual = entryRequestWorker.createRequest(userId, mockGroup);

        // then
        assertThat(actual).isEqualTo(mockGroupEntryRequest);
    }

    @Test
    void createRequest_fail_whileSave() {
        // given
        Long userId = 1L;

        given(memberRepository.getReferenceById(userId)).willReturn(mockMember);
        given(entryRequestMapper.toEntity(mockMember, mockGroup)).willReturn(mockGroupEntryRequest);
        given(entryRequestRepository.save(mockGroupEntryRequest))
                .willThrow(new GroupException(GroupErrorCode.GROUP_JOIN_FAIL));

        // when & then
        assertThatThrownBy(() -> entryRequestWorker.createRequest(userId, mockGroup))
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.GROUP_JOIN_FAIL);
    }
}
