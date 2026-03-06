package study.blog.post.infrastructure.redis;

public final class ViewCountRedisKeyGenerator {
    private ViewCountRedisKeyGenerator(){}

    public static String generateViewCountKey(Long postId){
        return "post:view:count:" + postId;
    }

    public static String generateDeDuplicationKey(Long postId, String identifier){
        return "post:view:dedup:" + postId + ":" + identifier;
    }

    public static long generateDeDuplicationTTL(){
        return 10;
    }
}
