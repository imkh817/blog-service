package study.blog.like.postlike.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import study.blog.like.postlike.entity.QPostLike;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public Map<Long, Long> countByPostIds(List<Long> postIds) {
        List<Tuple> tuples = queryFactory
                .select(postLike.postId, postLike.count())
                .from(postLike)
                .where(postLike.id.in(postIds))
                .groupBy(postLike.postId)
                .fetch();

        return tuples.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(postLike.postId),
                        tuple -> tuple.get(postLike.count())
                ));
    }

    private BooleanExpression postIdIn(List<Long> postIds) {
        return !postIds.isEmpty() ? postLike.postId.in(postIds) : null;

    }

    private BooleanExpression memberIdEq(Long memberId){
        return memberId != null ? postLike.memberId.eq(memberId) : null;
    }

}
