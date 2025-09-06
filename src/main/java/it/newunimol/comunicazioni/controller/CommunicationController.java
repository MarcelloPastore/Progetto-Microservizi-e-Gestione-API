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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.newunimol.comunicazioni.dto.NuovoMessaggioRequestDTO;
import it.newunimol.comunicazioni.model.Messaggio;
import it.newunimol.comunicazioni.service.MessageService;

@RestController
@RequestMapping("/api/v1/messages")
public class CommunicationController {

    private final MessageService messageService;

    @Autowired
    public CommunicationController(MessageService messageService) {
        this.messageService = messageService;
    }

    // POST /
    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                         @RequestBody NuovoMessaggioRequestDTO body) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        if (body.receiverId() == null || body.receiverId().isBlank()
                || body.subject() == null || body.subject().isBlank()
                || body.body() == null || body.body().isBlank()) {
            return ResponseEntity.badRequest().body("Campi obbligatori mancanti.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.sendMessage(userId, body));
    }

    // GET /inbox
    @GetMapping("/inbox")
    public ResponseEntity<?> getInbox(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        Page<Messaggio> page = messageService.inbox(userId, pageable);
        return ResponseEntity.ok(page);
    }

    // GET /sent
    @GetMapping("/sent")
    public ResponseEntity<?> getSent(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                     @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        Page<Messaggio> page = messageService.sent(userId, pageable);
        return ResponseEntity.ok(page);
    }

    // GET /{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                     @PathVariable Long id) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        return messageService.findVisible(userId, id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Messaggio non trovato o accesso negato."));
    }

    // PUT /{id}/read
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @PathVariable Long id) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        boolean ok = messageService.markRead(userId, id);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo il destinatario può marcare come letto o messaggio inesistente.");
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // GET /course/{courseId}
    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getByCourse(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                         @PathVariable Long courseId,
                                         @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        Page<Messaggio> page = messageService.byCourse(userId, courseId, pageable);
        return ResponseEntity.ok(page);
    }
}
