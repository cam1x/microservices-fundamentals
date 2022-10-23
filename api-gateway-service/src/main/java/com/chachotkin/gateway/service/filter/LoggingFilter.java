package com.chachotkin.gateway.service.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Set<URI> uris = exchange.getAttributeOrDefault(GATEWAY_ORIGINAL_REQUEST_URL_ATTR, Collections.emptySet());
        String originalUri = uris.isEmpty()
                ? exchange.getRequest().getURI().toString()
                : uris.iterator().next().toString();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        String routeId = route != null ? route.getId() : StringUtils.EMPTY;
        URI routeUri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        log.info(String.format(
                "Incoming request [%s] is routed to id [%s], uri [%s].",
                originalUri, routeId, routeUri)
        );
        return chain.filter(exchange);
    }
}
