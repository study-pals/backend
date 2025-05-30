package com.studypals.global.redis.redisHashRepository;

import java.util.*;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import com.studypals.global.redis.redisHashRepository.annotations.EnableRedisHashRepositories;

/**
 * {@link EnableRedisHashRepositories} 어노테이션을 해석하여,
 * Redis용 Repository 인터페이스들을 스프링 빈으로 등록하는 Registrar 클래스입니다.
 *
 * <p>Spring Data의 {@code @EnableJpaRepositories}와 동일한 방식으로 작동하며,
 * 명시된 Repository 인터페이스에 대해 {@link RedisHashRepositoryFactoryBean}을 생성하여 빈 정의를 등록합니다.
 *
 * <p><b>동작 개요:</b><br>
 * - {@code @EnableRedisHashRepositories(basePackageClasses = ...)} 에 지정된 인터페이스 목록을 읽음<br>
 * - 각 인터페이스가 {@code Repository<T, ID>} 를 구현했는지 검사<br>
 * - 해당 인터페이스에 대한 FactoryBean 정의를 구성하고 BeanDefinitionRegistry에 등록<br>
 * - 등록된 Bean은 이후 Spring이 FactoryBean을 통해 실제 프록시 객체로 대체함
 *
 * @see EnableRedisHashRepositories
 * @see RedisHashRepositoryFactoryBean
 * @see org.springframework.data.repository.Repository
 *
 * @author jack8
 * @since 2025-05-25
 */
@Component
public class RedisHashRepositoriesRegistrar implements ImportBeanDefinitionRegistrar {

    /**
     * {@code @EnableRedisHashRepositories}에 정의된 Repository 인터페이스들을 처리하여,
     * {@link RedisHashRepositoryFactoryBean}을 기반으로 빈 정의를 등록합니다.
     *
     * @param importingClassMetadata 어노테이션 선언 클래스의 메타정보
     * @param registry               스프링 빈 정의 레지스트리
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        AnnotationAttributes attr = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableRedisHashRepositories.class.getName(), false));
        assert attr != null;

        Class<?>[] targetRepoInterfaces = attr.getClassArray("basePackageClasses");

        for (Class<?> repoIface : targetRepoInterfaces) {
            // Repository<T, ID> 인터페이스가 아니면 예외
            if (!Repository.class.isAssignableFrom(repoIface)) {
                throw new IllegalArgumentException(
                        "[ERROR] " + repoIface.getName() + " does not implement Repository<T, ID>");
            }

            // FactoryBean 등록
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
                            RedisHashRepositoryFactoryBean.class)
                    .addConstructorArgValue(repoIface) // 첫 번째 생성자 인자: 인터페이스 타입
                    .addConstructorArgReference("redisTemplate"); // 두 번째 생성자 인자: RedisTemplate Bean

            GenericBeanDefinition def = (GenericBeanDefinition) builder.getRawBeanDefinition();

            // 해당 빈이 DI 대상으로 자동 등록되도록 지정
            def.setAutowireCandidate(true);

            // FactoryBean이 생성할 대상 타입을 명시 (타입 기반 DI에 필요)
            def.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, repoIface);

            // 최종 빈 등록 (이름은 FQN 기준)
            registry.registerBeanDefinition(repoIface.getName(), def);
        }
    }
}
