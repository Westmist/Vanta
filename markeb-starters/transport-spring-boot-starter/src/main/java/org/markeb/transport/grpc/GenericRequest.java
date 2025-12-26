package org.markeb.transport.grpc;

import com.google.protobuf.ByteString;

/**
 * 通用 RPC 请求
 */
public final class GenericRequest {

    private final String service;
    private final String method;
    private final ByteString payload;

    private GenericRequest(Builder builder) {
        this.service = builder.service;
        this.method = builder.method;
        this.payload = builder.payload;
    }

    public String getService() {
        return service;
    }

    public String getMethod() {
        return method;
    }

    public ByteString getPayload() {
        return payload;
    }

    public static GenericRequest getDefaultInstance() {
        return newBuilder().build();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .setService(service)
                .setMethod(method)
                .setPayload(payload);
    }

    public static final class Builder {
        private String service = "";
        private String method = "";
        private ByteString payload = ByteString.EMPTY;

        public Builder setService(String service) {
            this.service = service;
            return this;
        }

        public Builder setMethod(String method) {
            this.method = method;
            return this;
        }

        public Builder setPayload(ByteString payload) {
            this.payload = payload;
            return this;
        }

        public GenericRequest build() {
            return new GenericRequest(this);
        }
    }
}
