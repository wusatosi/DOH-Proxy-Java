package wusatosi;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

public class LocalServer {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Verticle());
    }

    static class Verticle extends AbstractVerticle {
        @Override
        public void init(Vertx vertx, Context context) {
            super.init(vertx, context);
            vertx
                    .createHttpServer()
                    .requestHandler(request -> {
                        System.out.println(LocalDateTime.now() + " request hit");
                        HttpServerResponse response = request.response();
                        request.bodyHandler(buffer -> {
                            Handler<Promise<Buffer>> makeRequestAsync = (wow) -> {
                                try {
                                    byte[] bytes1 = buffer.getBytes();
                                    System.out.println(Base64.getEncoder().encodeToString(bytes1));
                                    byte[] bytes = DOHProxy.makeRequest(bytes1);
                                    wow.complete(Buffer.buffer(bytes));
                                } catch (IOException e) {
                                    wow.fail(e);
                                }
                            };
                            Handler<AsyncResult<Buffer>> responseMaker = (wow) -> {
                                if (wow.failed()) {
                                    response.setStatusCode(500).end();
                                    System.out.println(LocalDateTime.now() + " failed, " + wow.cause());
                                    wow.cause().printStackTrace();
                                }
//                                response
//                                        .setStatusCode(200)
//                                        .end(wow.result());
                            };
                            vertx.executeBlocking(makeRequestAsync, responseMaker);
                            String s = "AACBgAABAAEAAAABBmdvb2dsZQNjb20AABwAAQZnb29nbGUDY29tAAAcAAEAAAD5ABAkBGgAQAUICQAAAAAAACAOAAApEAAAAAAAAAsACAAHAAEYAHychQ==";
                            response.setStatusCode(200)
                                    .end(Buffer.buffer(Base64.getDecoder().decode(s)));
                        });
                    }).listen(2020);
        }
    }
}
