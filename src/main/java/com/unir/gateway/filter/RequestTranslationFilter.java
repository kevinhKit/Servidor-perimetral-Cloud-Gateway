package com.unir.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Este filtro simplemente deja pasar las peticiones POST sin intentar deserializar el body como GatewayRequest.
 */
@Component
@Slf4j
public class RequestTranslationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpMethod method = exchange.getRequest().getMethod();

        if (method == HttpMethod.POST) {
            log.info("Pasando request POST sin modificar el cuerpo a: {}", exchange.getRequest().getURI());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Alta prioridad para ejecutar antes de otros filtros si es necesario
    }
}
