package it.newunimol.comunicazioni.controller;

// import org.springframework.beans.factory.annotation.Autowired; // constructor injection, no annotation needed
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.newunimol.comunicazioni.model.Notifica;
import it.newunimol.comunicazioni.model.ReadStatus;
import it.newunimol.comunicazioni.security.CurrentUserService;
import it.newunimol.comunicazioni.service.NotificationService;
import it.newunimol.comunicazioni.service.UserRegistryService;

@RestController
@SecurityRequirement(name = "bearer-jwt")
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;
    private final UserRegistryService userRegistryService;

    @Autowired
    public NotificationController(NotificationService notificationService, CurrentUserService currentUserService, UserRegistryService userRegistryService) {
        this.notificationService = notificationService;
        this.currentUserService = currentUserService;
        this.userRegistryService = userRegistryService;
    }

    // GET /
    @Operation(summary = "Lista notifiche", description = "Recupera notifiche dell'utente autenticato con filtro opzionale per stato di lettura.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagina di notifiche"),
        @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    @GetMapping
    public ResponseEntity<?> getNotifications(@RequestParam(value = "readStatus", required = false) ReadStatus readStatus,
                          @Parameter(description = "Paginazione (default size=10, sort=timestamp,DESC)")
                          @PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        String userId = currentUserService.userId();
        if (userId == null || userId.isBlank()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato.");
    userRegistryService.ensureUser(userId, currentUserService.role());
    Page<Notifica> page = notificationService.getNotifications(userId, readStatus, pageable);
        return ResponseEntity.ok(page);
    }

    // GET /unread/count
    @Operation(summary = "Conteggio notifiche non lette", description = "Restituisce il numero di notifiche con stato UNREAD per l'utente autenticato.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Conteggio restituito"),
        @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    @GetMapping("/unread/count")
    public ResponseEntity<?> unreadCount() {
        String userId = currentUserService.userId();
        if (userId == null || userId.isBlank()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato.");
    userRegistryService.ensureUser(userId, currentUserService.role());
    return ResponseEntity.ok(notificationService.countUnread(userId));
    }

    // PUT /{id}/read
    @Operation(summary = "Segna notifica come letta", description = "Cambia stato READ per la notifica se appartiene all'utente.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Aggiornato"),
        @ApiResponse(responseCode = "401", description = "Non autenticato"),
        @ApiResponse(responseCode = "403", description = "Accesso negato"),
        @ApiResponse(responseCode = "404", description = "Notifica non trovata")
    })
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        String userId = currentUserService.userId();
        if (userId == null || userId.isBlank()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato.");
        boolean ok = notificationService.markRead(userId, id);
        if (!ok) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accesso negato o notifica inesistente.");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // PUT /read-all
    @Operation(summary = "Segna tutte come lette", description = "Marca tutte le notifiche UNREAD dell'utente come READ.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Aggiornato"),
        @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllRead() {
        String userId = currentUserService.userId();
        if (userId == null || userId.isBlank()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato.");
        notificationService.markAllRead(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
