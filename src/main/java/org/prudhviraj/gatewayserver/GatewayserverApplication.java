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
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))

                        // Forward the request to the ACCOUNTS microservice via service discovery (LoadBalancer)
                        .uri("lb://ACCOUNTS"))

                // Route for LOANS microservice
                .route(p -> p
                        // Match incoming path that starts with /savemoneybank/loans/
                        .path("/savemoneybank/loans/**")

                        // Apply filters to the request
                        .filters(f -> f
                                // Remove /savemoneybank/loans prefix and forward the rest
                                .rewritePath("/savemoneybank/loans/(?<segment>.*)", "/${segment}")

                                // Add a custom response header to the response
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))

                        // Forward the request to the LOANS microservice via LoadBalancer
                        .uri("lb://LOANS"))

                // Route for CARDS microservice
                .route(p -> p
                        // Match incoming path that starts with /savemoneybank/cards/
                        .path("/savemoneybank/cards/**")

                        // Apply filters to the request
                        .filters(f -> f
                                // Remove /savemoneybank/cards prefix from the path
                                .rewritePath("/savemoneybank/cards/(?<segment>.*)", "/${segment}")

                                // Add a custom response header
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))

                        // Forward to CARDS microservice using LoadBalancer
                        .uri("lb://CARDS"))

                // Build the complete route configuration
                .build();
    }


}
