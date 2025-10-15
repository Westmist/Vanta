package com.game.vanta.persistent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "persistent")
public class PersistentProperties {

    private static final String DEFAULT_TOPIC = "persistent-topic";

    /** MQ Topic 名称 */
    private String topic = DEFAULT_TOPIC;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = (topic == null || topic.isEmpty()) ? DEFAULT_TOPIC : topic;
    }
}
