package com.studypals.domain.memberManage.worker;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.dao.MemberProfileImageRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.entity.MemberProfileImage;
import com.studypals.global.annotations.Worker;
import com.studypals.global.file.FileUtils;

@Worker
@RequiredArgsConstructor
public class MemberProfileImageWriter {
    private final MemberProfileImageRepository memberProfileImageRepository;

    @Transactional
    public Long save(Member member, String objectKey, String fileName) {
        String extension = FileUtils.extractExtension(fileName);

        MemberProfileImage savedImage = memberProfileImageRepository.save(MemberProfileImage.builder()
                .member(member)
                .objectKey(objectKey)
                .originalFileName(fileName)
                .mimeType(extension)
                .build());

        return savedImage.getId();
    }
}
