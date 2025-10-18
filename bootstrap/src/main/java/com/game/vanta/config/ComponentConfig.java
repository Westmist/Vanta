package com.game.vanta.config;

import com.game.vanta.redis.eventbus.config.RedisEventBusConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.PatternTopic;

import java.util.List;

@Configuration
public class ComponentConfig {

    @Bean
    public RedisEventBusConfig redisEventBusConfig() {
        return new RedisEventBusConfig("com.game.vanta.event.handler",
            List.of(new PatternTopic("player:*")));
    }

}
