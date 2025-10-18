package com.game.vanta.redis.eventbus;


@FunctionalInterface
public interface IRedisEventAction {

    void onEvent(RedisEvent event) throws Throwable;

}
