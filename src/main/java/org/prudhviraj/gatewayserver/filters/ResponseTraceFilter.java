package org.prudhviraj.gatewayserver.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

@Configuration // Declares this class as a configuration class for Spring
public class ResponseTraceFilter {

    // Logger instance for logging response trace messages
    private static final Logger logger = LoggerFactory.getLogger(ResponseTraceFilter.class);

    // Inject the utility class that handles correlation ID operations
    @Autowired
    FilterUtility filterUtility;

    /**
     * This bean defines a GlobalFilter that runs **after** the request is processed
     * and **just before** the response is sent back to the client.
     */
    @Bean
    public GlobalFilter postGlobalFilter() {
        return (exchange, chain) -> {
            // Continue processing the chain of filters first
            return chain.filter(exchange)
                    // After processing is complete, do something in response phase
                    .then(Mono.fromRunnable(() -> {
                        // Extract headers from the original request
                        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();

                        // Get the correlation ID from the request headers
                        String correlationId = filterUtility.getCorrelationId(requestHeaders);

                        // Log that we're adding the correlation ID to the response
                        logger.debug("Updated the correlation id to the outbound headers: {}", correlationId);

                        // Add the correlation ID to the response headers
                        exchange.getResponse().getHeaders().add(filterUtility.CORRELATION_ID, correlationId);
                    }));
        };
    }
}

