package co.sam.livecheck;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;
import co.sam.livecheck.LiveCheck.*;

public class LiveCheckerClient {

    private final ManagedChannel channel;
    private final LiveCheckerGrpc.LiveCheckerBlockingStub blockingStub;

    public LiveCheckerClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host,port).usePlaintext().build());
    }

    private LiveCheckerClient(ManagedChannel channel){
        this.channel = channel;
        blockingStub = LiveCheckerGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public String checkService() {

        LiveRequest liveRequest = LiveRequest.newBuilder().build();

        LiveReply reply = blockingStub.live(liveRequest);

        return reply.getMessage();
    }


}