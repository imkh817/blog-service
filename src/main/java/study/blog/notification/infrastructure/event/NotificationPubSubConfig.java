package study.blog.notification.infrastructure.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class NotificationPubSubConfig {

    private final RedisConnectionFactory redisConnectionFactory;
    private final NotificationPubSubListener notificationPubSubListener;

    @Bean
    public RedisMessageListenerContainer notificationListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(
                notificationPubSubListener,
                new ChannelTopic(NotificationPubSubPublisher.CHANNEL)
        );
        return container;
    }
}