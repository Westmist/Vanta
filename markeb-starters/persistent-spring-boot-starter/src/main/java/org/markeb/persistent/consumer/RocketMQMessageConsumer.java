package org.markeb.persistent.consumer;

import org.markeb.persistent.cache.CacheManager;
import org.markeb.persistent.queue.PersistentMessage;
import org.markeb.persistent.repository.RepositoryFactory;
import org.markeb.persistent.service.EntityMetadataRegistry;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;

/**
 * RocketMQ 消息消费者
 * 需要在应用中启用此消费者，可以通过继承或直接使用
 *
 * 使用方式：
 * 1. 在消费者应用中创建一个继承此类的 Bean
 * 2. 或者直接在配置中引入此类
 *
 * 注意：由于 @RocketMQMessageListener 注解需要在编译时确定 topic 和 consumerGroup，
 * 建议在消费者应用中创建自己的实现类
 *
 * 示例：
 * <pre>
 * @Component
 * @RocketMQMessageListener(
 *     topic = "${markeb.persistent.queue.topic:persistent-topic}",
 *     consumerGroup = "${rocketmq.consumer.group}",
 *     messageModel = MessageModel.CLUSTERING
 * )
 * public class MyPersistentConsumer extends RocketMQMessageConsumer {
 *     public MyPersistentConsumer(RepositoryFactory repositoryFactory,
 *                                  CacheManager cacheManager,
 *                                  EntityMetadataRegistry metadataRegistry) {
 *         super(repositoryFactory, cacheManager, metadataRegistry);
 *     }
 * }
 * </pre>
 */
public abstract class RocketMQMessageConsumer extends PersistentMessageConsumer
        implements RocketMQListener<PersistentMessage> {

    public RocketMQMessageConsumer(
            RepositoryFactory repositoryFactory,
            CacheManager cacheManager,
            EntityMetadataRegistry metadataRegistry) {
        super(repositoryFactory, cacheManager, metadataRegistry);
    }

    @Override
    public void onMessage(PersistentMessage message) {
        handleMessage(message);
    }

}

