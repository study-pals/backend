package com.studypals.domain.groupManage.dao.groupRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.studypals.domain.groupManage.dto.GroupSearchDto;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupHashTag;
import com.studypals.domain.groupManage.entity.HashTag;
import com.studypals.global.request.Cursor;
import com.studypals.global.utils.StringUtils;
import com.studypals.testModules.testSupport.DataJpaSupport;

/**
 * GroupCustomRepsitory 에 대한 query dsl jpa test 입니다.
 *
 * @author jack8
 * @since 2026-01-16
 */
@DisplayName("GroupCustomRepository_Querydsl_test")
class GroupCustomRepositoryTest extends DataJpaSupport {

    private static final int PAGE_SIZE = 20;
    private static final int TOTAL_GROUPS = 200;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    StringUtils stringUtils;

    private HashTag htJava;
    private HashTag htSpring;

    // 필터 대상 그룹을 따로 기억해두면 “필터 결과가 예상과 일치하는지” 계산하기 쉬움
    private final Set<Long> tagMatchedGroupIds = new HashSet<>();
    private final Set<Long> nameMatchedGroupIds = new HashSet<>();
    private final Set<Long> hashTagMatchedGroupIds = new HashSet<>();

    @BeforeEach
    void setUp() {
        htJava = insertHashTag("java");
        htSpring = insertHashTag("spring");

        seed200GroupsWithBoundaries();

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("POPULAR: tie(totalMember 동일) 블록이 페이지 경계를 넘을 때 중복/누락 없이 페이징된다")
    void popular_cursorPaging_noOverlap_noMissing_onTieBoundary() {
        GroupSearchDto dto = new GroupSearchDto(
                null, null, null, null, null // isOpen, isApprovalRequired (필요하면 켜서 추가 테스트)
                );

        List<Group> all = fetchAllByCursor(dto, GroupSortType.POPULAR);

        assertThat(all).hasSize(TOTAL_GROUPS);
        assertNoDuplicateIds(all);
        assertSortedPopularDeterministic(all);
    }

    @Test
    @DisplayName("NEW: tie(createdDate 동일) 블록이 페이지 경계를 넘을 때 중복/누락 없이 페이징된다")
    void new_cursorPaging_noOverlap_noMissing_onTieBoundary() {
        GroupSearchDto dto = new GroupSearchDto(null, null, null, null, null);

        List<Group> all = fetchAllByCursor(dto, GroupSortType.NEW);

        assertThat(all).hasSize(TOTAL_GROUPS);
        assertNoDuplicateIds(all);
        assertSortedNewDeterministic(all);
    }

    @Test
    @DisplayName("OLD: tie(createdDate 동일) 블록이 페이지 경계를 넘을 때 중복/누락 없이 페이징된다")
    void old_cursorPaging_noOverlap_noMissing_onTieBoundary() {
        GroupSearchDto dto = new GroupSearchDto(null, null, null, null, null);

        List<Group> all = fetchAllByCursor(dto, GroupSortType.OLD);

        assertThat(all).hasSize(TOTAL_GROUPS);
        assertNoDuplicateIds(all);
        assertSortedOldDeterministic(all);
    }

    @Test
    @DisplayName("tag 검색: POPULAR/NEW/OLD에서 tag 필터가 정확히 작동하고 정렬도 유지된다")
    void tagFilter_works_onAllSorts() {
        // tag 검색은 normalize + containsIgnoreCase
        GroupSearchDto dto = new GroupSearchDto("TAG-POOL", null, null, null, null);

        // POPULAR
        List<Group> popular = fetchAllByCursor(dto, GroupSortType.POPULAR);
        assertThat(popular).allSatisfy(g -> assertThat(g.getTag().toLowerCase()).contains("tag-pool".toLowerCase()));
        assertNoDuplicateIds(popular);
        assertSortedPopularDeterministic(popular);
        assertThat(popular.stream().map(Group::getId).collect(Collectors.toSet()))
                .isSubsetOf(tagMatchedGroupIds);

        // NEW
        List<Group> newer = fetchAllByCursor(dto, GroupSortType.NEW);
        assertNoDuplicateIds(newer);
        assertSortedNewDeterministic(newer);
        assertThat(newer.stream().map(Group::getId).collect(Collectors.toSet())).isSubsetOf(tagMatchedGroupIds);

        // OLD
        List<Group> older = fetchAllByCursor(dto, GroupSortType.OLD);
        assertNoDuplicateIds(older);
        assertSortedOldDeterministic(older);
        assertThat(older.stream().map(Group::getId).collect(Collectors.toSet())).isSubsetOf(tagMatchedGroupIds);
    }

    @Test
    @DisplayName("name 검색: POPULAR/NEW/OLD에서 name 필터가 정확히 작동한다 (tag가 비어있을 때만)")
    void nameFilter_works_onAllSorts() {
        GroupSearchDto dto = new GroupSearchDto(null, "NamePool", null, null, null);

        List<Group> popular = fetchAllByCursor(dto, GroupSortType.POPULAR);
        assertThat(popular)
                .allSatisfy(g -> assertThat(g.getName().toLowerCase()).contains("namepool".toLowerCase()));
        assertThat(popular.stream().map(Group::getId).collect(Collectors.toSet()))
                .isSubsetOf(nameMatchedGroupIds);

        List<Group> newer = fetchAllByCursor(dto, GroupSortType.NEW);
        assertThat(newer.stream().map(Group::getId).collect(Collectors.toSet())).isSubsetOf(nameMatchedGroupIds);

        List<Group> older = fetchAllByCursor(dto, GroupSortType.OLD);
        assertThat(older.stream().map(Group::getId).collect(Collectors.toSet())).isSubsetOf(nameMatchedGroupIds);
    }

    @Test
    @DisplayName("hashTag 검색: POPULAR/NEW/OLD에서 exists 서브쿼리 기반 필터가 정확히 작동한다 (tag/name 없을 때만)")
    void hashTagFilter_works_onAllSorts() {
        GroupSearchDto dto = new GroupSearchDto(null, null, "java", null, null);

        List<Group> popular = fetchAllByCursor(dto, GroupSortType.POPULAR);
        assertThat(popular.stream().map(Group::getId).collect(Collectors.toSet()))
                .isSubsetOf(hashTagMatchedGroupIds);
        assertSortedPopularDeterministic(popular);

        List<Group> newer = fetchAllByCursor(dto, GroupSortType.NEW);
        assertThat(newer.stream().map(Group::getId).collect(Collectors.toSet())).isSubsetOf(hashTagMatchedGroupIds);
        assertSortedNewDeterministic(newer);

        List<Group> older = fetchAllByCursor(dto, GroupSortType.OLD);
        assertThat(older.stream().map(Group::getId).collect(Collectors.toSet())).isSubsetOf(hashTagMatchedGroupIds);
        assertSortedOldDeterministic(older);
    }

    // =========================
    // Cursor 페이징 공용 유틸
    // =========================

    private List<Group> fetchAllByCursor(GroupSearchDto dto, GroupSortType sort) {
        List<Group> acc = new ArrayList<>();

        Cursor cursor = new Cursor(0L, null, PAGE_SIZE, sort);

        while (true) {
            var slice = groupRepository.search(dto, cursor);
            List<Group> content = slice.getContent();

            if (content.isEmpty()) break;

            acc.addAll(content);

            if (!slice.hasNext()) break;

            Group last = content.get(content.size() - 1);
            cursor = nextCursor(sort, last);
        }

        return acc;
    }

    private Cursor nextCursor(GroupSortType sort, Group last) {
        String value;
        if (sort == GroupSortType.POPULAR) {
            value = String.valueOf(last.getTotalMember());
        } else if (sort == GroupSortType.NEW || sort == GroupSortType.OLD) {
            value = String.valueOf(last.getCreatedDate()); // LocalDate -> "yyyy-MM-dd"
        } else {
            throw new IllegalArgumentException("unsupported sort: " + sort);
        }
        return new Cursor(last.getId(), value, PAGE_SIZE, sort);
    }

    private void assertNoDuplicateIds(List<Group> all) {
        List<Long> ids = all.stream().map(Group::getId).toList();
        Set<Long> uniq = new HashSet<>(ids);
        assertThat(uniq).as("중복 ID가 없어야 함").hasSize(ids.size());
    }

    private void assertSortedPopularDeterministic(List<Group> all) {
        // totalMember desc, id desc
        for (int i = 0; i < all.size() - 1; i++) {
            Group a = all.get(i);
            Group b = all.get(i + 1);

            if (!Objects.equals(a.getTotalMember(), b.getTotalMember())) {
                assertThat(a.getTotalMember()).isGreaterThan(b.getTotalMember());
            } else {
                assertThat(a.getId()).isGreaterThan(b.getId());
            }
        }
    }

    private void assertSortedNewDeterministic(List<Group> all) {
        // createdDate desc, id desc
        for (int i = 0; i < all.size() - 1; i++) {
            Group a = all.get(i);
            Group b = all.get(i + 1);

            if (!a.getCreatedDate().equals(b.getCreatedDate())) {
                assertThat(a.getCreatedDate()).isAfter(b.getCreatedDate());
            } else {
                assertThat(a.getId()).isGreaterThan(b.getId());
            }
        }
    }

    private void assertSortedOldDeterministic(List<Group> all) {
        // createdDate asc, id asc
        for (int i = 0; i < all.size() - 1; i++) {
            Group a = all.get(i);
            Group b = all.get(i + 1);

            if (!a.getCreatedDate().equals(b.getCreatedDate())) {
                assertThat(a.getCreatedDate()).isBefore(b.getCreatedDate());
            } else {
                assertThat(a.getId()).isLessThan(b.getId());
            }
        }
    }

    // =========================
    // 데이터 풀 생성
    // =========================

    private void seed200GroupsWithBoundaries() {
        // POPULAR tie boundary를 확실히 만들기 위한 totalMember 블록
        // 10 + 25 + 30 + 40 + 50 + 45 = 200
        // pageSize=20 => 10(100) + 10(90)에서 페이지1 끝. 다음 페이지는 남은 15(90)부터 시작해야 함.
        List<Integer> totals = new ArrayList<>();
        totals.addAll(Collections.nCopies(10, 100));
        totals.addAll(Collections.nCopies(25, 90)); // tie block crosses boundary
        totals.addAll(Collections.nCopies(30, 80));
        totals.addAll(Collections.nCopies(40, 70));
        totals.addAll(Collections.nCopies(50, 60));
        totals.addAll(Collections.nCopies(45, 50));
        assertThat(totals).hasSize(TOTAL_GROUPS);

        // NEW/OLD tie boundary 만들기 위한 createdDate 블록(25개짜리 덩어리 여러 개)
        // 200개를 8블록(각 25개)으로 구성 -> 항상 페이지 경계를 넘는 tie를 만들기 쉬움
        List<LocalDate> dates = new ArrayList<>();
        LocalDate base = LocalDate.of(2026, 1, 1);
        for (int block = 0; block < 8; block++) {
            LocalDate d = base.plusDays(block);
            dates.addAll(Collections.nCopies(25, d));
        }
        assertThat(dates).hasSize(TOTAL_GROUPS);

        // 필터용: tag/name/hashTag 매칭 그룹을 일부 골라서 심기
        // - tag contains "tag-pool"로 매칭되게
        // - name contains "namepool"로 매칭되게
        // - hashtag: java 매핑되게
        for (int i = 0; i < TOTAL_GROUPS; i++) {
            int total = totals.get(i);
            LocalDate createdDate = dates.get(i);

            String tag = "tag-" + i;
            String name = "group-" + i;

            boolean isTagPool = (i % 10 == 0); // 20개 정도
            boolean isNamePool = (i % 10 == 1); // 20개 정도
            boolean isJavaHash = (i % 10 == 2); // 20개 정도

            if (isTagPool) tag = "TAG-POOL-" + i;
            if (isNamePool) name = "NamePool-" + i;

            Group g = insertGroup(total, tag, name, createdDate);

            if (isTagPool) tagMatchedGroupIds.add(g.getId());
            if (isNamePool) nameMatchedGroupIds.add(g.getId());

            if (isJavaHash) {
                insertGroupHashTag(htJava, g, "#Java");
                hashTagMatchedGroupIds.add(g.getId());
            }
        }
    }

    // =========================
    // 사용자 제공 helper 기반 확장
    // =========================

    private HashTag insertHashTag(String displayName) {
        return em.persist(HashTag.builder()
                .tag(stringUtils.normalize(displayName))
                .usedCount(1L)
                .build());
    }

    private Group insertGroup(int total, String tag, String name, LocalDate createdDate) {
        Group g = Group.builder()
                .name(name)
                .tag(tag)
                .totalMember(total)
                // 필요하면 maxMember / isOpen / isApprovalRequired도 채움
                .maxMember(999)
                .isOpen(true)
                .isApprovalRequired(false)
                .createdDate(createdDate) // 여기 막히면 리플렉션으로 세팅
                .build();

        return em.persist(g);
    }

    private GroupHashTag insertGroupHashTag(HashTag hashTag, Group group, String displayName) {
        return em.persist(GroupHashTag.builder()
                .group(group)
                .hashTag(hashTag)
                .displayTag(displayName)
                .build());
    }
}
