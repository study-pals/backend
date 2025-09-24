package com.studypals.domain.groupManage.api;

import java.net.URI;
import java.util.List;

import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.ResponseCode;
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
 * 그룹 카테고리에 대한 CRUD 엔드포인트입니다.
 * <pre>
 *     - POST /groups/categories     : 새로운 카테고리를 생성합니다.
 *     - GET /groups/categories/{groupId} : 특정 그룹에 대한 카테고리 정보를 반환합니다.
 *     - DELETE /groups/categories/{categoryId} : 해당하는 카테고리를 약한 삭제합니다.
 *     - PUT /groups/categories : 해당하는 카테고리 정보를 갱신합니다.
 * </pre>
 *
 * @author jack8
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

        CreateCategoryDto dto = categoryMapper.reqToDto(req, StudyType.GROUP, req.groupId());

        Long categoryId = studyCategoryService.createCategory(userId, dto);
        return ResponseEntity.created(URI.create("/groups/categories/" + categoryId)).build();
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<Response<List<GetCategoryRes>>> read(@PathVariable Long groupId) {

        List<GetCategoryRes> res = studyCategoryService.getGroupCategories(groupId);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.STUDY_CATEGORY_LIST, res));
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
        return ResponseEntity.created(URI.create("/groups/categories/" + categoryId)).build();
    }
}
