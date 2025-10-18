package com.game.vanta.redis.eventbus.config;

import org.springframework.data.redis.listener.Topic;

import java.util.Collection;

public class RedisEventBusConfig {

    private final String eventActionPackages;

    private final Collection<? extends Topic> topics;

    public RedisEventBusConfig(String eventActionPackages, Collection<? extends Topic> topics) {
        this.eventActionPackages = eventActionPackages;
        this.topics = topics;
    }

    public String getEventActionPackages() {
        return eventActionPackages;
    }

    public Collection<? extends Topic> getTopics() {
        return topics;
    }
}
