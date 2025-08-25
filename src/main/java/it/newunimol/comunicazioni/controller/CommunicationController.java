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
import it.newunimol.comunicazioni.model.Notifica;
import it.newunimol.comunicazioni.model.NotificationType;
import it.newunimol.comunicazioni.model.ReadStatus;
import it.newunimol.comunicazioni.repository.MessaggioRepository;
import it.newunimol.comunicazioni.repository.NotificaRepository;

@RestController
@RequestMapping("/api/v1/messages")
public class CommunicationController {

    private final MessaggioRepository messaggioRepository;
    private final NotificaRepository notificaRepository;

    @Autowired
    public CommunicationController(MessaggioRepository messaggioRepository,
                                   NotificaRepository notificaRepository) {
        this.messaggioRepository = messaggioRepository;
        this.notificaRepository = notificaRepository;
    }

    // POST /
    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                         @RequestBody NuovoMessaggioRequestDTO body) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        if (body.getReceiverId() == null || body.getReceiverId().isBlank()
                || body.getSubject() == null || body.getSubject().isBlank()
                || body.getBody() == null || body.getBody().isBlank()) {
            return ResponseEntity.badRequest().body("Campi obbligatori mancanti.");
        }

        Messaggio m = new Messaggio(userId,
                body.getReceiverId(),
                body.getCourseContextId(),
                body.getSubject(),
                body.getBody());
        m.setReadStatus(ReadStatus.UNREAD);
        Messaggio salvato = messaggioRepository.save(m);

        // CREA NOTIFICA SEMPLICE PER IL DESTINATARIO
        Notifica notifica = new Notifica(
                body.getReceiverId(),
                NotificationType.NEW_MESSAGE,
                "Nuovo messaggio",
                body.getSubject() != null ? body.getSubject() : "Hai ricevuto un nuovo messaggio",
                salvato.getId(),           // referenceId = id messaggio
                "/messages/" + salvato.getId()
        );
        notificaRepository.save(notifica);

        return ResponseEntity.status(HttpStatus.CREATED).body(salvato);
    }

    // GET /inbox
    @GetMapping("/inbox")
    public ResponseEntity<?> getInbox(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        Page<Messaggio> page = messaggioRepository.findByReceiverId(userId, pageable);
        return ResponseEntity.ok(page);
    }

    // GET /sent
    @GetMapping("/sent")
    public ResponseEntity<?> getSent(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                     @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        Page<Messaggio> page = messaggioRepository.findBySenderId(userId, pageable);
        return ResponseEntity.ok(page);
    }

    // GET /{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                     @PathVariable Long id) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        return messaggioRepository.findById(id)
                .map(m -> {
                    if (!m.getSenderId().equals(userId) && !m.getReceiverId().equals(userId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accesso negato.");
                    }
                    return ResponseEntity.ok(m);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Messaggio non trovato."));
    }

    // PUT /{id}/read
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @PathVariable Long id) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        return messaggioRepository.findById(id)
                .map(m -> {
                    if (!m.getReceiverId().equals(userId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solo il destinatario può marcare come letto.");
                    }
                    if (m.getReadStatus() != ReadStatus.READ) {
                        m.setReadStatus(ReadStatus.READ);
                        messaggioRepository.save(m);
                    }
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Messaggio non trovato."));
    }

    // GET /course/{courseId}
    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getByCourse(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                         @PathVariable Long courseId,
                                         @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        Page<Messaggio> page = messaggioRepository.findByCourseAndUser(courseId, userId, pageable);
        return ResponseEntity.ok(page);
    }
}
