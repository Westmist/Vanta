package org.markeb.net.gateway;

import io.netty.channel.Channel;

/**
 * 简化在登录阶段绑定区服ID到 Channel。
 */
public final class GatewaySessionUtil {

    private GatewaySessionUtil() {
    }

    public static void bindZone(Channel channel, String zoneId) {
        channel.attr(GatewayAttributes.ZONE_ID).set(zoneId);
    }
}

