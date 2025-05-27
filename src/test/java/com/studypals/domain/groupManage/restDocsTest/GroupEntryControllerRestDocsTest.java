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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.studypals.global.responses.CommonResponse;
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
        GroupEntryCodeRes entryCodeRes = new GroupEntryCodeRes(groupId, "A1B2C3");
        Response<GroupEntryCodeRes> expected = CommonResponse.success(ResponseCode.GROUP_ENTRY_CODE, entryCodeRes);

        given(groupEntryService.generateEntryCode(any(), any())).willReturn(entryCodeRes);

        // when
        ResultActions result = mockMvc.perform(post("/groups/" + groupId + "/entry-code"));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/groups/1/entry-code/" + entryCodeRes.code()))
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        responseFields(
                                fieldWithPath("data.groupId").description("그룹 ID"),
                                fieldWithPath("data.code").description("그룹 초대 코드 | 6자리의 대문자 알파벳, 숫자 조합"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지")),
                        responseHeaders(headerWithName("Location").description("생성된 그룹 초대 코드"))));
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
        ResultActions result = mockMvc.perform(post("/groups/request-entry")
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
    void acceptEntryRequest_success() throws Exception {
        // given
        Long userId = 1L;
        Long memberId = 1L;
        AcceptEntryReq req = new AcceptEntryReq(1L, 1L);

        given(groupEntryService.acceptEntryRequest(userId, req)).willReturn(memberId);

        // when
        ResultActions result = mockMvc.perform(post("/groups/accept-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/groups/\\d+/members/\\d+")))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        requestFields(
                                fieldWithPath("groupId").description("그룹 ID").attributes(constraints("not null")),
                                fieldWithPath("requestId")
                                        .description("그룹 가입 요청 ID")
                                        .attributes(constraints("not null"))),
                        responseHeaders(headerWithName("Location").description("가입된 그룹원 id"))));
    }
}
