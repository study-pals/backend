package com.studypals.domain.memberManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.memberManage.dao.MemberProfileImageRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.entity.MemberProfileImage;
import com.studypals.global.file.entity.ImageStatus;

@ExtendWith(MockitoExtension.class)
class MemberProfileImageWriterTest {

    @InjectMocks
    private MemberProfileImageWriter memberProfileImageWriter;

    @Mock
    private MemberProfileImageRepository memberProfileImageRepository;

    @Test
    @DisplayName("프로필 이미지 메타데이터 저장에 성공하고, Member 객체에 연관관계를 설정한다")
    void save_Success() {
        // given
        Member member = Member.builder().id(1L).build();
        String objectKey = "profile/1/uuid.jpg";
        String fileName = "my-profile.jpg";
        Long expectedImageId = 99L;

        MemberProfileImage expectedImage = MemberProfileImage.builder()
                .id(expectedImageId)
                .member(member)
                .objectKey(objectKey)
                .originalFileName(fileName)
                .mimeType("jpg")
                .imageStatus(ImageStatus.PENDING)
                .build();

        given(memberProfileImageRepository.save(any(MemberProfileImage.class))).willReturn(expectedImage);

        // when
        MemberProfileImage savedImage = memberProfileImageWriter.save(member, objectKey, fileName);

        // then
        assertThat(savedImage.getMember()).isEqualTo(expectedImage.getMember());
        assertThat(savedImage.getObjectKey()).isEqualTo(expectedImage.getObjectKey());
        assertThat(savedImage.getOriginalFileName()).isEqualTo(expectedImage.getOriginalFileName());
        assertThat(savedImage.getMimeType()).isEqualTo(expectedImage.getMimeType());
        assertThat(savedImage.getImageStatus()).isEqualTo(expectedImage.getImageStatus());

        // repository.save()에 전달된 인자 캡처 및 검증
        ArgumentCaptor<MemberProfileImage> imageCaptor = ArgumentCaptor.forClass(MemberProfileImage.class);
        verify(memberProfileImageRepository).save(imageCaptor.capture());
        MemberProfileImage capturedImage = imageCaptor.getValue();

        assertThat(capturedImage.getMember()).isEqualTo(member);
        assertThat(capturedImage.getObjectKey()).isEqualTo(objectKey);
        assertThat(capturedImage.getOriginalFileName()).isEqualTo(fileName);
        assertThat(capturedImage.getMimeType()).isEqualTo("jpg");
        assertThat(capturedImage.getImageStatus()).isEqualTo(ImageStatus.PENDING);
    }
}
