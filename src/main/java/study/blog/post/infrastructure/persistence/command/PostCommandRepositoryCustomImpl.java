package study.blog.post.infrastructure.persistence.command;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static study.blog.post.entity.QPost.post;

@RequiredArgsConstructor
public class PostCommandRepositoryCustomImpl implements PostCommandRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public long incrementViewCount(Long postId, long viewCount) {
        // 게시글 조회수를 원자적으로 증가
        // (조회 후 엔티티 수정하는 방식이 아니라, DB 레벨에서 원자적으로 update를 수행하여 동시성 환경에서 안전하게 처리하기 위함)
        return queryFactory
                .update(post)
                .set(post.viewCount, post.viewCount.add(viewCount))
                .where(post.id.eq(postId))
                .execute();
    }

    @Override
    public long incrementLikeCount(Long postId, int delta) {
        // 게시글 좋아요수를 원자적으로 증가
        // (조회 후 엔티티 수정하는 방식이 아니라, DB 레벨에서 원자적으로 update를 수행하여 동시성 환경에서 안전하게 처리하기 위함)
        return queryFactory
                .update(post)
                .set(post.likeCount, post.likeCount.add(delta))
                .where(post.id.eq(postId))
                .execute();
    }
}
