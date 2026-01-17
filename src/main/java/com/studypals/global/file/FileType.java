package com.studypals.global.file;

import com.studypals.global.file.entity.ImageType;

/**
 * 시스템에서 다루는 모든 파일의 종류를 나타내기 위한 최상위 마커(Marker) 인터페이스입니다.
 * <p>
 * 이 인터페이스는 내부에 메서드를 가지지 않으며, 오직 타입을 그룹화하는 용도로만 사용됩니다.
 * {@link ImageType}과 같이 파일을 종류별로 구분하는 모든 열거형(Enum)은 이 인터페이스를 구현해야 합니다.
 * <p>
 * <b>설계 의도:</b>
 * <ul>
 *     <li><b>타입 안전성 및 다형성:</b> 서로 다른 파일 타입 Enum들을 공통된 {@code FileType}으로 다룰 수 있게 합니다.</li>
 *     <li><b>확장성:</b> 향후 'LogType' 등 새로운 파일 종류가 추가되더라도,
 *     이 인터페이스를 구현함으로써 기존의 파일 관리 메커니즘(예: 전략 패턴)에 쉽게 통합될 수 있습니다.</li>
 * </ul>
 * 예를 들어, {@code ImageFileServiceImpl}에서는 {@code Map<FileType, AbstractFileManager>} 구조를 사용하여
 * 파일 타입에 따라 적절한 Manager를 동적으로 선택합니다.
 *
 * @author sleepyhoon
 * @since 2026-01-10
 * @see ImageType
 * @see com.studypals.global.file.dao.AbstractFileManager
 */
public interface FileType {}
