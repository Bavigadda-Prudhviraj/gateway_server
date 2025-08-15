package org.prudhviraj.gatewayserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter; // Generic Spring Converter interface
import org.springframework.http.HttpMethod;               // Enum for HTTP methods (GET, POST, etc.)
import org.springframework.security.authentication.AbstractAuthenticationToken; // Base class for authentication tokens
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity; // Enables WebFlux-based security
import org.springframework.security.config.web.server.ServerHttpSecurity; // Main class to configure security for WebFlux
import org.springframework.security.oauth2.jwt.Jwt; // Represents a decoded JWT token
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter; // Converts JWT → Authentication
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter; // Adapts a blocking converter to reactive
import org.springframework.security.web.server.SecurityWebFilterChain; // The WebFlux security filter chain bean
import reactor.core.publisher.Mono; // Reactive type for returning zero or one value

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Main security configuration for the gateway server.
     * @param serverHttpSecurity The ServerHttpSecurity builder to configure rules.
     * @return The security filter chain bean.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
        serverHttpSecurity
                .authorizeExchange(exchange -> exchange
                        // 1️⃣ Allow all GET requests without authentication
                        .pathMatchers(HttpMethod.GET).permitAll()

                        // 2️⃣ Require "ROLE_ACCOUNTS" for account service routes
                        .pathMatchers("/savemoneybank/accounts/api/v1/accounts/**").hasRole("ACCOUNTS")

                        // 3️⃣ Require "ROLE_CARDS" for card service routes
                        .pathMatchers("/savemoneybank/cards/api/v1/cards/**").hasRole("CARDS")

                        // 4️⃣ Require "ROLE_LOANS" for loan service routes
                        .pathMatchers("/savemoneybank/loans/api/v1/loans/**").hasRole("LOANS")
                )
                // 5️⃣ Configure OAuth2 Resource Server to use JWT tokens
                .oauth2ResourceServer(oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec
                        // Use a custom converter that extracts Keycloak roles from JWT
                        .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
                );

        // 6️⃣ Disable CSRF since we are building a backend API (no browser form submissions)
        serverHttpSecurity.csrf(csrfSpec -> csrfSpec.disable());

        // 7️⃣ Build and return the configured security filter chain
        return serverHttpSecurity.build();
    }

    /**
     * Creates a reactive converter that extracts granted authorities from a JWT using our KeycloakRoleConverter.
     * This ensures roles from Keycloak ("realm_access") are converted into Spring Security roles.
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        // Create a standard (blocking) JWT authentication converter
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

        // Set a custom converter that pulls "roles" from the Keycloak token
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

        // Wrap it in a reactive adapter so it works in WebFlux
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}
