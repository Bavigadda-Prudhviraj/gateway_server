package org.prudhviraj.gatewayserver.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(1) // Ensures this filter executes early in the filter chain (lower value = higher priority)
@Component // Marks this as a Spring-managed component to be picked up during component scanning
public class RequestTraceFilter implements GlobalFilter {

    // Logger for debugging purposes
    private static final Logger logger = LoggerFactory.getLogger(RequestTraceFilter.class);

    // Autowire the FilterUtility to manage correlation ID logic
    @Autowired
    FilterUtility filterUtility;

    /**
     * The main method of a GlobalFilter that intercepts every request.
     *
     * @param exchange - the current HTTP request/response context
     * @param chain - the rest of the filter chain
     * @return Mono<Void> - reactive chain continuation
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Get the headers from the incoming request
        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();

        // Check if correlation ID is already present
        if (isCorrelationIdPresent(requestHeaders)) {
            // Log the existing correlation ID for traceability
            logger.debug("saveMoneyBank-correlation-id found in RequestTraceFilter : {}",
                    filterUtility.getCorrelationId(requestHeaders));
        } else {
            // Generate a new correlation ID if not found
            String correlationID = generateCorrelationId();

            // Set the new correlation ID in the request header
            exchange = filterUtility.setCorrelationId(exchange, correlationID);

            // Log the generated correlation ID
            logger.debug("saveMoneyBank-correlation-id generated in RequestTraceFilter : {}", correlationID);
        }

        // Continue with the remaining filters in the chain
        return chain.filter(exchange);
    }

    /**
     * Utility method to check if correlation ID is present in the headers.
     *
     * @param requestHeaders - headers of the incoming request
     * @return true if correlation ID exists, false otherwise
     */
    private boolean isCorrelationIdPresent(HttpHeaders requestHeaders) {
        return filterUtility.getCorrelationId(requestHeaders) != null;
    }

    /**
     * Generates a unique correlation ID using UUID.
     *
     * @return a new UUID string
     */
    private String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString();
    }
}

