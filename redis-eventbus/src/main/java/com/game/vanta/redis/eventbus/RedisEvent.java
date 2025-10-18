package com.game.vanta.redis.eventbus;


/**
 * 所有Redis事件的基类
 */
public interface RedisEvent {

    String channel();

}
