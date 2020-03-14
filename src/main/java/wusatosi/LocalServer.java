package wusatosi;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalServer {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new DOHProxyVerticle());
    }

    static class DOHProxyVerticle extends AbstractVerticle {
        private AtomicInteger count = new AtomicInteger();

        @Override
        public void init(Vertx vertx, Context context) {
            super.init(vertx, context);
            int port = 2020;
            vertx
                    .createHttpServer()
                    .requestHandler(new DOHProxyProcessContext(count.incrementAndGet()))
                    .listen(port, (server) -> System.out.printf("server started at port %d%n", port));
        }

        private static class DOHProxyProcessContext implements Handler<HttpServerRequest> {
            DOHProxyProcessContext(int requestId) {
                this.requestId = requestId;
            }

            int requestId;

            @Override
            public void handle(HttpServerRequest request) {
                log("request hit");
                Context context = Vertx.currentContext();
                request.bodyHandler(body ->
                        context.executeBlocking(proxyRequest(body), genResponse(request.response())));
            }

            private void log(String message) {
                System.out.printf("[%d] %s%n", requestId, message);
            }

            private Handler<AsyncResult<Buffer>> genResponse(HttpServerResponse response) {
                return (wow) -> {
                    if (wow.failed()) {
                        response.setStatusCode(500).end();
                        log("proxy request failed, " + wow.cause());
                        wow.cause().printStackTrace();
                    }
                    response
                            .setStatusCode(200)
                            .end(wow.result());
                };
            }

            private Handler<Promise<Buffer>> proxyRequest(Buffer buffer) {
                return (wow) -> {
                    try {
                        byte[] bytes = DOHProxy.makeRequest(buffer.getBytes());
                        wow.complete(Buffer.buffer(bytes));
                    } catch (IOException e) {
                        wow.fail(e);
                    }
                };
            }

        }
    }
}
