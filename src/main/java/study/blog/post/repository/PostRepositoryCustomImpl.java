package study.blog.post.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import study.blog.post.dto.PostSearchCondition;
import study.blog.post.entity.Post;
import study.blog.post.entity.QPostTag;
import study.blog.post.enums.PostSortType;
import study.blog.post.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.springframework.util.StringUtils.hasText;
import static study.blog.post.entity.QPost.post;
import static study.blog.post.entity.QPostTag.postTag;

@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> searchMemberPosts(Long memberId, PostSearchCondition condition, Pageable pageable) {
        return queryFactory
                .select(post)
                .from(post)
                .where(
                        authorEq(memberId),
                        postStatusEq(condition.postStatuses())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable))
                .fetch();
    }

    @Override
    public long countMemberPosts(Long memberId, PostSearchCondition condition) {
        return Objects.requireNonNullElse(
                queryFactory
                        .select(post.count())
                        .from(post)
                        .where(
                                authorEq(memberId),
                                postStatusEq(condition.postStatuses())
                        ).fetchOne(),
                0L
        );
    }

    @Override
    public List<Post> searchPostsByCondition(Long memberId, PostSearchCondition condition, Pageable pageable) {
        return queryFactory
                .select(post)
                .from(post)
                .where(
                        keywordLike(condition.keyword()),
                        tagIn(condition.tagNames()),
                        postStatusEq(condition.postStatuses()),
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
        return Objects.requireNonNullElse(
                queryFactory
                        .select(post.count())
                        .from(post)
                        .where(
                            keywordLike(condition.keyword()),
                            tagIn(condition.tagNames()),
                            postStatusEq(condition.postStatuses()),
                            createdAtFrom(condition.createdFrom()),
                            createdAtTo(condition.createdTo())
                ).fetchOne(),
                0L
        );
    }

    /**
     * 게시글 좋아요수를 원자적으로 증가
     *
     * 조회 후 엔티티 수정하는 방식이 아니라,
     * DB 레벨에서 원자적으로 update를 수행하여 동시성 환경에서 안전하게 처리
     *
     * @param postId 조회수를 증가시킬 게시글 ID
     * @param delta 증가시킬 좋아요 수 (음수 가능)
     * @return 영향받은 Row 수
     */
    @Override
    public long incrementLikeCount(Long postId, int delta) {
        return queryFactory
                .update(post)
                .set(post.likeCount, post.likeCount.add(delta))
                .where(post.id.eq(postId))
                .execute();
    }

    /**
     * 게시글 조회수를 원자적으로 증가
     *
     * 조회 후 엔티티 수정하는 방식이 아니라,
     * DB 레벨에서 원자적으로 update를 수행하여 동시성 환경에서 안전하게 처리
     *
     * @param postId 조회수를 증가시킬 게시글 ID
     * @param viewCount 증가시킬 수치
     * @return 영향받은 Row 수
     */
    @Override
    public long incrementViewCount(Long postId, long viewCount) {
        return queryFactory
                .update(post)
                .set(post.viewCount, post.viewCount.add(viewCount))
                .where(post.id.eq(postId))
                .execute();
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
        return !ObjectUtils.isEmpty(createdAtFrom) ? post.createdAt.goe(createdAtFrom) : null;
    }

    private BooleanExpression createdAtTo(LocalDateTime createdAtTo) {
        return !ObjectUtils.isEmpty(createdAtTo) ? post.createdAt.loe(createdAtTo) : null;
    }

    private BooleanExpression postStatusEq(List<PostStatus> postStatuses) {
        return !ObjectUtils.isEmpty(postStatuses) ? post.postStatus.in(postStatuses) : null;
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
