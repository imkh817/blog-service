package study.blog.like.postlike.repository.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static study.blog.like.postlike.entity.QPostLike.postLike;

@RequiredArgsConstructor
public class PostLikeQueryRepositoryCustomImpl implements PostLikeQueryRepositoryCustom {

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
