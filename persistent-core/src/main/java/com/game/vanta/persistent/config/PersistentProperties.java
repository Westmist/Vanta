package com.game.vanta.persistent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "persistent")
public class PersistentProperties {

    private static final String DEFAULT_TOPIC = "persistent-topic";

    private String persistentEntityPackages;

    /**
     * MQ Topic 名称
     */
    private String topic = DEFAULT_TOPIC;

    public String getPersistentEntityPackages() {
        return persistentEntityPackages;
    }

    public void setPersistentEntityPackages(String persistentEntityPackages) {
        this.persistentEntityPackages = persistentEntityPackages;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = (topic == null || topic.isEmpty()) ? DEFAULT_TOPIC : topic;
    }

}
