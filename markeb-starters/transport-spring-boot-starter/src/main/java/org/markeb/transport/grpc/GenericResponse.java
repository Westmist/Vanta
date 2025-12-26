package org.markeb.transport.grpc;

import com.google.protobuf.ByteString;

/**
 * 通用 RPC 响应
 */
public final class GenericResponse {

    private final ByteString payload;
    private final int code;
    private final String message;

    private GenericResponse(Builder builder) {
        this.payload = builder.payload;
        this.code = builder.code;
        this.message = builder.message;
    }

    public ByteString getPayload() {
        return payload;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static GenericResponse getDefaultInstance() {
        return newBuilder().build();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .setPayload(payload)
                .setCode(code)
                .setMessage(message);
    }

    public static final class Builder {
        private ByteString payload = ByteString.EMPTY;
        private int code = 0;
        private String message = "";

        public Builder setPayload(ByteString payload) {
            this.payload = payload;
            return this;
        }

        public Builder setCode(int code) {
            this.code = code;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public GenericResponse build() {
            return new GenericResponse(this);
        }
    }
}
