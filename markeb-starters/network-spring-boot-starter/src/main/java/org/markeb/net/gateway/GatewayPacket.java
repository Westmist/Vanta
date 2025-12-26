package org.markeb.net.gateway;

/**
 * 网关内部统一的包格式，已解析出协议头。
 * <p>
 * 协议头格式（网关 -> 游戏服）：
 * length(4) + sessionId(4) + msgId(4) + seq(4) + body(n)
 * <p>
 * sessionId: 网关为每个前端连接分配的唯一标识，游戏服回包时原样返回，网关据此路由回正确的玩家连接。
 * seq: 客户端请求序号，用于请求-响应关联。
 */
public class GatewayPacket {

    private final int sessionId;  // 网关分配的连接标识
    private final int msgId;
    private final int seq;
    private final byte[] body;

    public GatewayPacket(int sessionId, int msgId, int seq, byte[] body) {
        this.sessionId = sessionId;
        this.msgId = msgId;
        this.seq = seq;
        this.body = body;
    }

    public int getSessionId() {
        return sessionId;
    }

    public int getMsgId() {
        return msgId;
    }

    public int getSeq() {
        return seq;
    }

    public byte[] getBody() {
        return body;
    }

    /**
     * 生成用于 pending 映射的唯一 key
     */
    public String getPendingKey() {
        return sessionId + ":" + seq;
    }
}

