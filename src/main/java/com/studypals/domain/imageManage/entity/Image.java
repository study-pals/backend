package com.studypals.domain.imageManage.entity;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "image_metadata")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "destination")
    private String destination;

    @Column(name = "origin_filename_extension")
    @Enumerated(value = EnumType.STRING)
    private FilenameExtension extension;

    @Column(name = "size_type")
    @Enumerated(value = EnumType.STRING)
    private SizeType sizeType;

    @Column(name = "image_purpose")
    @Enumerated(value = EnumType.STRING)
    private ImagePurpose purpose;
}
