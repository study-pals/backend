package com.studypals.domain.groupManage.restDocsTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.studypals.domain.groupManage.api.GroupRankingController;
import com.studypals.domain.groupManage.dto.GroupMemberRankingDto;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.groupManage.service.GroupRankingService;
import com.studypals.testModules.testSupport.RestDocsSupport;

@WebMvcTest(GroupRankingController.class)
public class GroupRankingControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean
    private GroupRankingService groupRankingService;

    @Test
    @DisplayName("그룹 일간 랭킹 조회 API 성공 테스트")
    void getGroupRanking_Daily_Success() throws Exception {
        // given
        Long groupId = 10L;
        GroupRankingPeriod period = GroupRankingPeriod.DAILY;
        List<GroupMemberRankingDto> responseDto = List.of(
                new GroupMemberRankingDto(1L, "사용자", "https://image.com/1", 3600L, GroupRole.LEADER),
                new GroupMemberRankingDto(2L, "스터디열공", "https://image.com/2", 1800L, GroupRole.MEMBER));

        given(groupRankingService.getGroupRanking(any(), eq(groupId), eq(period)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/groups/rank/{groupId}/{period}", groupId, period))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        pathParameters(
                                parameterWithName("groupId").description("그룹 ID"),
                                parameterWithName("period").description("조회 기간 (DAILY, WEEKLY, MONTHLY)")),
                        responseFields(
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data[]").description("랭킹 목록"),
                                fieldWithPath("data[].id").description("멤버 ID"),
                                fieldWithPath("data[].nickname").description("닉네임 (본인은 '사용자')"),
                                fieldWithPath("data[].imageUrl").description("프로필 이미지 URL"),
                                fieldWithPath("data[].studyTime").description("총 공부 시간 (초)"),
                                fieldWithPath("data[].role").description("그룹 내 역할 (LEADER, MEMBER)"))));
    }

    @Test
    @DisplayName("그룹 주간 랭킹 조회 API 성공 테스트")
    void getGroupRanking_Weekly_Success() throws Exception {
        // given
        Long groupId = 10L;
        GroupRankingPeriod period = GroupRankingPeriod.WEEKLY;
        List<GroupMemberRankingDto> responseDto = List.of(
                new GroupMemberRankingDto(1L, "사용자", "https://image.com/1", 3600L, GroupRole.LEADER),
                new GroupMemberRankingDto(2L, "스터디열공", "https://image.com/2", 1800L, GroupRole.MEMBER));

        given(groupRankingService.getGroupRanking(any(), eq(groupId), eq(period)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/groups/rank/{groupId}/{period}", groupId, period))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        pathParameters(
                                parameterWithName("groupId").description("그룹 ID"),
                                parameterWithName("period").description("조회 기간 (DAILY, WEEKLY, MONTHLY)")),
                        responseFields(
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data[]").description("랭킹 목록"),
                                fieldWithPath("data[].id").description("멤버 ID"),
                                fieldWithPath("data[].nickname").description("닉네임 (본인은 '사용자')"),
                                fieldWithPath("data[].imageUrl").description("프로필 이미지 URL"),
                                fieldWithPath("data[].studyTime").description("총 공부 시간 (초)"),
                                fieldWithPath("data[].role").description("그룹 내 역할 (LEADER, MEMBER)"))));
    }

    @Test
    @DisplayName("그룹 월간 랭킹 조회 API 성공 테스트")
    void getGroupRanking_Monthly_Success() throws Exception {
        // given
        Long groupId = 10L;
        GroupRankingPeriod period = GroupRankingPeriod.MONTHLY;
        List<GroupMemberRankingDto> responseDto = List.of(
                new GroupMemberRankingDto(1L, "사용자", "https://image.com/1", 3600L, GroupRole.LEADER),
                new GroupMemberRankingDto(2L, "스터디열공", "https://image.com/2", 1800L, GroupRole.MEMBER));

        given(groupRankingService.getGroupRanking(any(), eq(groupId), eq(period)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/groups/rank/{groupId}/{period}", groupId, period))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        pathParameters(
                                parameterWithName("groupId").description("그룹 ID"),
                                parameterWithName("period").description("조회 기간 (DAILY, WEEKLY, MONTHLY)")),
                        responseFields(
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data[]").description("랭킹 목록"),
                                fieldWithPath("data[].id").description("멤버 ID"),
                                fieldWithPath("data[].nickname").description("닉네임 (본인은 '사용자')"),
                                fieldWithPath("data[].imageUrl").description("프로필 이미지 URL"),
                                fieldWithPath("data[].studyTime").description("총 공부 시간 (초)"),
                                fieldWithPath("data[].role").description("그룹 내 역할 (LEADER, MEMBER)"))));
    }
}
