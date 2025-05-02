package com.studypals.domain.groupManage.restDocsTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.groupManage.api.GroupEntryController;
import com.studypals.domain.groupManage.dto.*;
import com.studypals.domain.groupManage.service.GroupEntryService;
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

    //    @MockitoBean
    //    private GroupService groupService;

    @MockitoBean
    private GroupEntryService groupEntryService;

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
                                        .attributes(constraints("not null"))),
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
                                        .attributes(constraints("not null"))),
                        responseHeaders(headerWithName("Location").description("추가된 그룹 가입 요청 id"))));
    }
}
