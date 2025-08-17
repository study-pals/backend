package com.studypals.domain.imageManage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studypals.domain.imageManage.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {}
