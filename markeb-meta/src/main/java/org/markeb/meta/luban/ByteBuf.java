package org.markeb.meta.luban;

import java.nio.charset.StandardCharsets;

/**
 * Luban 二进制数据读取缓冲区
 * 用于从 Luban 生成的 binary 格式数据中读取配置
 */
public class ByteBuf {

    private final byte[] bytes;
    private int readerIndex;

    public ByteBuf(byte[] bytes) {
        this.bytes = bytes;
        this.readerIndex = 0;
    }

    public int size() {
        return bytes.length;
    }

    public int readerIndex() {
        return readerIndex;
    }

    public void readerIndex(int index) {
        this.readerIndex = index;
    }

    public boolean readBool() {
        return bytes[readerIndex++] != 0;
    }

    public byte readByte() {
        return bytes[readerIndex++];
    }

    public short readShort() {
        return (short) ((bytes[readerIndex++] & 0xff) | ((bytes[readerIndex++] & 0xff) << 8));
    }

    public int readInt() {
        return (bytes[readerIndex++] & 0xff)
                | ((bytes[readerIndex++] & 0xff) << 8)
                | ((bytes[readerIndex++] & 0xff) << 16)
                | ((bytes[readerIndex++] & 0xff) << 24);
    }

    public long readLong() {
        return (bytes[readerIndex++] & 0xffL)
                | ((bytes[readerIndex++] & 0xffL) << 8)
                | ((bytes[readerIndex++] & 0xffL) << 16)
                | ((bytes[readerIndex++] & 0xffL) << 24)
                | ((bytes[readerIndex++] & 0xffL) << 32)
                | ((bytes[readerIndex++] & 0xffL) << 40)
                | ((bytes[readerIndex++] & 0xffL) << 48)
                | ((bytes[readerIndex++] & 0xffL) << 56);
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * 读取变长整数 (Varint)
     */
    public int readSize() {
        int result = 0;
        int shift = 0;
        while (true) {
            byte b = bytes[readerIndex++];
            result |= (b & 0x7f) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
            shift += 7;
        }
    }

    /**
     * 读取有符号变长整数 (ZigZag + Varint)
     */
    public int readSint() {
        int n = readSize();
        return (n >>> 1) ^ -(n & 1);
    }

    /**
     * 读取有符号长整数 (ZigZag + Varint)
     */
    public long readSlong() {
        long result = 0;
        int shift = 0;
        while (true) {
            byte b = bytes[readerIndex++];
            result |= (long) (b & 0x7f) << shift;
            if ((b & 0x80) == 0) {
                break;
            }
            shift += 7;
        }
        return (result >>> 1) ^ -(result & 1);
    }

    public String readString() {
        int len = readSize();
        if (len == 0) {
            return "";
        }
        String s = new String(bytes, readerIndex, len, StandardCharsets.UTF_8);
        readerIndex += len;
        return s;
    }

    public byte[] readBytes() {
        int len = readSize();
        if (len == 0) {
            return new byte[0];
        }
        byte[] result = new byte[len];
        System.arraycopy(bytes, readerIndex, result, 0, len);
        readerIndex += len;
        return result;
    }

    /**
     * 跳过指定字节数
     */
    public void skip(int count) {
        readerIndex += count;
    }

    /**
     * 是否还有可读数据
     */
    public boolean hasRemaining() {
        return readerIndex < bytes.length;
    }

    /**
     * 剩余可读字节数
     */
    public int remaining() {
        return bytes.length - readerIndex;
    }
}

