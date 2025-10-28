package com.game.vanta.persistent.checker;

import com.game.vanta.persistent.checker.abs.IChecker;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocketMQProducerChecker implements IChecker {

    private static final Logger log = LoggerFactory.getLogger(RocketMQProducerChecker.class);

    private final DefaultMQProducer rocketMQProducer;

    private final String healthCheckTopic = "health-check-topic";

    public RocketMQProducerChecker(DefaultMQProducer rocketMQProducer) {
        this.rocketMQProducer = rocketMQProducer;
    }

    @Override
    public String name() {
        return "RocketMQ-Producer";
    }

    @Override
    public void check() throws MQBrokerException, RemotingException, InterruptedException, MQClientException {
        String ns = rocketMQProducer.getNamesrvAddr();
        log.info("Checking RocketMQ producer, NameServer={}", ns);

        // 发送轻量测试消息
        SendResult sendResult = rocketMQProducer.send(new Message(
            healthCheckTopic, "ping".getBytes()
        ));
        log.info("RocketMQ test message sent: {}", sendResult);
    }

}
