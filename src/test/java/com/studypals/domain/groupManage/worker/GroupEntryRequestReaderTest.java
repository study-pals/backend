package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.studypals.domain.groupManage.dao.GroupEntryRequestRepository;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.request.CommonSortType;
import com.studypals.global.request.Cursor;

@ExtendWith(MockitoExtension.class)
public class GroupEntryRequestReaderTest {

    @Mock
    private GroupEntryRequestRepository entryRequestRepository;

    @Mock
    private Group mockGroup;

    @Mock
    private GroupEntryRequest mockEntryRequest;

    @InjectMocks
    private GroupEntryRequestReader entryRequestReader;

    @Test
    void getById_success() {
        // given
        Long id = 1L;

        given(entryRequestRepository.findById(id)).willReturn(Optional.of(mockEntryRequest));

        // when
        GroupEntryRequest actual = entryRequestReader.getById(id);

        // then
        assertThat(actual).isEqualTo(mockEntryRequest);
    }

    @Test
    void getById_fail_notFound() {
        // given
        Long id = 1L;

        given(entryRequestRepository.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> entryRequestReader.getById(id))
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.GROUP_ENTRY_REQUEST_NOT_FOUND);
    }

    @Test
    void getByGroup_success() {
        // given
        GroupEntryRequest request1 =
                GroupEntryRequest.builder().id(1L).group(mockGroup).build();
        GroupEntryRequest request2 =
                GroupEntryRequest.builder().id(2L).group(mockGroup).build();
        GroupEntryRequest request3 =
                GroupEntryRequest.builder().id(3L).group(mockGroup).build();
        List<GroupEntryRequest> requests = List.of(request1, request2, request3);

        Long groupId = 1L;
        Cursor cursor = new Cursor(0, 10, CommonSortType.NEW);

        given(mockGroup.getId()).willReturn(groupId);
        given(entryRequestRepository.findAllByGroupIdWithPagination(groupId, cursor))
                .willReturn(new SliceImpl<>(requests, PageRequest.of(0, requests.size()), false));

        // when
        Slice<GroupEntryRequest> slice = entryRequestReader.getByGroup(mockGroup, cursor);

        // then
        assertThat(slice.getContent()).hasSameSizeAs(requests);
        assertThat(slice.hasNext()).isFalse();
    }

    @Test
    void getByGroup_success_empty() {
        Long groupId = 1L;
        Cursor cursor = new Cursor(0, 10, CommonSortType.NEW);

        given(mockGroup.getId()).willReturn(groupId);
        given(entryRequestRepository.findAllByGroupIdWithPagination(groupId, cursor))
                .willReturn(new SliceImpl<>(List.of()));

        // when
        Slice<GroupEntryRequest> slice = entryRequestReader.getByGroup(mockGroup, cursor);

        // then
        assertThat(slice.getContent()).isEmpty();
        assertThat(slice.hasNext()).isFalse();
    }
}
