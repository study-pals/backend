package com.studypals.domain.imageManage.dto;

import com.studypals.domain.imageManage.entity.ImagePurpose;
import com.studypals.domain.imageManage.entity.SizeType;

public record ImageDto(Long id, String destination, SizeType sizeType, ImagePurpose purpose, byte[] contents) {}
