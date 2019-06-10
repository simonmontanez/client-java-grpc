package co.sam.livecheck;

import io.grpc.*;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import co.sam.livecheck.LiveCheck.*;

public class LiveCheckerClient {

    private final ManagedChannel channel;
    private final LiveCheckerGrpc.LiveCheckerBlockingStub blockingStub;

    public LiveCheckerClient(String host, int port, String apiKey) {
        channel = ManagedChannelBuilder.forAddress(host,port).usePlaintext().build();
        Channel chApiKey = ClientInterceptors.intercept(channel,  new Interceptor(apiKey));
        blockingStub = LiveCheckerGrpc.newBlockingStub(chApiKey);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public String checkService() {

        LiveRequest liveRequest = LiveRequest.newBuilder().build();

        LiveReply reply = blockingStub.live(liveRequest);

        return reply.getMessage();
    }

    private static final class Interceptor implements ClientInterceptor {
        private final String apiKey;

        private static Logger LOGGER = Logger.getLogger("InfoLogging");

        private static Metadata.Key<String> API_KEY_HEADER =
                Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER);

        public Interceptor(String apiKey) {
            this.apiKey = apiKey;
        }

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                MethodDescriptor<ReqT,RespT> method, CallOptions callOptions, Channel next) {
            LOGGER.info("Intercepted " + method.getFullMethodName());
            ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);

            call = new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(call) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    if (apiKey != null && !apiKey.isEmpty()) {
                        LOGGER.info("Attaching API Key: " + apiKey);
                        headers.put(API_KEY_HEADER, apiKey);
                    }
                    super.start(responseListener, headers);
                }
            };
            return call;
        }
    }

}