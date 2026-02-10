package study.blog.post.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import study.blog.post.dto.PostSearchCondition;
import study.blog.post.enums.PostSortType;
import study.blog.post.entity.Post;
import study.blog.post.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.blog.post.entity.QPost.*;
import static study.blog.post.entity.QPostTag.postTag;

@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> searchPostByCondition(PostSearchCondition condition, Pageable pageable) {

        JPAQuery<Post> query = queryFactory.selectFrom(post);

        if (condition.tagNames() != null && !condition.tagNames().isEmpty()) {
            query.join(post.tags, postTag);
        }

        return query
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

    private BooleanExpression keywordLike(String keyword) {
        return hasText(keyword) ? post.content.contains(keyword).or(post.title.contains(keyword)) : null;
    }

    private BooleanExpression tagIn(List<String> tagNames) {
        if (CollectionUtils.isEmpty(tagNames)) {
            return null;
        }

        List<String> validNames = tagNames.stream()
                .filter(StringUtils::hasText)
                .toList();

        return !validNames.isEmpty() ? postTag.name.in(validNames) : null;
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
