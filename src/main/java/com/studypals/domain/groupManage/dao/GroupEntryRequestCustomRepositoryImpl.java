package com.studypals.domain.groupManage.dao;

import static com.studypals.domain.groupManage.entity.QGroupEntryRequest.groupEntryRequest;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import lombok.RequiredArgsConstructor;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.global.dao.AbstractPagingRepository;
import com.studypals.global.request.Cursor;

/**
 * group entry request custom repository 의 구현 클래스입니다.
 *
 * <p>group entry request 관련 커스텀 쿼리를 구현합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link GroupEntryRequestCustomRepository} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * Repository
 *
 * @author s0o0bn
 * @see GroupEntryRequestCustomRepository
 * @since 2025-06-05
 */
@RequiredArgsConstructor
public class GroupEntryRequestCustomRepositoryImpl extends AbstractPagingRepository<GroupEntryRequest>
        implements GroupEntryRequestCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<GroupEntryRequest> findAllByGroupIdWithPagination(Long groupId, Cursor cursor) {
        List<GroupEntryRequest> results = queryFactory
                .selectFrom(groupEntryRequest)
                .where(groupEntryRequest.group.id.eq(groupId).and(groupEntryRequest.id.gt(cursor.cursor())))
                .orderBy(getOrderSpecifier(GroupEntryRequest.class, "groupEntryRequest", cursor.sort()))
                .limit(cursor.size() + 1)
                .fetch();

        boolean hasNext = results.size() > cursor.size();
        if (hasNext) {
            results.remove(results.size() - 1);
        }

        return new SliceImpl<>(results, PageRequest.of(0, cursor.size()), hasNext);
    }
}
