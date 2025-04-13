package com.studypals.domain.groupManage.restDocsTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.groupManage.api.GroupController;
import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.fixture.GroupFixture;
import com.studypals.domain.groupManage.service.GroupService;
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
    void createGroup_success() throws Exception {

        // given
        CreateGroupReq req = GroupFixture.createGroupReq();

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
