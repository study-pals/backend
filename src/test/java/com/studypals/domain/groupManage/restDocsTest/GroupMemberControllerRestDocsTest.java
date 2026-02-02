package com.studypals.domain.groupManage.restDocsTest;

import com.studypals.domain.groupManage.api.GroupMemberController;
import com.studypals.domain.groupManage.service.GroupMemberService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.RestDocsSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link GroupMemberController} 에 대한 rest docs web mvc test 입니다. 문서를 생성합니다.
 *
 * @author zjxlomin
 * @see GroupMemberController
 * @see RestDocsSupport
 * @since 2026-01-28
 */
@WebMvcTest(GroupMemberController.class)
public class GroupMemberControllerRestDocsTest extends RestDocsSupport {
    @MockitoBean
    private GroupMemberService groupMemberService;

    @Test
    @WithMockUser
    void promoteLeader_success() throws Exception {
        // given
        Long groupId = 1L;
        Long nextLeaderId = 2L;

        doNothing().when(groupMemberService).promoteLeader(eq(groupId), any(), eq(nextLeaderId));
        Response<Long> expected = CommonResponse.success(ResponseCode.GROUP_LEADER, groupId);

        // when
        ResultActions result = mockMvc.perform(
                put("/groups/{groupId}/promote/{nextLeaderId}", groupId, nextLeaderId));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        pathParameters(
                                parameterWithName("groupId").description("그룹 ID"),
                                parameterWithName("nextLeaderId").description("차기 그룹장 ID")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data").description("그룹 ID")
                        )
                ));
    }
}
