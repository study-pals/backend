package com.studypals.domain.groupManage.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.groupManage.api.GroupStudyController;
import com.studypals.domain.groupManage.dto.DailySuccessRateDto;
import com.studypals.domain.groupManage.dto.DailySuccessRateRes;
import com.studypals.domain.groupManage.dto.GroupMemberProfileImageDto;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.domain.groupManage.service.GroupStudyCategoryService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.RestDocsSupport;

@WebMvcTest(GroupStudyController.class)
public class GroupStudyControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean
    private GroupStudyCategoryService groupStudyCategoryService;

    @Test
    @WithMockUser
    void getGroupDailyGoal_success() throws Exception {
        // given
        Long groupId = 1L;
        DailySuccessRateRes dailySuccessRateRes = new DailySuccessRateRes(
                3,
                List.of(
                        new DailySuccessRateDto(
                                1L,
                                "category1",
                                GroupStudyCategoryType.DAILY.name(),
                                60,
                                0.5,
                                List.of(
                                        new GroupMemberProfileImageDto("image1", GroupRole.LEADER),
                                        new GroupMemberProfileImageDto("image2", GroupRole.MEMBER))),
                        new DailySuccessRateDto(
                                2L,
                                "category2",
                                GroupStudyCategoryType.WEEKLY.name(),
                                10 * 60,
                                0.5,
                                List.of(
                                        new GroupMemberProfileImageDto("image1", GroupRole.LEADER),
                                        new GroupMemberProfileImageDto("image2", GroupRole.MEMBER)))));
        Response<DailySuccessRateRes> expected =
                CommonResponse.success(ResponseCode.GROUP_DAILY_GOAL, dailySuccessRateRes);

        given(groupStudyCategoryService.getGroupDailyGoal(groupId)).willReturn(dailySuccessRateRes);

        // when
        ResultActions result = mockMvc.perform(get("/groups/{groupId}/routines/daily-goal", groupId));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        pathParameters(parameterWithName("groupId")
                                .description("조회할 그룹 ID")
                                .attributes(constraints("not null"))),
                        responseFields(
                                fieldWithPath("data.totalMember").description("총 그룹원 수"),
                                fieldWithPath("data.categories[].categoryId").description("그룹 카테고리 ID"),
                                fieldWithPath("data.categories[].name").description("그룹 카테고리명"),
                                fieldWithPath("data.categories[].type")
                                        .description("그룹 카테고리 타입 (일간 - DAILY, 주간 - WEEKLY)"),
                                fieldWithPath("data.categories[].goalTime").description("그룹 카테고리 목표 시간"),
                                fieldWithPath("data.categories[].categoryId").description("그룹 카테고리 ID"),
                                fieldWithPath("data.categories[].successRate")
                                        .description("그룹 카테고리 목표 달성률 (백분위 기준 0.0 ~ 1.0)"),
                                fieldWithPath("data.categories[].profiles[].imageUrl")
                                        .description("그룹 카테고리 목표 달성한 그룹원 이미지 URL"),
                                fieldWithPath("data.categories[].profiles[].role")
                                        .description("그룹 카테고리 목표 달성한 그룹원 권한 (그룹장 - LEADER, 멤버 - MEMBER)"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"))));
    }
}
