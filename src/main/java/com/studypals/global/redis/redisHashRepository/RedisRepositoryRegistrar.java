package com.studypals.global.redis.redisHashRepository;

import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import com.studypals.global.redis.redisHashRepository.annotations.EnableRedisHashRepositories;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-05-26
 */
public class RedisRepositoryRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata md, BeanDefinitionRegistry registry) {
        Map<String, Object> attr = md.getAnnotationAttributes(EnableRedisHashRepositories.class.getName());

        Class<?>[] target = (Class<?>[]) attr.get("basePackageClasses");

        for (Class<?> repoInf : target) {
            BeanDefinitionBuilder b = BeanDefinitionBuilder.genericBeanDefinition(RedisHashRepositoryFactoryBean.class)
                    .addConstructorArgValue(repoInf)
                    .addConstructorArgReference("redisTemplate");

            registry.registerBeanDefinition(repoInf.getSimpleName(), b.getBeanDefinition());
        }
    }
}
