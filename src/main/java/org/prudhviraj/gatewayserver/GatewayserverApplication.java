package org.prudhviraj.gatewayserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class GatewayserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayserverApplication.class, args);
    }


    @Bean
    public RouteLocator saveMoneyBankRouteLocatorConfig(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                // Route for ACCOUNTS microservice
                .route(p -> p
                        // Match incoming path that starts with /savemoneybank/accounts/
                        .path("/savemoneybank/accounts/**")
                        // Apply filters to the request
                        .filters(f -> f
                                // Rewrite the path by removing the /savemoneybank/accounts prefix
                                // For example: /savemoneybank/accounts/details -> /details
                                .rewritePath("/savemoneybank/accounts/(?<segment>.*)", "/${segment}")
                                // Add a custom response header to show current time
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .circuitBreaker(cb -> cb.setName("accountsCircuitBreaker")))
                        // Forward the request to the ACCOUNTS microservice via service discovery (LoadBalancer)
                        .uri("lb://ACCOUNTS"))

                // Route for LOANS microservice
                .route(p -> p
                        .path("/savemoneybank/loans/**")
                        .filters(f -> f
                                .rewritePath("/savemoneybank/loans/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .circuitBreaker(cb -> cb.setName("loansCircuitBreaker")))
                        .uri("lb://LOANS"))

                // Route for CARDS microservice
                .route(p -> p
                        .path("/savemoneybank/cards/**")
                        .filters(f -> f
                                .rewritePath("/savemoneybank/cards/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .circuitBreaker(cb -> cb.setName("cardsCircuitBreaker")))
                        .uri("lb://CARDS"))

                // Build the complete route configuration
                .build();
    }


}
