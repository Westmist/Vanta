package org.markeb.net.protocol;

/**
 * 协议类型枚举
 */
public enum ProtocolType {

    /**
     * 网关协议
     * 4 length + 4 messageId + 2 seq + 2 magicNum
     * 总协议头长度: 12 bytes
     */
    GATEWAY(12),

    /**
     * 游戏服协议
     * 4 length + 4 messageId + 2 seq + 2 gateId + 8 roleId + 8 conId
     * 总协议头长度: 28 bytes
     */
    GAME_SERVER(28);

    private final int headerLength;

    ProtocolType(int headerLength) {
        this.headerLength = headerLength;
    }

    public int getHeaderLength() {
        return headerLength;
    }
}

