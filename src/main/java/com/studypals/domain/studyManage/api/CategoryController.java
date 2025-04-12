package com.studypals.domain.studyManage.api;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
import com.studypals.domain.studyManage.service.StudyCategoryService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;

/**
 * category 에 대한 컨트롤러입니다. 담당하는 엔드포인트는 다음과 같습니다.
 * <pre>
 *     - POST /category                   : 카테고리 생성({@link CreateCategoryReq})
 *     - DELETE /category/{categoryId}    : 카테고리 제거
 *     - DELETE /category/all             : 카테고리 전부 제거
 *     - PUT /category                    : 카테고리 수정({@link UpdateCategoryReq})
 *     - GET /category                    : 카테고리 정보 요청
 * </pre>
 *
 * @author jack8
 * @since 2025-04-12
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {

    private final StudyCategoryService studyCategoryService;

    @PostMapping
    public ResponseEntity<Response<Long>> create(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody CreateCategoryReq req) {

        Long categoryId = studyCategoryService.createCategory(userId, req);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.STUDY_CATEGORY_ADD, categoryId));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Response<Void>> delete(@AuthenticationPrincipal Long userId, @PathVariable Long categoryId) {

        studyCategoryService.deleteCategory(userId, categoryId);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.STUDY_CATEGORY_DELETE));
    }

    @DeleteMapping("/all")
    public ResponseEntity<Response<Void>> deleteAll(@AuthenticationPrincipal Long userId) {

        studyCategoryService.initCategory(userId);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.STUDY_CATEGORY_DELETE));
    }

    @PutMapping
    public ResponseEntity<Response<Void>> update(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody UpdateCategoryReq req) {
        studyCategoryService.updateCategory(userId, req);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.STUDY_CATEGORY_UPDATE));
    }

    @GetMapping
    public ResponseEntity<Response<List<GetCategoryRes>>> read(@AuthenticationPrincipal Long userId) {

        List<GetCategoryRes> res = studyCategoryService.getUserCategory(userId);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.STUDY_CATEGORY_LIST, res));
    }
}
