package br.com.alura.codechella.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class RequestResponseLoggingDecorator {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingDecorator.class);

    public static class RequestDecorator extends ServerHttpRequestDecorator {

        public RequestDecorator(ServerHttpRequest delegate) {
            super(delegate);
        }

        @Override
        public Flux<DataBuffer> getBody() {
            return super.getBody().doOnNext(buffer -> {
                byte[] bytes = new byte[buffer.readableByteCount()];
                buffer.read(bytes);
                buffer.readPosition(0);

                String body = new String(bytes, StandardCharsets.UTF_8);
                log.info("REQUEST BODY: {}", body);
            });
        }
    }

    public static class ResponseDecorator extends ServerHttpResponseDecorator {

        public ResponseDecorator(ServerHttpResponse delegate) {
            super(delegate);
        }

        @Override
        public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
            Flux<DataBuffer> buffer = Flux.from(body);
            return super.writeWith(buffer.doOnNext(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                dataBuffer.readPosition(0);

                String responseBody = new String(bytes, StandardCharsets.UTF_8);
                log.info("RESPONSE BODY: {}", responseBody);
            }));
        }
    }
}