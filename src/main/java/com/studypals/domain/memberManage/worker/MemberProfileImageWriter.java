package com.studypals.domain.memberManage.worker;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.dao.MemberProfileImageRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.entity.MemberProfileImage;
import com.studypals.global.annotations.Worker;
import com.studypals.global.file.FileUtils;
import com.studypals.global.file.entity.ImageStatus;

/**
 * 회원 프로필 이미지의 메타데이터를 데이터베이스에 저장하는 역할을 전담하는 Worker 클래스입니다.
 * <p>
 * 이 클래스는 CQRS(Command Query Responsibility Segregation) 패턴의 'Command' 측면을 담당하며,
 * 시스템의 상태를 변경하는 '쓰기(Write)' 작업에만 집중합니다.
 * {@link Transactional} 어노테이션을 통해 데이터 저장 작업의 원자성을 보장합니다.
 *
 * @author sleepyhoon
 * @since 2024-01-15
 * @see MemberProfileImage
 * @see MemberProfileImageRepository
 */
@Worker
@RequiredArgsConstructor
public class MemberProfileImageWriter {
    private final MemberProfileImageRepository memberProfileImageRepository;

    /**
     * 회원 프로필 이미지의 메타데이터를 생성하고 데이터베이스에 저장합니다.
     * <p>
     * 이 메서드는 클라이언트가 Presigned URL을 통해 스토리지에 파일을 업로드하기 <b>전</b>에 호출됩니다.
     * 파일이 실제로 업로드되기 전에 데이터베이스에 해당 파일의 존재를 미리 기록하는 역할을 합니다.
     *
     * @param member 프로필 이미지가 속한 회원 엔티티
     * @param objectKey 스토리지에 저장될 파일의 고유 객체 키
     * @param fileName 원본 파일의 이름
     * @return 데이터베이스에 저장된 {@link MemberProfileImage}의 고유 ID
     */
    @Transactional
    public Long save(Member member, String objectKey, String fileName) {
        String extension = FileUtils.extractExtension(fileName);

        MemberProfileImage savedImage = memberProfileImageRepository.save(MemberProfileImage.builder()
                .member(member)
                .objectKey(objectKey)
                .originalFileName(fileName)
                .mimeType(extension)
                .imageStatus(ImageStatus.PENDING) // 비동기 리사이징 대기 상태로 저장
                .build());

        member.setProfileImage(savedImage);

        return savedImage.getId();
    }
}
