package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.groupEntryRepository.GroupEntryCodeRedisRepository;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupEntryCode;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;
import com.studypals.global.utils.TimeUtils;

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

    @Mock
    private TimeUtils timeUtils;

    @InjectMocks
    private GroupEntryCodeManager entryCodeManager;

    @Test
    void getOrCreateCode_existing_unlimitedTtl_doesNotReset() {
        // given
        Long groupId = 1L;
        LocalDateTime now = LocalDateTime.of(2026, 1, 8, 12, 0);
        LocalDateTime threshold = now.plusDays(1);

        given(timeUtils.getRawLocalDateTime()).willReturn(now);

        GroupEntryCode existing = GroupEntryCode.builder()
                .groupId(groupId)
                .code("ABC123")
                .ttl(-1L) // 무제한 표시
                .expireAt(null) // 무제한이면 null일 가능성
                .build();

        given(entryCodeRepository.findByGroupId(groupId)).willReturn(Optional.of(existing));

        // when
        GroupEntryCode result = entryCodeManager.getOrCreateCode(groupId);

        // then
        ArgumentCaptor<GroupEntryCode> captor = ArgumentCaptor.forClass(GroupEntryCode.class);
        verify(entryCodeRepository).save(captor.capture());

        GroupEntryCode saved = captor.getValue();
        assertThat(result).isSameAs(existing);
        assertThat(saved.getTtl()).isEqualTo(-1L);
        assertThat(saved.getExpireAt()).isNull(); // 그대로

        verify(entryCodeRepository).findByGroupId(groupId);
    }

    @Test
    void getOrCreateCode_existing_expireAtAfterThreshold_doesNotReset() {
        // given
        Long groupId = 1L;
        LocalDateTime now = LocalDateTime.of(2026, 1, 8, 12, 0);
        LocalDateTime threshold = now.plusDays(1);

        given(timeUtils.getRawLocalDateTime()).willReturn(now);

        GroupEntryCode existing = GroupEntryCode.builder()
                .groupId(groupId)
                .code("ABC123")
                .ttl(7L) // 유한
                .expireAt(threshold.plusDays(3)) // 1일보다 큼(=threshold 이후)
                .build();

        given(entryCodeRepository.findByGroupId(groupId)).willReturn(Optional.of(existing));

        // when
        GroupEntryCode result = entryCodeManager.getOrCreateCode(groupId);

        // then
        ArgumentCaptor<GroupEntryCode> captor = ArgumentCaptor.forClass(GroupEntryCode.class);
        verify(entryCodeRepository).save(captor.capture());

        GroupEntryCode saved = captor.getValue();
        assertThat(result).isSameAs(existing);
        assertThat(saved.getTtl()).isEqualTo(7L);
        assertThat(saved.getExpireAt()).isEqualTo(threshold.plusDays(3)); // 그대로

        verify(entryCodeRepository).findByGroupId(groupId);
    }

    @Test
    void getOrCreateCode_existing_withinOneDay_resetsToOneDay() {
        // given
        Long groupId = 1L;
        LocalDateTime now = LocalDateTime.of(2026, 1, 8, 12, 0);
        LocalDateTime threshold = now.plusDays(1);

        given(timeUtils.getRawLocalDateTime()).willReturn(now);

        GroupEntryCode existing = GroupEntryCode.builder()
                .groupId(groupId)
                .code("ABC123")
                .ttl(3L) // 유한
                .expireAt(now.plusHours(3)) // 1일보다 작음(=threshold 이전)
                .build();

        given(entryCodeRepository.findByGroupId(groupId)).willReturn(Optional.of(existing));

        // when
        GroupEntryCode result = entryCodeManager.getOrCreateCode(groupId);

        // then
        ArgumentCaptor<GroupEntryCode> captor = ArgumentCaptor.forClass(GroupEntryCode.class);
        verify(entryCodeRepository).save(captor.capture());

        GroupEntryCode saved = captor.getValue();
        assertThat(result).isSameAs(existing);
        assertThat(saved.getTtl()).isEqualTo(TimeUtils.getSecondOfDay(1L));
        assertThat(saved.getExpireAt()).isEqualTo(threshold); // 1일로 리셋

        verify(entryCodeRepository).findByGroupId(groupId);
    }

    @Test
    void increaseExpire_success_dayMinusOne() {
        // given
        Long groupId = 1L;
        Long day = -1L;

        LocalDateTime fixedNow = LocalDateTime.of(2026, 1, 8, 12, 0, 0);
        given(timeUtils.getRawLocalDateTime()).willReturn(fixedNow);

        GroupEntryCode existing = GroupEntryCode.builder()
                .groupId(groupId)
                .code("ABC123")
                .ttl(1L)
                .expireAt(fixedNow.plusDays(1))
                .build();

        given(entryCodeRepository.findByGroupId(groupId)).willReturn(Optional.of(existing));

        // when
        entryCodeManager.increaseExpire(groupId, day);

        // then: save 파라미터 직접 검증
        ArgumentCaptor<GroupEntryCode> captor = ArgumentCaptor.forClass(GroupEntryCode.class);
        verify(entryCodeRepository).save(captor.capture());

        GroupEntryCode saved = captor.getValue();
        assertThat(saved.getGroupId()).isEqualTo(groupId);
        assertThat(saved.getTtl()).isEqualTo(-1L); // 코드 그대로면 -1 저장
        assertThat(saved.getExpireAt()).isNull();

        verify(entryCodeRepository).findByGroupId(groupId);
    }

    @Test
    void increaseExpire_success_dayPositive() {
        // given
        Long groupId = 1L;
        Long day = 3L;

        LocalDateTime fixedNow = LocalDateTime.of(2026, 1, 8, 12, 0, 0);
        given(timeUtils.getRawLocalDateTime()).willReturn(fixedNow);

        GroupEntryCode existing = GroupEntryCode.builder()
                .groupId(groupId)
                .code("ABC123")
                .ttl(1L)
                .expireAt(fixedNow.plusDays(1))
                .build();

        given(entryCodeRepository.findByGroupId(groupId)).willReturn(Optional.of(existing));

        // when
        entryCodeManager.increaseExpire(groupId, day);

        // then: save 파라미터 직접 검증
        ArgumentCaptor<GroupEntryCode> captor = ArgumentCaptor.forClass(GroupEntryCode.class);
        verify(entryCodeRepository).save(captor.capture());

        GroupEntryCode saved = captor.getValue();
        assertThat(saved.getGroupId()).isEqualTo(groupId);
        assertThat(saved.getTtl()).isEqualTo(TimeUtils.getSecondOfDay(3L));
        assertThat(saved.getExpireAt()).isEqualTo(fixedNow.plusDays(3));

        verify(entryCodeRepository).findByGroupId(groupId);
    }

    @Test
    void getGroupId_success() {
        // given
        Long groupId = 1L;
        String entryCode = "entry code";
        GroupEntryCode groupEntryCode =
                GroupEntryCode.builder().code(entryCode).groupId(groupId).build();

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
        GroupEntryCode groupEntryCode =
                GroupEntryCode.builder().code(entryCode).groupId(group.getId()).build();

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
        GroupEntryCode groupEntryCode =
                GroupEntryCode.builder().code(entryCode).groupId(2L).build();

        given(entryCodeRepository.findById(entryCode)).willReturn(Optional.of(groupEntryCode));

        // when & then
        assertThatThrownBy(() -> entryCodeManager.validateCodeBelongsToGroup(group, entryCode))
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.GROUP_CODE_INVALID);
    }
}
