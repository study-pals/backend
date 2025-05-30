package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.GroupEntryRequestRepository;
import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;

@ExtendWith(MockitoExtension.class)
public class GroupEntryRequestReaderTest {

    @Mock
    private GroupEntryRequestRepository entryRequestRepository;

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
}
