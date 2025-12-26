package org.markeb.persistent.queue.rocketmq;

import org.markeb.persistent.queue.*;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * RocketMQ 持久化队列实现
 */
public class RocketMQPersistentQueue implements PersistentQueue {

    private static final Logger log = LoggerFactory.getLogger(RocketMQPersistentQueue.class);

    private final RocketMQTemplate rocketMQTemplate;
    private final String topic;

    public RocketMQPersistentQueue(RocketMQTemplate rocketMQTemplate, String topic) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.topic = topic;
    }

    @Override
    public QueueType getType() {
        return QueueType.ROCKETMQ;
    }

    @Override
    public CompletableFuture<Void> sendAsync(PersistentMessage message) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Message<PersistentMessage> mqMessage = MessageBuilder
                .withPayload(message)
                .setHeader("KEYS", message.getEntityId())
                .build();

        rocketMQTemplate.asyncSend(topic, mqMessage, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.debug("Sent persistent message: {} -> {}", message.getEntityClass(), message.getEntityId());
                future.complete(null);
            }

            @Override
            public void onException(Throwable e) {
                log.error("Failed to send persistent message: {} -> {}",
                        message.getEntityClass(), message.getEntityId(), e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    @Override
    public void sendSync(PersistentMessage message) {
        Message<PersistentMessage> mqMessage = MessageBuilder
                .withPayload(message)
                .setHeader("KEYS", message.getEntityId())
                .build();

        SendResult result = rocketMQTemplate.syncSend(topic, mqMessage);
        log.debug("Sent persistent message sync: {} -> {}, result: {}",
                message.getEntityClass(), message.getEntityId(), result.getSendStatus());
    }

    @Override
    public void subscribe(PersistentMessageHandler handler) {
        // RocketMQ 的订阅通过 @RocketMQMessageListener 注解实现
        // 这里只是保存 handler 引用，实际订阅在配置类中完成
        log.info("RocketMQ subscription should be configured via @RocketMQMessageListener");
    }

    @Override
    public void start() {
        log.info("RocketMQ persistent queue started with topic: {}", topic);
    }

    @Override
    public void stop() {
        log.info("RocketMQ persistent queue stopped");
    }
}
