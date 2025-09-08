package it.newunimol.comunicazioni.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.newunimol.comunicazioni.security.CurrentUserService;
import it.newunimol.comunicazioni.service.UserRegistryService;

@RestController
@SecurityRequirement(name = "bearer-jwt")
@RequestMapping("/api/v1/me")
public class IdentityController {

    private final CurrentUserService currentUserService;
    private final UserRegistryService userRegistryService;

    @Autowired
    public IdentityController(CurrentUserService currentUserService, UserRegistryService userRegistryService) {
        this.currentUserService = currentUserService;
        this.userRegistryService = userRegistryService;
    }

    @Operation(summary = "Identità utente", description = "Restituisce userId e ruolo estratti dal token JWT.")
    @ApiResponse(responseCode = "200", description = "Identità", content = @Content(schema = @Schema(implementation = java.util.Map.class)))
    @GetMapping
    public ResponseEntity<?> me() {
        String userId = currentUserService.userId();
        if (userId == null) return ResponseEntity.status(401).body("Non autenticato");
    userRegistryService.ensureUser(userId, currentUserService.role());
    return ResponseEntity.ok(Map.of(
                "userId", userId,
                "role", currentUserService.role()
        ));
    }
}
