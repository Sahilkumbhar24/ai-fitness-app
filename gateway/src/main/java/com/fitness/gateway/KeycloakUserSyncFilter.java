package com.fitness.gateway;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.fitness.gateway.user.RegisterRequest;
import com.fitness.gateway.user.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements WebFilter {

    private final UserService userService;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        // Don't intercept internal or user management endpoints
        String path = exchange.getRequest().getPath().value();
        if (path.startsWith("/api/users")) {
            return chain.filter(exchange);
        }

        try {
            RegisterRequest request = getUserDetails(token);
            String headerUserId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (headerUserId == null) {
                headerUserId = exchange.getRequest().getHeaders().getFirst("X-USER-ID");
            }
            String userId = headerUserId != null ? headerUserId : request.getKeycloakId();

            if (userId != null && request.getEmail() != null) {
                // Perform user sync in background so it never delays or blocks client requests
                userService.validateUser(userId)
                    .defaultIfEmpty(false)
                    .flatMap(isValid -> {
                        if (Boolean.FALSE.equals(isValid)) {
                            log.info("User {} does not exist in User Service. Background syncing...", userId);
                            return userService.registerUser(request);
                        }
                        return Mono.empty();
                    })
                    .onErrorResume(e -> {
                        log.warn("Background user sync skipped: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .subscribe();
            }

            return chain.filter(exchange);
        } catch (Exception e) {
            log.warn("Failed to process Keycloak token for user sync: {}", e.getMessage());
            return chain.filter(exchange);
        }
    }

    private RegisterRequest getUserDetails(String token) {
        try {
            String tokenWithoutBearer = token.replace("Bearer ", "").trim();
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            RegisterRequest request = new RegisterRequest();
            request.setEmail(claims.getStringClaim("email"));
            request.setKeycloakId(claims.getStringClaim("sub"));
            request.setFirstName(claims.getStringClaim("given_name"));
            request.setLastName(claims.getStringClaim("family_name"));
            request.setPassword("dummy@123");

            return request;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract user details from token", e);
        }
    }

}
