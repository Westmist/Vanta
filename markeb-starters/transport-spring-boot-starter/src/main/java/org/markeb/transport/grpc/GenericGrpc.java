package org.markeb.transport.grpc;

import com.google.protobuf.ByteString;
import io.grpc.MethodDescriptor;
import io.grpc.stub.AbstractBlockingStub;
import io.grpc.stub.AbstractAsyncStub;
import io.grpc.stub.StreamObserver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * gRPC 通用服务定义
 * 用于服务间的通用 RPC 调用
 */
public final class GenericGrpc {

    private GenericGrpc() {}

    public static final String SERVICE_NAME = "markeb.transport.Generic";

    /**
     * GenericRequest 的自定义 Marshaller
     */
    private static final MethodDescriptor.Marshaller<GenericRequest> REQUEST_MARSHALLER =
            new MethodDescriptor.Marshaller<>() {
                @Override
                public InputStream stream(GenericRequest value) {
                    // 简单的二进制序列化: service长度(4) + service + method长度(4) + method + payload
                    byte[] serviceBytes = value.getService().getBytes(StandardCharsets.UTF_8);
                    byte[] methodBytes = value.getMethod().getBytes(StandardCharsets.UTF_8);
                    byte[] payloadBytes = value.getPayload().toByteArray();
                    
                    ByteBuffer buffer = ByteBuffer.allocate(4 + serviceBytes.length + 4 + methodBytes.length + payloadBytes.length);
                    buffer.putInt(serviceBytes.length);
                    buffer.put(serviceBytes);
                    buffer.putInt(methodBytes.length);
                    buffer.put(methodBytes);
                    buffer.put(payloadBytes);
                    
                    return new ByteArrayInputStream(buffer.array());
                }

                @Override
                public GenericRequest parse(InputStream stream) {
                    try {
                        byte[] data = stream.readAllBytes();
                        ByteBuffer buffer = ByteBuffer.wrap(data);
                        
                        int serviceLen = buffer.getInt();
                        byte[] serviceBytes = new byte[serviceLen];
                        buffer.get(serviceBytes);
                        
                        int methodLen = buffer.getInt();
                        byte[] methodBytes = new byte[methodLen];
                        buffer.get(methodBytes);
                        
                        byte[] payloadBytes = new byte[buffer.remaining()];
                        buffer.get(payloadBytes);
                        
                        return GenericRequest.newBuilder()
                                .setService(new String(serviceBytes, StandardCharsets.UTF_8))
                                .setMethod(new String(methodBytes, StandardCharsets.UTF_8))
                                .setPayload(ByteString.copyFrom(payloadBytes))
                                .build();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse GenericRequest", e);
                    }
                }
            };

    /**
     * GenericResponse 的自定义 Marshaller
     */
    private static final MethodDescriptor.Marshaller<GenericResponse> RESPONSE_MARSHALLER =
            new MethodDescriptor.Marshaller<>() {
                @Override
                public InputStream stream(GenericResponse value) {
                    // 简单的二进制序列化: code(4) + message长度(4) + message + payload
                    byte[] messageBytes = value.getMessage().getBytes(StandardCharsets.UTF_8);
                    byte[] payloadBytes = value.getPayload().toByteArray();
                    
                    ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + messageBytes.length + payloadBytes.length);
                    buffer.putInt(value.getCode());
                    buffer.putInt(messageBytes.length);
                    buffer.put(messageBytes);
                    buffer.put(payloadBytes);
                    
                    return new ByteArrayInputStream(buffer.array());
                }

                @Override
                public GenericResponse parse(InputStream stream) {
                    try {
                        byte[] data = stream.readAllBytes();
                        ByteBuffer buffer = ByteBuffer.wrap(data);
                        
                        int code = buffer.getInt();
                        
                        int messageLen = buffer.getInt();
                        byte[] messageBytes = new byte[messageLen];
                        buffer.get(messageBytes);
                        
                        byte[] payloadBytes = new byte[buffer.remaining()];
                        buffer.get(payloadBytes);
                        
                        return GenericResponse.newBuilder()
                                .setCode(code)
                                .setMessage(new String(messageBytes, StandardCharsets.UTF_8))
                                .setPayload(ByteString.copyFrom(payloadBytes))
                                .build();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse GenericResponse", e);
                    }
                }
            };

    private static final MethodDescriptor<GenericRequest, GenericResponse> CALL_METHOD =
            MethodDescriptor.<GenericRequest, GenericResponse>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Call"))
                    .setRequestMarshaller(REQUEST_MARSHALLER)
                    .setResponseMarshaller(RESPONSE_MARSHALLER)
                    .build();

    public static GenericBlockingStub newBlockingStub(io.grpc.Channel channel) {
        return new GenericBlockingStub(channel, io.grpc.CallOptions.DEFAULT);
    }

    public static GenericStub newStub(io.grpc.Channel channel) {
        return new GenericStub(channel, io.grpc.CallOptions.DEFAULT);
    }

    public static abstract class GenericImplBase implements io.grpc.BindableService {

        public void call(GenericRequest request, StreamObserver<GenericResponse> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(CALL_METHOD, responseObserver);
        }

        @Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
                    .addMethod(CALL_METHOD,
                            io.grpc.stub.ServerCalls.asyncUnaryCall(
                                    (request, responseObserver) -> call(request, responseObserver)))
                    .build();
        }
    }

    public static final class GenericBlockingStub extends AbstractBlockingStub<GenericBlockingStub> {

        private GenericBlockingStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected GenericBlockingStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new GenericBlockingStub(channel, callOptions);
        }

        public GenericResponse call(GenericRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), CALL_METHOD, getCallOptions(), request);
        }
    }

    public static final class GenericStub extends AbstractAsyncStub<GenericStub> {

        private GenericStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected GenericStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new GenericStub(channel, callOptions);
        }

        public void call(GenericRequest request, StreamObserver<GenericResponse> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(CALL_METHOD, getCallOptions()), request, responseObserver);
        }
    }
}
