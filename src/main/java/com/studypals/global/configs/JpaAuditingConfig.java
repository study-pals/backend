package com.studypals.global.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * jpa auditing 에 관한 configuration class
 *
 * @author jack8
 * @since 2025-04-06
 */
@EnableJpaAuditing
@Configuration
public class JpaAuditingConfig {}
