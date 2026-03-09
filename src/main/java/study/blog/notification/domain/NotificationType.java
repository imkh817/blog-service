package study.blog.notification.domain;

public enum NotificationType {

    SUBSCRIBED {
        @Override
        public String createMessage(String actorName) {
            return actorName + "님이 구독하였습니다.";
        }
    },

    COMMENT {
        @Override
        public String createMessage(String actorName) {
            return actorName + "님이 댓글을 달았습니다.";
        }
    },

    LIKE {
        @Override
        public String createMessage(String actorName) {
            return actorName + "님이 좋아요를 눌렀습니다.";
        }
    };

    public abstract String createMessage(String actorName);
}