package com.game.vanta.persistent.mq;

import com.game.vanta.persistent.PersistentPool;
import com.game.vanta.persistent.config.PersistentProperties;
import com.game.vanta.persistent.dao.IPersistent;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;


@Component
public class PersistentMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(PersistentMessageProducer.class);

    private final PersistentPool persistentPool;

    private final RocketMQTemplate rocketMQTemplate;

    private final PersistentProperties persistentProperties;

    private final SendCallback persistentMqSendCallback;

    public PersistentMessageProducer(
            PersistentPool persistentPool,
            RocketMQTemplate rocketMQTemplate,
            PersistentProperties persistentProperties,
            SendCallback persistentMqSendCallback) {
        this.persistentPool = persistentPool;
        this.rocketMQTemplate = rocketMQTemplate;
        this.persistentProperties = persistentProperties;
        this.persistentMqSendCallback = persistentMqSendCallback;
    }

    public <T extends IPersistent> SendResult syncSendMqNotice(T data) {
        Message<PersistentMqNotice> mqMessage = createMqMessage(data);
        return rocketMQTemplate
                .syncSend(persistentProperties.getTopic(), mqMessage);
    }

    public <T extends IPersistent> void asyncSendMqNotice(T data) {
        Message<PersistentMqNotice> mqMessage = createMqMessage(data);
        rocketMQTemplate
                .asyncSend(persistentProperties.getTopic(),
                        mqMessage,
                        persistentMqSendCallback);
    }

    public <T extends IPersistent> Message<PersistentMqNotice> createMqMessage(T data) {
        String collectName = persistentPool.findCollectName(data.getClass());
        PersistentMqNotice mqNotice = new PersistentMqNotice();
        mqNotice.setCollectName(collectName);
        mqNotice.setId(data.getId());
        return MessageBuilder
                .withPayload(mqNotice)
                .setHeader("KEYS", data.getId())
                .build();
    }

}
