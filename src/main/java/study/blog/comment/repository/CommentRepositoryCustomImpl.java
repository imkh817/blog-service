package study.blog.comment.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import study.blog.comment.entity.Comment;
import study.blog.comment.enums.CommentSortType;

import java.util.List;

import static study.blog.comment.entity.QComment.comment;

@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> findAllCommentsWithPaging(Long postId, Pageable pageable){
        return queryFactory
                .selectFrom(comment)
                .where(postIdEq(postId))
                .orderBy(getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
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
