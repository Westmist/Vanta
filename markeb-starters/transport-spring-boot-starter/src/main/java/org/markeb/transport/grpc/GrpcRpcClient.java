package org.markeb.transport.grpc;

import org.markeb.transport.RpcClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 基于 gRPC 的 RPC 客户端实现
 */
public class GrpcRpcClient implements RpcClient {

    private static final Logger log = LoggerFactory.getLogger(GrpcRpcClient.class);

    private final ConcurrentHashMap<String, ManagedChannel> channels = new ConcurrentHashMap<>();
    private final int timeout;

    public GrpcRpcClient(int timeout) {
        this.timeout = timeout;
    }

    private ManagedChannel getChannel(String target) {
        return channels.computeIfAbsent(target, t -> {
            String[] parts = t.split(":");
            String host = parts[0];
            int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9090;

            log.info("Creating gRPC channel to {}:{}", host, port);
            return ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext()
                    .build();
        });
    }

    @Override
    public byte[] call(String target, String service, String method, byte[] request) {
        ManagedChannel channel = getChannel(target);
        GenericGrpc.GenericBlockingStub stub = GenericGrpc.newBlockingStub(channel);

        GenericRequest grpcRequest = GenericRequest.newBuilder()
                .setService(service)
                .setMethod(method)
                .setPayload(com.google.protobuf.ByteString.copyFrom(request))
                .build();

        GenericResponse response = stub.call(grpcRequest);
        return response.getPayload().toByteArray();
    }

    @Override
    public CompletableFuture<byte[]> callAsync(String target, String service, String method, byte[] request) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();

        ManagedChannel channel = getChannel(target);
        GenericGrpc.GenericStub stub = GenericGrpc.newStub(channel);

        GenericRequest grpcRequest = GenericRequest.newBuilder()
                .setService(service)
                .setMethod(method)
                .setPayload(com.google.protobuf.ByteString.copyFrom(request))
                .build();

        stub.call(grpcRequest, new StreamObserver<GenericResponse>() {
            @Override
            public void onNext(GenericResponse response) {
                future.complete(response.getPayload().toByteArray());
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
            }
        });

        return future;
    }

    @Override
    public void shutdown() {
        channels.forEach((target, channel) -> {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                channel.shutdownNow();
            }
        });
        channels.clear();
    }
}

