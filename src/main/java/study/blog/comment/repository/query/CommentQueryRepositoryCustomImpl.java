package study.blog.comment.repository.query;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import study.blog.comment.dto.CommentViewResponse;
import study.blog.comment.dto.QCommentViewResponse;
import study.blog.comment.enums.CommentSortType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static study.blog.comment.entity.QComment.comment;
import static study.blog.member.entity.QMember.member;

@RequiredArgsConstructor
public class CommentQueryRepositoryCustomImpl implements CommentQueryRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public List<CommentViewResponse> findAllCommentsWithPaging(Long postId, Pageable pageable) {
        // 조회 전용 DTO로 바로 프로젝션하여 N+1 및 불필요한 엔티티 로딩을 피한다.
        // 작성자(member)는 존재하지 않을 수 있어(left join) null-safe 하게 조회한다. (탈퇴/익명 처리 등 정책 대비)
        return queryFactory
                .select(new QCommentViewResponse(
                        comment.id,
                        comment.postId,
                        member.id,
                        member.nickname,
                        comment.content,
                        comment.parentId,
                        comment.status,
                        comment.createdAt
                ))
                .from(comment)
                .leftJoin(member).on(member.id.eq(comment.authorId))
                .where(postIdEq(postId))
                // 정렬은 요청 Sort를 CommentSortType으로 화이트리스트 처리한다. (허용되지 않은 정렬은 기본 정렬(createdAt)로 폴백)
                .orderBy(getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public Map<Long, Long> countCommentByPostIds(List<Long> postIds) {
        // 게시글 목록 조회 시 댓글 수 N+1을 피하기 위해 postId 단위로 groupBy 집계한다.
        List<Tuple> result = queryFactory
                .select(comment.postId, comment.count())
                .from(comment)
                .where(comment.postId.in(postIds))
                .groupBy(comment.postId)
                .fetch();

        return result.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(comment.postId),
                        tuple -> tuple.get(comment.count())
                ));
    }

    @Override
    public long countCommentByPostId(Long postId) {
        Long result = queryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.postId.eq(postId))
                .fetchOne();
        return result != null ? result : 0L;
    }

    public BooleanExpression postIdEq(Long postId){
        return postId != null ? comment.postId.eq(postId) : null;
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
        return pageable
                .getSort()
                .stream()
                .map(order -> CommentSortType.from(order.getProperty())
                        .<OrderSpecifier<?>>map(sortType -> sortType.toOrder(comment, order.getDirection()))
                        .orElseGet(this::defaultOrder)
                )
                .toArray(OrderSpecifier<?>[]::new);
    }

    private OrderSpecifier<?> defaultOrder() {
        return new OrderSpecifier<>(Order.DESC, comment.createdAt);
    }
}
