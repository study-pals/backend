package com.studypals.domain.fileManage.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.fileManage.dao.MemberProfileRepository;
import com.studypals.domain.fileManage.dto.PresignedUrlReq;

@Service
@RequiredArgsConstructor
public class FileService {
    private final MemberProfileRepository memberProfileRepository;

    public String getProfileUploadUrl(PresignedUrlReq request) {
        return memberProfileRepository.getUploadUrl(request.fileName());
    }
}
