package com.studypals.global.file;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "file.upload")
public record FileProperties(@NotEmpty List<String> extensions, @Positive int presignedUrlExpireTime) {}
