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

        try {
            RegisterRequest request = getUserDetails(token);
            String headerUserId = exchange.getRequest().getHeaders().getFirst("X-USER-ID");
            String userId = headerUserId != null ? headerUserId : request.getKeycloakId();

            return userService.validateUser(userId)
                    .flatMap(isValid -> {
                        if (Boolean.FALSE.equals(isValid)) {
                            log.info("User {} does not exist in User Service. Syncing/registering...", userId);
                            return userService.registerUser(request)
                                    .onErrorResume(e -> {
                                        log.warn("Could not sync user with User Service: {}", e.getMessage());
                                        return Mono.empty();
                                    })
                                    .then(chain.filter(exchange));
                        }
                        return chain.filter(exchange);
                    })
                    .onErrorResume(e -> {
                        log.warn("User validation check failed: {}", e.getMessage());
                        return chain.filter(exchange);
                    });
        } catch (Exception e) {
            log.error("Failed to extract or sync user from Keycloak token: {}", e.getMessage());
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
