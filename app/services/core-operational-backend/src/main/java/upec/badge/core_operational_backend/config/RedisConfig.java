package upec.badge.core_operational_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import upec.badge.shared.model.Person;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Person> personRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Person> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Person.class));
        return template;
    }
}
