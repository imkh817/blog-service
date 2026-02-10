package study.blog.post.enums;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import org.springframework.data.domain.Sort;
import study.blog.post.entity.QPost;

import java.util.Arrays;
import java.util.Optional;

public enum PostSortType {
    CREATED_AT("createdAt"),
    VIEW_COUNT("viewCount");

    private final String property;

    PostSortType(String property) {
        this.property = property;
    }

    public static Optional<PostSortType> from(String property){
        return Arrays.stream(values())
                .filter(postSortType -> postSortType.property.equals(property))
                .findFirst();
    }

    public OrderSpecifier<?> toOrder(QPost post, Sort.Direction direction) {
        Order order = direction.isAscending() ? Order.ASC : Order.DESC;

        return switch (this) {
            case CREATED_AT -> new OrderSpecifier<>(order, post.createdAt);
            case VIEW_COUNT -> new OrderSpecifier<>(order, post.viewCount);
        };
    }
}
