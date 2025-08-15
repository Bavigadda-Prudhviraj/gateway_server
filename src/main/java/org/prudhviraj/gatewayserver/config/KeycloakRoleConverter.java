package org.prudhviraj.gatewayserver.config;

import org.springframework.core.convert.converter.Converter;                  // Spring's Converter interface to transform one type into another
import org.springframework.security.core.GrantedAuthority;                  // Represents an authority (role/permission) granted to the user
import org.springframework.security.core.authority.SimpleGrantedAuthority;  // Simple implementation of GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt;                          // Represents a decoded JWT token

import java.util.ArrayList;     // For returning an empty list when no roles are found
import java.util.Collection;    // Generic interface for a group of GrantedAuthority objects
import java.util.List;          // Used for list of roles from Keycloak
import java.util.Map;           // For working with the claims map in the JWT
import java.util.stream.Collectors; // To convert a stream into a list

/**
 * Custom converter to extract Keycloak realm roles from a JWT token
 * and convert them into Spring Security GrantedAuthority objects.
 */
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    /**
     * Extracts the "realm_access" roles from the JWT and converts them into Spring Security authorities.
     * @param source The decoded JWT token
     * @return A collection of GrantedAuthority objects (Spring Security roles)
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        // Extract the "realm_access" claim from JWT (Keycloak puts roles here for realm-level access)
        Map<String, Object> realmAccess = (Map<String, Object>) source.getClaims().get("realm_access");

        // If "realm_access" is missing or empty, return an empty list (user has no realm-level roles)
        if (realmAccess == null || realmAccess.isEmpty()) {
            return new ArrayList<>();
        }

        // Extract the "roles" list from the realm_access map and convert each role string:
        // 1️⃣ Prefix each role with "ROLE_" (Spring Security standard for roles)
        // 2️⃣ Wrap each role string in a SimpleGrantedAuthority object
        // 3️⃣ Collect the results into a list
        Collection<GrantedAuthority> returnValue = ((List<String>) realmAccess.get("roles"))
                .stream()
                .map(roleName -> "ROLE_" + roleName)           // e.g., "user" → "ROLE_user"
                .map(SimpleGrantedAuthority::new)              // Convert string into GrantedAuthority
                .collect(Collectors.toList());                 // Collect into a List

        // Return the final list of authorities
        return returnValue;
    }
}
