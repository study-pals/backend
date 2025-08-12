package com.studypals.domain.groupManage.api;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dto.CreateCategoryDto;
import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
import com.studypals.domain.studyManage.dto.mappers.CategoryMapper;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.domain.studyManage.service.StudyCategoryService;
import com.studypals.global.responses.Response;

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
 * @since 2025-08-11
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/groups/categories")
public class GroupCategoryController {

    private final StudyCategoryService studyCategoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody CreateCategoryReq req) {

        CreateCategoryDto dto = categoryMapper.reqToDto(req, StudyType.GROUP, userId);

        Long categoryId = studyCategoryService.createCategory(userId, dto);
        return ResponseEntity.created(URI.create("/categories/" + categoryId)).build();
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long categoryId) {

        studyCategoryService.deleteCategory(userId, categoryId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity<Response<Void>> update(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody UpdateCategoryReq req) {

        Long categoryId = studyCategoryService.updateCategory(userId, req);
        return ResponseEntity.created(URI.create("/categories/" + categoryId)).build();
    }

    @GetMapping
    public ResponseEntity<List<>>
}
