package org.prudhviraj.gatewayserver;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;


import java.time.Duration;
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
                                .circuitBreaker(cb -> cb.setName("accountsCircuitBreaker")
                                        .setFallbackUri("forward:/contactSupport")))// when circuit breaker is open means service is down this fallback uri will be called
                        // Forward the request to the ACCOUNTS microservice via service discovery (LoadBalancer)
                        .uri("lb://ACCOUNTS"))

                // Route for LOANS microservice
                .route(p -> p
                        .path("/savemoneybank/loans/**")
                        .filters(f -> f
                                .rewritePath("/savemoneybank/loans/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                // commented this line because we are implementing time out configuration is following circuit breaker design pattern
//                                .circuitBreaker(cb -> cb.setName("loansCircuitBreaker")
//                                        .setFallbackUri("forward:/contactSupport"))
                                        //retry configuration
                                        .retry(retryConfig -> retryConfig
                                                .setRetries(2) // Retry a maximum of 2 times before failing
                                                .setMethods(HttpMethod.GET) // Apply retry only for GET requests
                                                .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true) // Exponential backoff starting at 100ms, up to 1000ms, with multiplier 2
                                        )

                        )
                        .uri("lb://LOANS"))

                // Route for CARDS microservice
                .route(p -> p
                        .path("/savemoneybank/cards/**")
                        .filters(f -> f
                                .rewritePath("/savemoneybank/cards/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .circuitBreaker(cb -> cb.setName("cardsCircuitBreaker")
                                        .setFallbackUri("forward:/contactSupport"))
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())))
                        .uri("lb://CARDS"))

                // Build the complete route configuration
                .build();
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> customizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build()).build());
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // replenishRate, burstCapacity, requestedTokens
        return new RedisRateLimiter(1, 1, 1);
    }

    @Bean
    KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("user"))
                .defaultIfEmpty("anonymous");
    }
}
