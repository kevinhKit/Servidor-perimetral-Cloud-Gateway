package com.unir.gateway.filter;

import com.unir.gateway.decorator.RequestDecoratorFactory;
import com.unir.gateway.model.GatewayRequest;
import com.unir.gateway.utils.RequestBodyExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestTranslationFilter implements GlobalFilter {

    private final RequestBodyExtractor requestBodyExtractor;
    private final RequestDecoratorFactory requestDecoratorFactory;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpMethod method = exchange.getRequest().getMethod();
        HttpHeaders headers = exchange.getRequest().getHeaders();

        // Solo interceptar peticiones POST con Content-Type definido
        if (method != HttpMethod.POST || headers.getContentType() == null) {
            log.info("Skipping filter: method={}, contentType={}", method, headers.getContentType());
            return chain.filter(exchange);
        }

        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    GatewayRequest request = requestBodyExtractor.getRequest(exchange, dataBuffer);
                    ServerHttpRequest mutatedRequest = requestDecoratorFactory.getDecorator(request);

                    // Actualiza la URI del request antes de continuar
                    exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, mutatedRequest.getURI());

                    if (request.getQueryParams() != null) {
                        request.getQueryParams().clear();
                    }

                    log.info("Proxying request: {} {}", mutatedRequest.getMethod(), mutatedRequest.getURI());

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
    }
}
