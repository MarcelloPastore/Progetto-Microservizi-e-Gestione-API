package it.newunimol.comunicazioni.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.newunimol.comunicazioni.dto.NuovoMessaggioRequestDTO;
import it.newunimol.comunicazioni.model.Messaggio;
import it.newunimol.comunicazioni.security.CurrentUserService;
import it.newunimol.comunicazioni.service.MessageService;

@RestController
@SecurityRequirement(name = "bearer-jwt")
@RequestMapping("/api/v1/messages")
public class CommunicationController {

    private final MessageService messageService;
    private final CurrentUserService currentUserService;

    @Autowired
    public CommunicationController(MessageService messageService, CurrentUserService currentUserService) {
        this.messageService = messageService;
        this.currentUserService = currentUserService;
    }

    // POST /
    @Operation(summary = "Invia un nuovo messaggio", description = "Crea e persiste un messaggio diretto dall'utente autenticato (mittente) al destinatario indicato. Genera automaticamente una notifica di tipo NEW_MESSAGE per il destinatario.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Messaggio creato",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Messaggio.class))),
        @ApiResponse(responseCode = "400", description = "Campi obbligatori mancanti"),
        @ApiResponse(responseCode = "401", description = "Token assente o non valido")
    })
    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody NuovoMessaggioRequestDTO body) {
        String authenticated = currentUserService.userId();
        if (authenticated == null || authenticated.isBlank()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato.");

        // Se il client fornisce senderId lo usiamo, altrimenti usiamo l'utente autenticato
        String effectiveSender = (body.senderId() != null && !body.senderId().isBlank()) ? body.senderId() : authenticated;

        if (body.receiverId() == null || body.receiverId().isBlank()
                || body.subject() == null || body.subject().isBlank()
                || body.body() == null || body.body().isBlank()) {
            return ResponseEntity.badRequest().body("Campi obbligatori mancanti.");
        }
        if (effectiveSender.equals(body.receiverId())) {
            return ResponseEntity.badRequest().body("Sender e receiver non possono coincidere.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.sendMessage(effectiveSender, body));
    }

    // GET /inbox
    @Operation(summary = "Messaggi ricevuti (inbox)", description = "Recupera i messaggi ricevuti dall'utente autenticato ordinati per data discendente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagina di messaggi",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    @GetMapping("/inbox")
    public ResponseEntity<?> getInbox(@Parameter(description = "Parametri di paginazione (default size=10, sort=timestamp,DESC)")
                      @PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        String userId = currentUserService.userId();
        if (userId == null || userId.isBlank()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato.");
        Page<Messaggio> page = messageService.inbox(userId, pageable);
        return ResponseEntity.ok(page);
    }

    // GET /sent
    @Operation(summary = "Messaggi inviati", description = "Recupera i messaggi inviati dall'utente autenticato.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagina di messaggi"),
        @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    @GetMapping("/sent")
    public ResponseEntity<?> getSent(@Parameter(description = "Parametri di paginazione (default size=10, sort=timestamp,DESC)")
                     @PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        String userId = currentUserService.userId();
        if (userId == null || userId.isBlank()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato.");
        Page<Messaggio> page = messageService.sent(userId, pageable);
        return ResponseEntity.ok(page);
    }

    // GET /{id}
    @Operation(summary = "Dettaglio messaggio", description = "Recupera un messaggio se l'utente è mittente o destinatario.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Messaggio trovato"),
        @ApiResponse(responseCode = "401", description = "Non autenticato"),
        @ApiResponse(responseCode = "404", description = "Messaggio non trovato o accesso negato")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        String userId = currentUserService.userId();
        if (userId == null || userId.isBlank()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato.");
        return messageService.findVisible(userId, id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Messaggio non trovato o accesso negato."));
    }

    // PUT /{id}/read
    @Operation(summary = "Segna messaggio come letto", description = "Imposta lo stato di lettura su READ se l'utente è il destinatario.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Aggiornato"),
        @ApiResponse(responseCode = "401", description = "Non autenticato"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato a modificare"),
        @ApiResponse(responseCode = "404", description = "Messaggio non trovato")
    })
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        String userId = currentUserService.userId();
        if (userId == null || userId.isBlank()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato.");
        boolean ok = messageService.markRead(userId, id);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo il destinatario può marcare come letto o messaggio inesistente.");
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // GET /course/{courseId}
    @Operation(summary = "Messaggi per corso", description = "Filtra i messaggi dell'utente autenticato relativi a uno specifico corso.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagina di messaggi"),
        @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getByCourse(@PathVariable Long courseId,
                     @Parameter(description = "Parametri di paginazione (default size=10, sort=timestamp,DESC)")
                     @PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        String userId = currentUserService.userId();
        if (userId == null || userId.isBlank()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato.");
        Page<Messaggio> page = messageService.byCourse(userId, courseId, pageable);
        return ResponseEntity.ok(page);
    }
}
