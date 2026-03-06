package study.blog.post.infrastructure.persistence.query;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import study.blog.post.domain.PostSortType;
import study.blog.post.domain.PostStatus;
import study.blog.post.domain.entity.Post;
import study.blog.post.entity.QPostTag;
import study.blog.post.presentation.requset.PostSearchCondition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;
import static study.blog.post.entity.QPost.post;
import static study.blog.post.entity.QPostTag.postTag;

@RequiredArgsConstructor
public class PostQueryRepositoryCustomImpl implements PostQueryRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<Post> findMainPosts(Pageable pageable) {
        return queryFactory
                .select(post)
                .from(post)
                .where(postStatusEq(PostStatus.PUBLISHED))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable))
                .fetch();
    }

    @Override
    public long countMainPosts() {
        return Objects.requireNonNullElse(
                queryFactory
                        .select(post.count())
                        .from(post)
                        .where(postStatusEq(PostStatus.PUBLISHED))
                        .fetchOne(),
                0L
        );
    }

    @Override
    public List<Post> searchMemberPosts(Long memberId, PostSearchCondition condition, Pageable pageable) {
        // 마이페이지/내 글 목록: "작성자" + "상태" 조건으로만 단순 조회 (페이징/정렬 포함)
        return queryFactory
                .select(post)
                .from(post)
                .where(
                        authorEq(memberId),
                        postStatusIn(condition.postStatuses())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable))
                .fetch();
    }

    @Override
    public long countMemberPosts(Long memberId, PostSearchCondition condition) {
        // 목록 조회 쿼리와 count 쿼리를 분리한다.
        // (검색 조건/조인 확장 시 count 성능 저하 가능성이 커, 필요 시 count 최적화 포인트를 분리해두기 위함)
        return Objects.requireNonNullElse(
                queryFactory
                        .select(post.count())
                        .from(post)
                        .where(
                                authorEq(memberId),
                                postStatusIn(condition.postStatuses())
                        ).fetchOne(),
                0L
        );
    }

    @Override
    public List<Post> searchPostsByCondition(PostSearchCondition condition, Pageable pageable) {
        // 검색 목록: 키워드/태그/상태/기간 등 조합 조건을 지원 (페이징/정렬 포함)
        return queryFactory
                .select(post)
                .from(post)
                .where(
                        keywordLike(condition.keyword()),
                        tagIn(condition.tagNames()),
                        postStatusIn(condition.postStatuses()),
                        createdAtFrom(condition.createdFrom()),
                        createdAtTo(condition.createdTo())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable))
                .fetch();
    }

    @Override
    public long countPostByCondition(PostSearchCondition condition) {
        // 목록 조회 쿼리와 count 쿼리를 분리한다.
        // (검색 조건/조인 확장 시 count 성능 저하 가능성이 커, 필요 시 count 최적화 포인트를 분리해두기 위함)
        return Objects.requireNonNullElse(
                queryFactory
                        .select(post.count())
                        .from(post)
                        .where(
                            keywordLike(condition.keyword()),
                            tagIn(condition.tagNames()),
                            postStatusIn(condition.postStatuses()),
                            createdAtFrom(condition.createdFrom()),
                            createdAtTo(condition.createdTo())
                ).fetchOne(),
                0L
        );
    }

    private BooleanExpression keywordLike(String keyword) {
        return hasText(keyword) ? post.content.contains(keyword).or(post.title.contains(keyword)) : null;
    }

    private BooleanExpression tagIn2(List<String> tagNames) {
        if (CollectionUtils.isEmpty(tagNames)) {
            return null;
        }

        List<String> validNames = tagNames.stream()
                .filter(StringUtils::hasText)
                .toList();

        return !validNames.isEmpty() ? postTag.name.in(validNames) : null;
    }

    private BooleanExpression tagIn(List<String> tagNames) {
        if (CollectionUtils.isEmpty(tagNames)) {
            return null;
        }

        List<String> validNames = tagNames.stream()
                .filter(StringUtils::hasText)
                .toList();

        if (validNames.isEmpty()) return null;

        QPostTag subTag = new QPostTag("subTag");

        return JPAExpressions
                .selectOne()
                .from(subTag)
                .where(
                        subTag.post.eq(post),
                        subTag.name.in(validNames)
                )
                .exists();
    }

    private BooleanExpression createdAtFrom(LocalDateTime createdAtFrom) {
        return !isEmpty(createdAtFrom) ? post.createdAt.goe(createdAtFrom) : null;
    }

    private BooleanExpression createdAtTo(LocalDateTime createdAtTo) {
        return !isEmpty(createdAtTo) ? post.createdAt.loe(createdAtTo) : null;
    }

    private BooleanExpression postStatusIn(List<PostStatus> postStatuses) {
        return !isEmpty(postStatuses) ? post.postStatus.in(postStatuses) : null;
    }

    private BooleanExpression postStatusEq(PostStatus postStatus){
        return postStatus != null ? post.postStatus.eq(postStatus) : null;
    }

    private BooleanExpression authorEq(Long memberId){
        return memberId != null ? post.authorId.eq(memberId) : null;
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
        return pageable
                .getSort()
                .stream()
                .map(order -> PostSortType.from(order.getProperty())
                        .<OrderSpecifier<?>>map(sortType -> sortType.toOrder(post, order.getDirection()))
                        .orElseGet(this::defaultOrder)
                )
                .toArray(OrderSpecifier<?>[]::new);
    }

    private OrderSpecifier<?> defaultOrder() {
        return new OrderSpecifier<>(Order.DESC, post.createdAt);
    }
}
