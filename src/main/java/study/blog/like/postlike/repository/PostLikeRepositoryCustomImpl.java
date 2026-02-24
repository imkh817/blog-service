package study.blog.like.postlike.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import study.blog.like.postlike.entity.QPostLike;

import java.util.List;

import static study.blog.like.postlike.entity.QPostLike.*;

@RequiredArgsConstructor
public class PostLikeRepositoryCustomImpl implements PostLikeRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findPostIdByMemberIdAndPostIdIn(Long memberId, List<Long> postIds){
        return queryFactory
                .select(postLike.postId)
                .from(postLike)
                .where(
                        memberIdEq(memberId),
                        postIdIn(postIds)
                )
                .fetch();
    }

    private BooleanExpression postIdIn(List<Long> postIds) {
        return !postIds.isEmpty() ? postLike.postId.in(postIds) : null;

    }

    private BooleanExpression memberIdEq(Long memberId){
        return memberId != null ? postLike.memberId.eq(memberId) : null;
    }
}
