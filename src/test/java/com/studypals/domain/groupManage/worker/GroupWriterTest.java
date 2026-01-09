package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.GroupRepository;
import com.studypals.domain.groupManage.dao.GroupTagRepository;
import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.dto.mappers.GroupMapper;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * {@link GroupWriter} 에 대한 단위 테스트입니다.
 *
 * <p>성공 케이스와 예외 케이스에 대한 테스트입니다.
 *
 * @author s0o0bn
 * @see GroupWriter
 * @since 2025-04-15
 */
@ExtendWith(MockitoExtension.class)
public class GroupWriterTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupTagRepository groupTagRepository;

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private Group mockGroup;

    @InjectMocks
    private GroupWriter groupWriter;

    @Test
    void create_success() {
        // given
        CreateGroupReq req =
                new CreateGroupReq("group name", "group tag", 10, false, false, "image.example.com", List.of());

        given(groupMapper.toEntity(req)).willReturn(mockGroup);
        given(groupTagRepository.existsById(req.tag())).willReturn(true);

        // when
        Group actual = groupWriter.create(req);

        // then
        assertThat(actual).isEqualTo(mockGroup);
    }

    @Test
    void create_fail_tagNotFound() {
        // given
        GroupErrorCode errorCode = GroupErrorCode.GROUP_CREATE_FAIL;
        CreateGroupReq req =
                new CreateGroupReq("group name", "group tag", 10, false, false, "image.example.com", List.of());

        given(groupMapper.toEntity(req)).willReturn(mockGroup);
        given(groupTagRepository.existsById(req.tag())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> groupWriter.create(req))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void create_fail_whileSave() {
        // given
        GroupErrorCode errorCode = GroupErrorCode.GROUP_CREATE_FAIL;
        CreateGroupReq req =
                new CreateGroupReq("group name", "group tag", 10, false, false, "image.example.com", List.of());

        given(groupMapper.toEntity(req)).willReturn(mockGroup);
        given(groupTagRepository.existsById(req.tag())).willReturn(true);
        given(groupRepository.save(mockGroup)).willThrow(new GroupException(errorCode));

        // when & then
        assertThatThrownBy(() -> groupWriter.create(req))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
