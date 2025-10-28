package com.game.vanta.persistent.checker;

import com.game.vanta.persistent.checker.abs.IChecker;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RocketMQConsumerChecker implements IChecker {

    private static final Logger log = LoggerFactory.getLogger(RocketMQConsumerChecker.class);

    private final List<DefaultRocketMQListenerContainer> listenerContainers;

    public RocketMQConsumerChecker(
        List<DefaultRocketMQListenerContainer> listenerContainers) {
        this.listenerContainers = listenerContainers;
    }

    @Override
    public String name() {
        return "RocketMQ-Consumer";
    }

    @Override
    public void check() throws MQClientException {
        for (DefaultRocketMQListenerContainer container : listenerContainers) {
            container.getConsumer().fetchSubscribeMessageQueues(container.getTopic());
            log.info("RocketMQ consumer check OK: topic={}, group={}", container.getTopic(), container.getConsumerGroup());
        }
    }

}
