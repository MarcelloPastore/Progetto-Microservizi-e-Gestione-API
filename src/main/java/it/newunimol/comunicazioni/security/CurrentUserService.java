package it.newunimol.comunicazioni.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public String userId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getSubject(); // claim "sub"
        }
        return null;
    }

    public String role() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getClaim("role");
        }
        return null;
    }
}
