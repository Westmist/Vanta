package org.markeb.net.gateway;

import io.netty.util.AttributeKey;

public final class GatewayAttributes {

    private GatewayAttributes() {
    }

    /**
     * 前端连接上记录区服 ID。
     */
    public static final AttributeKey<String> ZONE_ID = AttributeKey.valueOf("zoneId");

    /**
     * 前端连接的唯一标识（网关分配的 sessionId）。
     * 游戏服回包时会原样返回，网关据此路由回正确的玩家连接。
     */
    public static final AttributeKey<Integer> SESSION_ID = AttributeKey.valueOf("sessionId");
}

