package study.blog.post.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ObjectUtils;
import study.blog.post.dto.PostSearchCondition;
import study.blog.post.entity.Post;
import study.blog.post.entity.PostTag;
import study.blog.post.entity.QPostTag;
import study.blog.post.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;
import static study.blog.post.entity.QPost.*;
import static study.blog.post.entity.QPostTag.postTag;

@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> searchPostByCondition(PostSearchCondition condition, Pageable pageable) {
        return queryFactory
                .select(post)
                .from(post)
                .join(post.tags, postTag)
                .where(
                        keywordLike(condition.keyword()),
                        tagEq(condition.tagNames()),
                        postStatusEq(condition.postStatuses()),
                        createdAtFrom(condition.createdFrom()),
                        createdAtTo(condition.createdTo())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression keywordLike(String keyword){
        return hasText(keyword) ? post.content.contains(keyword).or(post.title.contains(keyword)) : null;
    }

    private BooleanExpression tagEq(List<String> tagNames){
        return !isEmpty(tagNames) ? postTag.name.in(tagNames) : null;
    }

    private BooleanExpression createdAtFrom(LocalDateTime createdAtFrom){
        return !isEmpty(createdAtFrom) ? post.createdAt.goe(createdAtFrom) : null;
    }

    private BooleanExpression createdAtTo(LocalDateTime createdAtTo){
        return !isEmpty(createdAtTo) ? post.createdAt.loe(createdAtTo) : null;
    }

    private BooleanExpression postStatusEq(List<PostStatus> postStatuses){
        return !isEmpty(postStatuses) ? post.postStatus.in(postStatuses) : null;
    }
}
