package study.blog.post.infra;

public final class ViewCountRedisKeys {
    private ViewCountRedisKeys(){}

    public static String getViewCountKey(Long postId){
        return "post:view:count:" + postId;
    }

    public static String getDeDuplicationKey(Long postId, String identifier){
        return "post:view:dedup:" + postId + ":" + identifier;
    }

    public static long getDeDuplicationTTL(){
        return 10;
    }
}
