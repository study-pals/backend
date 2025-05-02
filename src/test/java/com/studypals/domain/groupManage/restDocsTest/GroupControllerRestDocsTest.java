package com.studypals.domain.groupManage.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.groupManage.api.GroupController;
import com.studypals.domain.groupManage.dto.*;
import com.studypals.domain.groupManage.service.GroupService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.RestDocsSupport;

/**
 * {@link GroupController} 에 대한 rest docs web mvc test 입니다. 문서를 생성합니다.
 *
 * @author s0o0bn
 * @see GroupController
 * @see RestDocsSupport
 * @since 2025-04-12
 */
@WebMvcTest(GroupController.class)
public class GroupControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean
    private GroupService groupService;

    @Test
    void getGroupTags_success() throws Exception {

        // given
        List<GetGroupTagRes> list = List.of(new GetGroupTagRes("tag"));
        Response<List<GetGroupTagRes>> expected = CommonResponse.success(ResponseCode.GROUP_TAG_LIST, list);

        given(groupService.getGroupTags()).willReturn(list);

        // when
        ResultActions result = mockMvc.perform(get("/groups/tags"));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        responseFields(
                                fieldWithPath("data[].name").description("그룹 태그 이름"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"))));
    }

    @Test
    @WithMockUser
    void createGroup_success() throws Exception {

        // given
        CreateGroupReq req = new CreateGroupReq("group name", "group tag", 10, false, false);

        given(groupService.createGroup(any(), any())).willReturn(1L);

        // when
        ResultActions result = mockMvc.perform(
                post("/groups").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/groups/1"))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        requestFields(
                                fieldWithPath("name").description("그룹명").attributes(constraints("not null")),
                                fieldWithPath("tag").description("그룹 태그").attributes(constraints("not null")),
                                fieldWithPath("maxMember")
                                        .description("그룹 최대 인원수 / Default 100")
                                        .attributes(constraints("not null")),
                                fieldWithPath("isOpen")
                                        .description("그룹 공개 여부 / Default FALSE")
                                        .attributes(constraints("not null")),
                                fieldWithPath("isApprovalRequired")
                                        .description("그룹 가입 시 승인 필요 여부 / Default FALSE")
                                        .attributes(constraints("not null"))),
                        responseHeaders(headerWithName("Location").description("추가된 그룹 id"))));
    }
}
