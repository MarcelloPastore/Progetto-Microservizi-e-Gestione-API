package it.newunimol.comunicazioni.security;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.public-key}")
    private String publicKeyB64;

    @Bean
    JwtDecoder jwtDecoder() throws Exception {
        try {
            byte[] decoded = Base64.getDecoder().decode(publicKeyB64.trim());
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            PublicKey pk = KeyFactory.getInstance("RSA").generatePublic(spec);
            return NimbusJwtDecoder.withPublicKey((RSAPublicKey) pk).build();
        } catch (Exception e) {
            // Log minimale (manteniamo semplicità) e rilancio per far fallire il contesto chiaramente
            System.err.println("[JWT] Errore creazione JwtDecoder: " + e.getMessage());
            throw e;
        }
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaim("role");
            if (role == null || role.isBlank()) return java.util.List.of();
            GrantedAuthority ga = new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
            return java.util.List.of(ga);
        });
        return conv;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtConv) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/api/v1/dev/**").permitAll()
                .anyRequest().authenticated()
        );
        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConv)));
        return http.build();
    }
}
