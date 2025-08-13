package org.prudhviraj.gatewayserver.filters;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import org.springframework.http.HttpHeaders;
import java.util.List;

@Component // Marks this class as a Spring-managed component so it can be auto-wired in filters or services
public class FilterUtility {

    // Constant header key for storing correlation ID
    public static final String CORRELATION_ID = "saveMoneyBank-correlation-id";

    /**
     * Extracts the correlation ID from the incoming request headers.
     *
     * @param requestHeaders - the HttpHeaders from the incoming request
     * @return the correlation ID if present, otherwise null
     */
    public String getCorrelationId(HttpHeaders requestHeaders) {
        // Check if the correlation ID header is present
        if (requestHeaders.get(CORRELATION_ID) != null) {
            // Get the list of values for the header
            List<String> requestHeaderList = requestHeaders.get(CORRELATION_ID);
            // Return the first value from the list (usually there's only one)
            return requestHeaderList.stream().findFirst().get();
        } else {
            return null;
        }
    }

    /**
     * Adds or overrides a custom header in the ServerWebExchange (reactive request).
     *
     * @param exchange - the current web exchange
     * @param name     - the name of the header to add
     * @param value    - the value of the header
     * @return the mutated ServerWebExchange with the new header
     */
    public ServerWebExchange setRequestHeader(ServerWebExchange exchange, String name, String value) {
        // Mutates the request to include the custom header and builds a new exchange
        return exchange.mutate()
                .request(exchange.getRequest().mutate().header(name, value).build())
                .build();
    }

    /**
     * Shortcut method specifically to set the correlation ID in the request header.
     *
     * @param exchange       - the current web exchange
     * @param correlationId  - the correlation ID to set
     * @return mutated ServerWebExchange with the correlation ID set in headers
     */
    public ServerWebExchange setCorrelationId(ServerWebExchange exchange, String correlationId) {
        return this.setRequestHeader(exchange, CORRELATION_ID, correlationId);
    }
}

