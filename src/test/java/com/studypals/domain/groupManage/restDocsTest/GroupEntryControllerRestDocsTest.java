package com.studypals.domain.groupManage.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.groupManage.api.GroupEntryController;
import com.studypals.domain.groupManage.dto.*;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.groupManage.service.GroupEntryService;
import com.studypals.domain.memberManage.dto.MemberProfileDto;
import com.studypals.global.request.Cursor;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.CursorResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.RestDocsSupport;

/**
 * {@link GroupEntryController} 에 대한 rest docs web mvc test 입니다. 문서를 생성합니다.
 *
 * @author s0o0bn
 * @see GroupEntryController
 * @see RestDocsSupport
 * @since 2025-04-12
 */
@WebMvcTest(GroupEntryController.class)
public class GroupEntryControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean
    private GroupEntryService groupEntryService;

    @Test
    @WithMockUser
    void generateEntryCode_success() throws Exception {
        // given
        Long groupId = 1L;
        LocalDateTime expiredAt = LocalDateTime.of(2026, 1, 1, 12, 0, 0);
        GroupEntryCodeRes entryCodeRes = new GroupEntryCodeRes(groupId, "A1B2C3", expiredAt);
        Response<GroupEntryCodeRes> expected = CommonResponse.success(ResponseCode.GROUP_ENTRY_CODE, entryCodeRes);

        given(groupEntryService.generateEntryCode(any(), any())).willReturn(entryCodeRes);

        // when
        ResultActions result = mockMvc.perform(post("/groups/{groupId}/entry-code", groupId));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/groups/1/entry-code/" + entryCodeRes.code()))
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        pathParameters(parameterWithName("groupId")
                                .description("코드 생성할 그룹 ID")
                                .attributes(constraints("not null"))),
                        responseFields(
                                fieldWithPath("data.groupId").description("그룹 ID"),
                                fieldWithPath("data.code").description("그룹 초대 코드 | 6자리의 대문자 알파벳, 숫자 조합"),
                                fieldWithPath("data.expiredAt").description("해당 코드가 만료되는 날짜 및 시간 | null 인 경우 무제한"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지")),
                        responseHeaders(headerWithName("Location").description("생성된 그룹 초대 코드"))));
    }

    @Test
    @WithMockUser
    void increaseCodeExpire_success() throws Exception {
        // given
        Long groupId = 1L;
        Long day = 7L;
        UpdateEntryCodeReq req = new UpdateEntryCodeReq(day);

        // when
        ResultActions result = mockMvc.perform(patch("/groups/{groupId}/entry-code", groupId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        pathParameters(parameterWithName("groupId")
                                .description("초대 코드를 연장할 그룹의 아이디")
                                .attributes(constraints("not null"))),
                        requestFields(fieldWithPath("day")
                                .description("연장할 초대 코드의 그룹 아이디")
                                .attributes(constraints("not null, min(0), max(7)")))));
    }

    @Test
    void getGroupSummary_success() throws Exception {
        // given
        String entryCode = "A1B2C3";
        List<GroupMemberProfileImageDto> profiles = List.of(
                new GroupMemberProfileImageDto("imageUrl url", GroupRole.LEADER),
                new GroupMemberProfileImageDto("imageUrl url", GroupRole.MEMBER));
        GroupSummaryRes groupSummaryRes = GroupSummaryRes.builder()
                .id(1L)
                .name("group name")
                .tag("tag")
                .isOpen(true)
                .memberCount(2)
                .profiles(profiles)
                .build();
        Response<GroupSummaryRes> expected = CommonResponse.success(ResponseCode.GROUP_SUMMARY, groupSummaryRes);

        given(groupEntryService.getGroupSummary(entryCode)).willReturn(groupSummaryRes);

        // when
        ResultActions result = mockMvc.perform(get("/groups/summary").param("entryCode", entryCode));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        queryParameters(parameterWithName("entryCode")
                                .description("그룹 초대 코드")
                                .attributes(constraints("문자열 길이 6"))),
                        responseFields(
                                fieldWithPath("data.id").description("그룹 ID"),
                                fieldWithPath("data.name").description("그룹명"),
                                fieldWithPath("data.tag").description("그룹 태그"),
                                fieldWithPath("data.isOpen").description("그룹 공개 여부"),
                                fieldWithPath("data.memberCount").description("그룹 전체 멤버 수"),
                                fieldWithPath("data.profiles[].imageUrl").description("그룹 멤버 프로필 이미지"),
                                fieldWithPath("data.profiles[].role").description("그룹 멤버 권한 | LEADER, MEMBER"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"))));
    }

    @Test
    @WithMockUser
    void joinGroup_success() throws Exception {
        // given
        Long groupId = 1L;
        Long joinId = 1L;
        String entryCode = "1A2B3C";
        GroupEntryReq req = new GroupEntryReq(groupId, entryCode);

        given(groupEntryService.joinGroup(any(), eq(req))).willReturn(joinId);

        // when
        ResultActions result = mockMvc.perform(post("/groups/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/groups/" + groupId + "/members/" + joinId))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        requestFields(
                                fieldWithPath("groupId").description("그룹 ID").attributes(constraints("not null")),
                                fieldWithPath("entryCode")
                                        .description("그룹 초대 코드")
                                        .attributes(constraints("문자열 길이 6"))),
                        responseHeaders(headerWithName("Location").description("추가된 그룹 멤버 id"))));
    }

    @Test
    @WithMockUser
    void requestParticipant_success() throws Exception {
        // given
        Long groupId = 1L;
        Long requestId = 1L;
        String entryCode = "1A2B3C";
        GroupEntryReq req = new GroupEntryReq(groupId, entryCode);

        given(groupEntryService.requestParticipant(any(), eq(req))).willReturn(requestId);

        // when
        ResultActions result = mockMvc.perform(post("/groups/entry-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/groups/" + groupId + "/requests/" + requestId))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        requestFields(
                                fieldWithPath("groupId").description("그룹 ID").attributes(constraints("not null")),
                                fieldWithPath("entryCode")
                                        .description("그룹 초대 코드")
                                        .attributes(constraints("문자열 길이 6"))),
                        responseHeaders(headerWithName("Location").description("추가된 그룹 가입 요청 id"))));
    }

    @Test
    @WithMockUser
    void getEntryRequests_success() throws Exception {
        // given
        Long groupId = 1L;
        List<GroupEntryRequestDto> content = List.of(
                new GroupEntryRequestDto(1L, new MemberProfileDto(2L, "member2", "image2"), LocalDate.of(2025, 6, 11)),
                new GroupEntryRequestDto(2L, new MemberProfileDto(3L, "member3", "image3"), LocalDate.of(2025, 6, 5)));
        CursorResponse.Content<GroupEntryRequestDto> res = new CursorResponse.Content<>(
                content, content.get(content.size() - 1).requestId(), false);

        given(groupEntryService.getEntryRequests(any(), eq(groupId), any(Cursor.class)))
                .willReturn(res);

        // when
        ResultActions result = mockMvc.perform(get("/groups/{groupId}/entry-requests", groupId)
                .queryParam("cursor", "0")
                .queryParam("size", "10")
                .queryParam("sort", "NEW"));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        queryParameters(
                                parameterWithName("cursor").description("조회 기준 데이터 ID default 0, 이후 요청은 이전 응답의 next 값"),
                                parameterWithName("size").description("조회할 데이터 수 default 10"),
                                parameterWithName("sort").description("조회 시 정렬 기준 default NEW | NEW, OLD")),
                        responseFields(
                                fieldWithPath("data.content[].requestId").description("가입 요청 ID"),
                                fieldWithPath("data.content[].member.id").description("요청한 사용자 ID"),
                                fieldWithPath("data.content[].member.nickname").description("요청한 사용자 닉네임"),
                                fieldWithPath("data.content[].member.imageUrl").description("요청한 사용자 프로필 이미지 URL"),
                                fieldWithPath("data.content[].requestedDate").description("가입 요청 날짜"),
                                fieldWithPath("data.next").description("다음 조회할 ID, 해당 값이 다음 페이지 요청의 cursor가 됨"),
                                fieldWithPath("data.hasNext").description("데이터가 더 존재하는지, 마지막 페이지의 경우 false"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"))));
    }

    @Test
    @WithMockUser
    void acceptEntryRequest_success() throws Exception {
        // given
        Long requestId = 1L;
        AcceptEntryRes res = new AcceptEntryRes(1L, 1L);

        given(groupEntryService.acceptEntryRequest(any(), eq(requestId))).willReturn(res);

        // when
        ResultActions result = mockMvc.perform(post("/groups/entry-requests/{requestId}/accept", requestId));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/groups/\\d+/members/\\d+")))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        pathParameters(parameterWithName("requestId")
                                .description("승인할 요청 ID")
                                .attributes(constraints("not null"))),
                        responseHeaders(headerWithName("Location").description("가입된 그룹원 id"))));
    }

    @Test
    @WithMockUser
    void refuseEntryRequest_success() throws Exception {
        // given
        Long requestId = 1L;

        // when
        ResultActions result = mockMvc.perform(delete("/groups/entry-requests/{requestId}", requestId));

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        pathParameters(parameterWithName("requestId")
                                .description("거절할 요청 ID")
                                .attributes(constraints("not null")))));
    }
}
