package it.newunimol.comunicazioni;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@Profile("test")
public class TestSecurityConfig {

    // Decoder fittizio per evitare parsing della chiave
    @Bean
    @Primary
    JwtDecoder jwtDecoder() {
        return token -> {
            Map<String,Object> claims = Map.of(
                    "sub", "test-user",
                    "role", "student",
                    "iat", Instant.now().getEpochSecond()
            );
            org.springframework.security.oauth2.jwt.Jwt jwt = new org.springframework.security.oauth2.jwt.Jwt(
                    "dummy-token-value", // token value
                    Instant.now(), // issued at
                    Instant.now().plusSeconds(3600), // expires at
                    Map.of("alg", "none"), // headers
                    claims // claims
            );
            Authentication auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
            return jwt;
        };
    }

    @Bean
    @Primary
    SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {}));
        return http.build();
    }
}
