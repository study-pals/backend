package com.studypals.global.configs;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.studypals.global.file.FileProperties;

/**
 * 파일 관련 설정을 담당하는 Configuration 클래스입니다.
 * FileProperties 빈으로 등록하고 활성화합니다.
 */
@Configuration
@EnableConfigurationProperties(FileProperties.class)
public class FileConfig {}
