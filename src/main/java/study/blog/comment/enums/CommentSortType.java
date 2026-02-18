package study.blog.comment.enums;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import org.springframework.data.domain.Sort;
import study.blog.comment.entity.QComment;

import java.util.Arrays;
import java.util.Optional;

public enum CommentSortType {

    CREATED_AT("createdAt");

    private final String property;

    CommentSortType(String property) {
        this.property = property;
    }

    public static Optional<CommentSortType> from(String property){
        return Arrays.stream(values())
                .filter(commentSortType -> commentSortType.property.equals(property))
                .findFirst();
    }

    public OrderSpecifier<?> toOrder(QComment comment, Sort.Direction direction) {
        Order order = direction.isAscending() ? Order.ASC : Order.DESC;

        return switch (this) {
            case CREATED_AT -> new OrderSpecifier<>(order, comment.createdAt);
        };
    }
}
