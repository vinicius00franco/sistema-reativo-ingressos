package br.com.alura.codechella.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ObservabilityFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(ObservabilityFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        // Micrometer/Brave capturam trace_id e span_id automaticamente via MDC
        log.info(">>> REQUEST: {} {} | Params: {}",
            method, path, exchange.getRequest().getQueryParams());

        return chain.filter(exchange)
            .doOnSuccess(v -> {
                int statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value() : 0;
                log.info("<<< RESPONSE: {} {} | Status: {}", method, path, statusCode);
            })
            .doOnError(error -> {
                log.error("<<< ERROR: {} {} | Error: {}", method, path, error.getMessage());
            });
    }
}