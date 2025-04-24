package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.GroupMemberRepository;
import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.groupManage.entity.GroupRole;

/**
 * {@link GroupMemberReader} 에 대한 단위 테스트입니다.
 *
 * <p>성공 케이스와 예외 케이스에 대한 테스트입니다.
 *
 * @author s0o0bn
 * @see GroupMemberReader
 * @since 2025-04-19
 */
@ExtendWith(MockitoExtension.class)
public class GroupMemberReaderTest {

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private GroupMemberReader groupMemberReader;

    @Test
    void getTopNMemberProfiles_success() {
        // given
        Long groupId = 1L;
        int limit = 2;
        List<GroupMemberProfileDto> profiles = List.of(
                new GroupMemberProfileDto(1L, "name", "imageUrl url", GroupRole.LEADER),
                new GroupMemberProfileDto(2L, "name2", "imageUrl url", GroupRole.MEMBER));

        given(groupMemberRepository.findTopNMemberByJoinedAt(groupId, limit)).willReturn(profiles);

        // when
        List<GroupMemberProfileDto> actual = groupMemberReader.getTopNMemberProfiles(groupId, limit);

        // then
        assertThat(actual).isEqualTo(profiles);
    }
}
