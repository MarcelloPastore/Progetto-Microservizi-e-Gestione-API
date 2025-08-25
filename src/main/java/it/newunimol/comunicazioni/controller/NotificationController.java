package it.newunimol.comunicazioni.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.newunimol.comunicazioni.model.Notifica;
import it.newunimol.comunicazioni.model.ReadStatus;
import it.newunimol.comunicazioni.repository.NotificaRepository;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificaRepository notificaRepository;

    @Autowired
    public NotificationController(NotificaRepository notificaRepository) {
        this.notificaRepository = notificaRepository;
    }

    // GET /
    @GetMapping
    public ResponseEntity<?> getNotifications(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                              @RequestParam(value = "readStatus", required = false) ReadStatus readStatus,
                                              @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        Page<Notifica> page = (readStatus == null)
                ? notificaRepository.findByUserId(userId, pageable)
                : notificaRepository.findByUserIdAndReadStatus(userId, readStatus, pageable);
        return ResponseEntity.ok(page);
    }

    // GET /unread/count
    @GetMapping("/unread/count")
    public ResponseEntity<?> unreadCount(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        long count = notificaRepository.countByUserIdAndReadStatus(userId, ReadStatus.UNREAD);
        return ResponseEntity.ok(count);
    }

    // PUT /{id}/read
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @PathVariable Long id) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        return notificaRepository.findById(id)
                .map(n -> {
                    if (!n.getUserId().equals(userId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accesso negato.");
                    }
                    if (n.getReadStatus() != ReadStatus.READ) {
                        n.setReadStatus(ReadStatus.READ);
                        notificaRepository.save(n);
                    }
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notifica non trovata."));
    }

    // PUT /read-all
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllRead(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Header X-User-Id mancante (stub auth).");
        }
        List<Notifica> unread = notificaRepository.findByUserIdAndReadStatus(userId, ReadStatus.UNREAD);
        if (!unread.isEmpty()) {
            unread.forEach(n -> n.setReadStatus(ReadStatus.READ));
            notificaRepository.saveAll(unread);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
