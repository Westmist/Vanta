package com.game.vanta.persistent.producer;

import org.apache.rocketmq.client.producer.SendCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPersistentMqSendCallback implements SendCallback {

  private static final Logger log = LoggerFactory.getLogger(DefaultPersistentMqSendCallback.class);

  @Override
  public void onSuccess(org.apache.rocketmq.client.producer.SendResult sendResult) {
    log.info("Successfully sent persistent message to MQ: {}", sendResult);
  }

  @Override
  public void onException(Throwable e) {
    log.error("Failed to send persistent message to MQ", e);
  }
}
