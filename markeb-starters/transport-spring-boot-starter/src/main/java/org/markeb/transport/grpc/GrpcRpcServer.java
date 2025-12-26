package org.markeb.transport.grpc;

import org.markeb.transport.RpcHandler;
import org.markeb.transport.RpcServer;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 gRPC 的 RPC 服务端实现
 */
public class GrpcRpcServer implements RpcServer {

    private static final Logger log = LoggerFactory.getLogger(GrpcRpcServer.class);

    private final int port;
    private Server server;
    private final ConcurrentHashMap<String, RpcHandler> handlers = new ConcurrentHashMap<>();

    public GrpcRpcServer(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        try {
            server = ServerBuilder.forPort(port)
                    .addService(new GenericServiceImpl())
                    .build()
                    .start();

            log.info("gRPC server started on port {}", port);

            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        } catch (IOException e) {
            log.error("Failed to start gRPC server", e);
            throw new RuntimeException("Failed to start gRPC server", e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
            log.info("gRPC server stopped");
        }
    }

    @Override
    public void registerHandler(String service, RpcHandler handler) {
        handlers.put(service, handler);
        log.info("Registered RPC handler for service: {}", service);
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isRunning() {
        return server != null && !server.isShutdown();
    }

    private class GenericServiceImpl extends GenericGrpc.GenericImplBase {

        @Override
        public void call(GenericRequest request, StreamObserver<GenericResponse> responseObserver) {
            String service = request.getService();
            String method = request.getMethod();
            byte[] payload = request.getPayload().toByteArray();

            RpcHandler handler = handlers.get(service);
            if (handler == null) {
                responseObserver.onError(new RuntimeException("Service not found: " + service));
                return;
            }

            try {
                byte[] result = handler.handle(method, payload);
                GenericResponse response = GenericResponse.newBuilder()
                        .setPayload(com.google.protobuf.ByteString.copyFrom(result))
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (Exception e) {
                log.error("Error handling RPC request: {}.{}", service, method, e);
                responseObserver.onError(e);
            }
        }
    }
}

