package org.example;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.4.0)",
    comments = "Source: grid.proto")
public final class GridServiceGrpc {

  private GridServiceGrpc() {}

  public static final String SERVICE_NAME = "org.example.GridService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.example.TaskRequest,
      org.example.SubTaskRequest> METHOD_GET_SUB_TASK =
      io.grpc.MethodDescriptor.<org.example.TaskRequest, org.example.SubTaskRequest>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
          .setFullMethodName(generateFullMethodName(
              "org.example.GridService", "getSubTask"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.example.TaskRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.example.SubTaskRequest.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.example.Task,
      org.example.TaskRequest> METHOD_ADD_TASK =
      io.grpc.MethodDescriptor.<org.example.Task, org.example.TaskRequest>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "org.example.GridService", "addTask"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.example.Task.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.example.TaskRequest.getDefaultInstance()))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static GridServiceStub newStub(io.grpc.Channel channel) {
    return new GridServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static GridServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new GridServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static GridServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new GridServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class GridServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<org.example.TaskRequest> getSubTask(
        io.grpc.stub.StreamObserver<org.example.SubTaskRequest> responseObserver) {
      return asyncUnimplementedStreamingCall(METHOD_GET_SUB_TASK, responseObserver);
    }

    /**
     */
    public void addTask(org.example.Task request,
        io.grpc.stub.StreamObserver<org.example.TaskRequest> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ADD_TASK, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_GET_SUB_TASK,
            asyncBidiStreamingCall(
              new MethodHandlers<
                org.example.TaskRequest,
                org.example.SubTaskRequest>(
                  this, METHODID_GET_SUB_TASK)))
          .addMethod(
            METHOD_ADD_TASK,
            asyncUnaryCall(
              new MethodHandlers<
                org.example.Task,
                org.example.TaskRequest>(
                  this, METHODID_ADD_TASK)))
          .build();
    }
  }

  /**
   */
  public static final class GridServiceStub extends io.grpc.stub.AbstractStub<GridServiceStub> {
    private GridServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private GridServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GridServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new GridServiceStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.example.TaskRequest> getSubTask(
        io.grpc.stub.StreamObserver<org.example.SubTaskRequest> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(METHOD_GET_SUB_TASK, getCallOptions()), responseObserver);
    }

    /**
     */
    public void addTask(org.example.Task request,
        io.grpc.stub.StreamObserver<org.example.TaskRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ADD_TASK, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class GridServiceBlockingStub extends io.grpc.stub.AbstractStub<GridServiceBlockingStub> {
    private GridServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private GridServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GridServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new GridServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.example.TaskRequest addTask(org.example.Task request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ADD_TASK, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class GridServiceFutureStub extends io.grpc.stub.AbstractStub<GridServiceFutureStub> {
    private GridServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private GridServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GridServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new GridServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.example.TaskRequest> addTask(
        org.example.Task request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ADD_TASK, getCallOptions()), request);
    }
  }

  private static final int METHODID_ADD_TASK = 0;
  private static final int METHODID_GET_SUB_TASK = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final GridServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(GridServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ADD_TASK:
          serviceImpl.addTask((org.example.Task) request,
              (io.grpc.stub.StreamObserver<org.example.TaskRequest>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_SUB_TASK:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.getSubTask(
              (io.grpc.stub.StreamObserver<org.example.SubTaskRequest>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static final class GridServiceDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.example.Grid.getDescriptor();
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (GridServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new GridServiceDescriptorSupplier())
              .addMethod(METHOD_GET_SUB_TASK)
              .addMethod(METHOD_ADD_TASK)
              .build();
        }
      }
    }
    return result;
  }
}
