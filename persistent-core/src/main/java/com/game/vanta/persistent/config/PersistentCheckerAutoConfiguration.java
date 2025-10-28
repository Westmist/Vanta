package com.game.vanta.persistent.config;

import com.game.vanta.persistent.checker.*;
import com.game.vanta.persistent.checker.abs.CheckRunner;
import com.game.vanta.persistent.checker.abs.IChecker;
import com.mongodb.client.MongoClient;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQConsumer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.apache.rocketmq.spring.support.RocketMQListenerContainer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.List;

@Configuration
@AutoConfigureAfter(name = "org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration")
public class PersistentCheckerAutoConfiguration {

    @Bean
    public MongoConnectivityChecker mongoConnectivityChecker(
        MongoClient mongoClient,
        MongoProperties mongoProperties) {
        return new MongoConnectivityChecker(mongoClient, mongoProperties);
    }

    @Bean
    public RedisConnectivityChecker redisConnectivityChecker(
        RedisConnectionFactory redisConnectionFactory) {
        return new RedisConnectivityChecker(redisConnectionFactory);
    }

    @Bean
    @ConditionalOnBean(DefaultMQProducer.class)
    public RocketMQProducerChecker rocketMQProducerCheckerocketMQProducerChecker(
        DefaultMQProducer rocketMQProducer) {
        return new RocketMQProducerChecker(rocketMQProducer);
    }

    @Bean
    public RocketMQConsumerChecker rocketMQConsumerChecker(
        List<DefaultRocketMQListenerContainer> listenerContainers) {
        return new RocketMQConsumerChecker(listenerContainers);
    }

    @Bean
    @ConditionalOnBean(IChecker.class)
    public CheckRunner checkRunner(
        List<IChecker> checkers) {
        return new CheckRunner(checkers);
    }

}
