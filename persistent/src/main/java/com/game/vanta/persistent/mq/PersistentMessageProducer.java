package com.game.vanta.persistent.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.vanta.persistent.PersistentPool;
import com.game.vanta.persistent.dao.IPersistent;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;


@Component
public class PersistentMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(PersistentMessageProducer.class);

    private final PersistentPool persistentPool;

    private final RocketMQTemplate rocketMQTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PersistentMessageProducer(
            PersistentPool persistentPool,
            RocketMQTemplate rocketMQTemplate) {
        this.persistentPool = persistentPool;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * 发送持久化消息
     */
    public <T extends IPersistent> void sendMqPost(T data) {
        String collectName = persistentPool.findCollectName(data.getClass());
        PersistentMqPost mqPost = new PersistentMqPost();
        mqPost.setCollectName(collectName);
        mqPost.setId(data.getId());
        // TODO: 后续考虑扩展 payload 字段

        String body;
        try {
            body = objectMapper.writeValueAsString(mqPost);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.debug("Sending persistent message: {}", body);

        rocketMQTemplate.syncSend(
                "persistent-topic",
                MessageBuilder.withPayload(body)
                        .setHeader("KEYS", data.getId())
                        .build()
        );
    }

}
