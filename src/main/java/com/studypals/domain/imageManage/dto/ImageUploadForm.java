package com.studypals.domain.imageManage.dto;

import org.springframework.web.multipart.MultipartFile;

import com.studypals.domain.imageManage.entity.ImagePurpose;
import com.studypals.domain.imageManage.entity.SizeType;

public record ImageUploadForm(MultipartFile image, ImagePurpose purpose, SizeType sizeType) {}
