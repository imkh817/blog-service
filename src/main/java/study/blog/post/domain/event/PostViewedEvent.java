package study.blog.post.domain.event;

public record PostViewedEvent(
        Long memberId,
        Long postId,
        String ip
) {

    public String identifier(){
        if(memberId != null){
            return "user:" + memberId;
        }

        return "ip:" + ip;
    }
}
