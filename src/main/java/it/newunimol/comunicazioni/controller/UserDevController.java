package it.newunimol.comunicazioni.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.newunimol.comunicazioni.model.UserAccount;
import it.newunimol.comunicazioni.repository.UserAccountRepository;

@RestController
@RequestMapping("/api/v1/dev/users")
@Profile("dev")
@Tag(name = "Dev Users", description = "Gestione utenti fittizi per test broadcast notifiche (solo profilo dev)")
public class UserDevController {

    private final UserAccountRepository repository;

    @Autowired
    public UserDevController(UserAccountRepository repository) {
        this.repository = repository;
    }

    public record CreateUserRequest(String userId, String role) {}

    @Operation(summary = "Crea utente di test", description = "Crea utente se non esiste; se esiste restituisce quello presente.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creato"),
        @ApiResponse(responseCode = "200", description = "Già esistente"),
        @ApiResponse(responseCode = "400", description = "Dati mancanti")
    })
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateUserRequest body) {
        if (body == null || body.userId() == null || body.userId().isBlank() || body.role() == null || body.role().isBlank()) {
            return ResponseEntity.badRequest().body("userId e role obbligatori");
        }
        if (repository.existsById(body.userId())) {
            return ResponseEntity.ok(repository.findById(body.userId()));
        }
        UserAccount saved = repository.save(new UserAccount(body.userId(), body.role()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Lista utenti dev", description = "Elenca tutti gli utenti nella tabella user_account.")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public List<UserAccount> all() { return repository.findAll(); }

    public record UpdateRoleRequest(String role) {}

    @Operation(summary = "Aggiorna ruolo", description = "Aggiorna il ruolo di un utente esistente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Aggiornato"),
        @ApiResponse(responseCode = "400", description = "Role mancante"),
        @ApiResponse(responseCode = "404", description = "Utente non trovato")
    })
    @PatchMapping("/{userId}/role")
    public ResponseEntity<?> updateRole(@PathVariable String userId, @RequestBody UpdateRoleRequest body) {
        if (body == null || body.role() == null || body.role().isBlank()) {
            return ResponseEntity.badRequest().body("role obbligatorio");
        }
        var opt = repository.findById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utente non trovato");
        }
        var entity = opt.get();
        entity.setRole(body.role());
        repository.save(entity);
        return ResponseEntity.ok(entity);
    }
}
