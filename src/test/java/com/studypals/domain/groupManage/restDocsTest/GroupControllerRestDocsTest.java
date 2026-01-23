package com.studypals.domain.groupManage.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
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

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.groupManage.api.GroupController;
import com.studypals.domain.groupManage.dto.*;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.groupManage.service.GroupService;
import com.studypals.global.request.Cursor;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.CursorResponse;
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
        CreateGroupReq req =
                new CreateGroupReq("group name", "group tag", 10, false, false, "image.example.com", List.of());

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
                                fieldWithPath("imageUrl")
                                        .description("그룹 이미지 주소")
                                        .attributes(constraints("nullable - null 인 경우 클라이언트 자체적인 이미지 사용")),
                                fieldWithPath("tag").description("그룹 태그").attributes(constraints("not null")),
                                fieldWithPath("maxMember")
                                        .description("그룹 최대 인원수 / Default 100")
                                        .attributes(constraints("not null")),
                                fieldWithPath("hashTags")
                                        .description("해당 그룹을 나타내는 해시태그")
                                        .attributes(constraints("개수 0 ~ 10, 각 문자 길이 ~ 20")),
                                fieldWithPath("isOpen")
                                        .description("그룹 공개 여부 / Default FALSE")
                                        .attributes(constraints("not null")),
                                fieldWithPath("isApprovalRequired")
                                        .description("그룹 가입 시 승인 필요 여부 / Default FALSE")
                                        .attributes(constraints("not null"))),
                        responseHeaders(headerWithName("Location").description("추가된 그룹 id"))));
    }

    @Test
    @WithMockUser
    void getGroups_success() throws Exception {
        List<GetGroupsRes> list = List.of(
                new GetGroupsRes(
                        101L,
                        "알고리즘 코딩 마스터",
                        "취업준비",
                        List.of("알고리즘", "백준", "프로그래머스"),
                        10,
                        "chat_algo_01",
                        true,
                        false,
                        LocalDate.of(2025, 12, 1),
                        List.of(new GroupMemberProfileImageDto("https://exam.com/user1.png", GroupRole.LEADER)),
                        List.of(1L, 2L)),
                new GetGroupsRes(
                        205L,
                        "프론트엔드 리액트 스터디",
                        "프론트개발",
                        List.of("리엑트", "안드로이드 스튜디오"),
                        20,
                        "chat_react_fe",
                        false,
                        true,
                        LocalDate.of(2025, 10, 25),
                        List.of(
                                new GroupMemberProfileImageDto("https://exam.com/user2.png", GroupRole.LEADER),
                                new GroupMemberProfileImageDto("https://exam.com/user3.png", GroupRole.MEMBER)),
                        List.of(3L, 4L)));
        Response<List<GetGroupsRes>> expected = CommonResponse.success(ResponseCode.GROUP_LIST, list);

        given(groupService.getGroups(any())).willReturn(list);

        ResultActions result = mockMvc.perform(get("/groups"));

        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        responseFields(
                                fieldWithPath("code").description("응답 코드 (예: U02-17)"),
                                fieldWithPath("status").description("응답 상태 (예: success)"),
                                fieldWithPath("message").description("응답 메시지"),

                                // 그룹 기본 정보
                                fieldWithPath("data[].groupId").description("그룹의 고유 ID"),
                                fieldWithPath("data[].name").description("그룹 이름"),
                                fieldWithPath("data[].tag").description("그룹의 태그"),
                                fieldWithPath("data[].hashTags").description("그룹의 해시태그"),
                                fieldWithPath("data[].memberCount").description("그룹에 속한 전체 회원 수"),
                                fieldWithPath("data[].chatRoomId").description("그룹에 연결된 채팅방 ID"),
                                fieldWithPath("data[].isOpen").description("그룹 공개 여부 (true: 공개, false: 비공개)"),
                                fieldWithPath("data[].isApprovalRequired")
                                        .description("그룹 가입 시 승인 필요 여부 (true: 필요, false: 불필요)"),
                                fieldWithPath("data[].createdDate").description("그룹 생성일"),

                                // 멤버 프로필 정보 상세화
                                fieldWithPath("data[].profiles[].imageUrl").description("멤버의 프로필 이미지 URL"),
                                fieldWithPath("data[].profiles[].role").description("그룹 내 역할 (LEADER, MEMBER)"),

                                // 카테고리 정보
                                fieldWithPath("data[].categoryIds[]").description("그룹 카테고리 ID 목록"))));
    }

    @Test
    void getGroupDetail_success() throws Exception {
        List<GroupMemberProfileDto> profiles1 = List.of(
                new GroupMemberProfileDto(10L, "LeaderA", "url_a", GroupRole.LEADER),
                new GroupMemberProfileDto(11L, "MemberB", "url_b", GroupRole.MEMBER));

        // 1. 카테고리별 목표 목록 생성
        List<GroupCategoryGoalDto> categoryGoals = List.of(
                new GroupCategoryGoalDto(501L, 1000L, "CS 공부", 75), // CS 공부: 목표 1000 대비 75% 달성
                new GroupCategoryGoalDto(502L, 50L, "알고리즘", 100) // 알고리즘: 목표 50 대비 100% 달성
                );

        // 2. GroupTotalGoalDto 객체로 묶기
        GroupTotalGoalDto totalGoals = new GroupTotalGoalDto(categoryGoals, 88); // 평균 88% 가정

        GetGroupDetailRes groupDetailRes = new GetGroupDetailRes(
                100L,
                "핵심 CS 전공 스터디",
                "tag",
                List.of("운영체제", "네트워크", "자료구조"),
                true, // 공개
                false, // 승인 불필요
                10, // 최대 10명
                2, // 현재 2명
                profiles1,
                totalGoals); // GroupTotalGoalDto 객체 주입

        when(groupService.getGroupDetails(any(), any())).thenReturn(groupDetailRes);
        Response<GetGroupDetailRes> expected = CommonResponse.success(ResponseCode.GROUP_DETAIL, groupDetailRes);

        ResultActions result = mockMvc.perform(get("/groups/{groupId}", 1L).contentType(MediaType.APPLICATION_JSON));
        result.andExpect(hasKey(expected))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        responseFields(
                                fieldWithPath("code").description("U02-18"),
                                fieldWithPath("status").description("응답 상태(예: success or failed"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data").description("그룹 상세 정보 객체"),

                                // GetGroupDetailRes 필드 설명
                                fieldWithPath("data.id").description("그룹의 고유 ID"),
                                fieldWithPath("data.name").description("그룹 이름"),
                                fieldWithPath("data.tag").description("그룹의 태그"),
                                fieldWithPath("data.hashTags").description("그룹의 해시태그"),
                                fieldWithPath("data.isOpen").description("그룹 공개 여부 (true: 공개, false: 비공개)"),
                                fieldWithPath("data.isApprovalRequired")
                                        .description("그룹 가입 시 승인 필요 여부 (true: 필요, false: 불필요)"),
                                fieldWithPath("data.totalMemberCount").description("그룹의 최대 멤버 수"),
                                fieldWithPath("data.currentMemberCount").description("그룹의 현재 멤버 수"),

                                // profiles 배열 필드
                                fieldWithPath("data.profiles")
                                        .description("그룹 멤버 프로필 목록 (List<GroupMemberProfileDto>)"),
                                fieldWithPath("data.profiles[].id").description("멤버의 고유 ID"),
                                fieldWithPath("data.profiles[].nickname").description("멤버의 닉네임"),
                                fieldWithPath("data.profiles[].imageUrl").description("멤버 프로필 이미지 URL"),
                                fieldWithPath("data.profiles[].role").description("그룹 내 멤버 역할 (예: LEADER, MEMBER)"),

                                // userGoals 객체 필드 (GroupTotalGoalDto)
                                fieldWithPath("data.groupGoals").description("그룹 목표 및 달성률 정보 객체"),
                                fieldWithPath("data.groupGoals.overallAveragePercent")
                                        .description("전체 카테고리 목표의 평균 달성률 (%)"),

                                // userGoals.userGoals 배열 필드 (GroupCategoryGoalDto 목록)
                                fieldWithPath("data.groupGoals.categoryGoals")
                                        .description("그룹 카테고리별 달성률 목록 (List<GroupCategoryGoalDto>)"),
                                fieldWithPath("data.groupGoals.categoryGoals[].categoryId")
                                        .description("스터디 카테고리의 고유 ID"),
                                fieldWithPath("data.groupGoals.categoryGoals[].categoryGoal")
                                        .description("카테고리의 그룹 목표량"),
                                fieldWithPath("data.groupGoals.categoryGoals[].categoryName")
                                        .description("카테고리 이름"),
                                fieldWithPath("data.groupGoals.categoryGoals[].achievementPercent")
                                        .description("그룹 목표 대비 카테고리 달성률 (%)"))));
    }

    @Test
    @WithMockUser
    void searchGroups_success() throws Exception {

        // given
        List<GetGroupsRes> content = List.of(
                new GetGroupsRes(
                        101L,
                        "알고리즘 코딩 마스터",
                        "취업준비",
                        List.of("알고리즘", "백준"),
                        10,
                        "chat_algo_01",
                        true,
                        false,
                        LocalDate.of(2025, 12, 1),
                        List.of(new GroupMemberProfileImageDto("https://exam.com/user1.png", GroupRole.LEADER)),
                        List.of(1L, 2L)),
                new GetGroupsRes(
                        205L,
                        "자바 스터디",
                        "백엔드",
                        List.of("java", "spring"),
                        20,
                        "chat_java_01",
                        true,
                        false,
                        LocalDate.of(2025, 11, 20),
                        List.of(new GroupMemberProfileImageDto("https://exam.com/user2.png", GroupRole.MEMBER)),
                        List.of(3L)));

        CursorResponse.Content<GetGroupsRes> cursorContent = new CursorResponse.Content<>(content, 205L, true);

        CursorResponse<GetGroupsRes> response = CursorResponse.success(ResponseCode.GROUP_SEARCH, cursorContent);

        given(groupService.search(any(GroupSearchDto.class), any(Cursor.class))).willReturn(cursorContent);

        // when
        ResultActions result = mockMvc.perform(get("/groups/search")
                .param("tag", "취업")
                .param("cursor", "0")
                .param("size", "5")
                .param("sort", "POPULAR")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(response))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),

                        /* ===== Query Parameters ===== */
                        queryParameters(
                                parameterWithName("tag").optional().description("그룹 태그 검색 (tag/hashTag/name 중 하나만 허용)"),
                                parameterWithName("hashTag").optional().description("해시태그 검색"),
                                parameterWithName("name").optional().description("그룹 이름 검색"),
                                parameterWithName("open").optional().description("공개 그룹 여부 (default: true)"),
                                parameterWithName("approval").optional().description("승인 필요 여부 (default: true)"),
                                parameterWithName("cursor").optional().description("커서 기준 ID (첫 페이지는 0, default: 0)"),
                                parameterWithName("value")
                                        .optional()
                                        .description("커서 기준 tie point (첫 페이지는 null, default: \"\")"),
                                parameterWithName("size").description("페이지 크기"),
                                parameterWithName("sort").description("정렬 기준 (POPULAR | NEW | OLD)")),

                        /* ===== Response Fields ===== */
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data").description("커서 기반 페이징 응답"),
                                fieldWithPath("data.content").description("조회된 그룹 목록"),
                                fieldWithPath("data.next").description("다음 페이지 커서 값"),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),

                                // content[].*
                                fieldWithPath("data.content[].groupId").description("그룹 ID"),
                                fieldWithPath("data.content[].name").description("그룹 이름"),
                                fieldWithPath("data.content[].tag").description("그룹 태그"),
                                fieldWithPath("data.content[].hashTags").description("그룹 해시태그 목록"),
                                fieldWithPath("data.content[].memberCount").description("그룹 인원 수"),
                                fieldWithPath("data.content[].chatRoomId").description("채팅방 ID"),
                                fieldWithPath("data.content[].isOpen").description("공개 여부"),
                                fieldWithPath("data.content[].isApprovalRequired")
                                        .description("승인 필요 여부"),
                                fieldWithPath("data.content[].createdDate").description("그룹 생성일"),
                                fieldWithPath("data.content[].profiles").description("상위 멤버 프로필 목록"),
                                fieldWithPath("data.content[].profiles[].imageUrl")
                                        .description("프로필 이미지 URL"),
                                fieldWithPath("data.content[].profiles[].role").description("그룹 내 역할"),
                                fieldWithPath("data.content[].categoryIds").description("그룹 카테고리 ID 목록"))));
    }
}
