package ch.kafkademo.transaction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures the service as an OAuth 2.0 / OpenID Connect resource server.
 *
 * <p>Every request must carry a valid JWT Bearer token issued by the configured
 * identity provider (Keycloak). The token is validated against the issuer's JWKS
 * endpoint (signature, expiry, issuer) automatically by Spring Security based on
 * {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}.
 *
 * <p>Writing a transaction additionally requires the {@code transactions:write}
 * OAuth 2.0 scope, which Spring maps to the {@code SCOPE_transactions:write}
 * authority.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless REST API secured with bearer tokens – no CSRF cookies/sessions.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Health/readiness probes are public so orchestrators can poll them.
                        .requestMatchers("/actuator/health/**").permitAll()
                        // Creating a transaction requires the dedicated write scope.
                        .requestMatchers(HttpMethod.POST, "/api/transactions")
                        .hasAuthority("SCOPE_transactions:write")
                        // Everything else just needs a valid token.
                        .anyRequest().authenticated())
                // Validate incoming JWT access tokens.
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}


