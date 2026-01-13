package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.GroupTagRepository;
import com.studypals.domain.groupManage.dao.groupRepository.GroupRepository;
import com.studypals.domain.groupManage.entity.GroupTag;

/**
 * {@link GroupReader} 에 대한 단위 테스트입니다.
 *
 * <p>성공 케이스와 예외 케이스에 대한 테스트입니다.
 *
 * @author s0o0bn
 * @see GroupReader
 * @since 2025-04-15
 */
@ExtendWith(MockitoExtension.class)
public class GroupReaderTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupTagRepository groupTagRepository;

    @Mock
    private GroupTag mockGroupTag;

    @InjectMocks
    private GroupReader groupReader;

    @Test
    void getGroupTags_success() {
        // given
        List<GroupTag> tags = List.of(mockGroupTag);
        given(groupTagRepository.findAll()).willReturn(tags);

        // when
        List<GroupTag> actual = groupReader.getGroupTags();

        // then
        assertThat(actual).isEqualTo(tags);
    }
}
