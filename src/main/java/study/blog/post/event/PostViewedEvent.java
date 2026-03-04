package study.blog.post.event;

public record PostViewedEvent(
        Long postId,
        Long memberId,
        String ip
) {

    public String identifier(){
        if(memberId != null){
            return "user:" + memberId;
        }

        return "ip:" + ip;
    }
}
