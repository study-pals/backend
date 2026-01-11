package com.studypals.domain.fileManage.dto;

import com.studypals.domain.fileManage.entity.FileType;

public record PresignedUrlReq(String fileName, FileType type) {}
