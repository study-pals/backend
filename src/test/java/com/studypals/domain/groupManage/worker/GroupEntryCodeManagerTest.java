package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.GroupEntryCodeRedisRepository;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupEntryCode;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * {@link GroupEntryCodeManager} 에 대한 단위 테스트입니다.
 *
 * <p>성공 케이스와 예외 케이스에 대한 테스트입니다.
 *
 * @author s0o0bn
 * @see GroupEntryCodeManager
 * @since 2025-04-16
 */
@ExtendWith(MockitoExtension.class)
public class GroupEntryCodeManagerTest {

    @Mock
    private GroupEntryCodeRedisRepository entryCodeRepository;

    @InjectMocks
    private GroupEntryCodeManager entryCodeManager;

    @Test
    void generate_success() {
        // given
        Long groupId = 1L;

        // when
        String code = entryCodeManager.generate(groupId);

        // then
        assertThat(code).isNotNull();
        assertThat(code).hasSize(6);
        assertThat(code).matches("[A-Z0-9]+");
    }

    @Test
    void getGroupId_success() {
        // given
        Long groupId = 1L;
        String entryCode = "entry code";
        GroupEntryCode groupEntryCode = new GroupEntryCode(entryCode, groupId);

        given(entryCodeRepository.findById(entryCode)).willReturn(Optional.of(groupEntryCode));

        // when
        Long actual = entryCodeManager.getGroupId(entryCode);

        // then
        assertThat(actual).isEqualTo(groupId);
    }

    @Test
    void getGroupId_fail_entryCodeNotFound() {
        // given
        String entryCode = "entry code";
        GroupErrorCode errorCode = GroupErrorCode.GROUP_CODE_NOT_FOUND;

        given(entryCodeRepository.findById(entryCode)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> entryCodeManager.getGroupId(entryCode))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void validateCodeBelongsToGroup_success() {
        // given
        Group group = Group.builder().id(1L).build();
        String entryCode = "entry code";
        GroupEntryCode groupEntryCode = new GroupEntryCode(entryCode, group.getId());

        given(entryCodeRepository.findById(entryCode)).willReturn(Optional.of(groupEntryCode));

        // when & then
        assertThatCode(() -> entryCodeManager.validateCodeBelongsToGroup(group, entryCode))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCodeBelongsToGroup_fail_entryCodeNotEquals() {
        // given
        Group group = Group.builder().id(1L).build();
        String entryCode = "entry code";
        GroupEntryCode groupEntryCode = new GroupEntryCode(entryCode, 2L);

        given(entryCodeRepository.findById(entryCode)).willReturn(Optional.of(groupEntryCode));

        // when & then
        assertThatThrownBy(() -> entryCodeManager.validateCodeBelongsToGroup(group, entryCode))
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.GROUP_CODE_INVALID);
    }
}
