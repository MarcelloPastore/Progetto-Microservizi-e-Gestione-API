package it.newunimol.comunicazioni.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.newunimol.comunicazioni.model.CourseSubscription;
import it.newunimol.comunicazioni.repository.CourseSubscriptionRepository;

/**
 * Controller SOLO per facilitare i test locali (inserire iscrizioni senza aprire il DB).
 * In un ambiente reale andrebbe rimosso o protetto.
 */
@RestController
@RequestMapping("/api/v1/dev/subscriptions")
@Profile("dev") // caricato solo con spring.profiles.active=dev
public class SubscriptionDevController {

    private final CourseSubscriptionRepository repository;

    @Autowired
    public SubscriptionDevController(CourseSubscriptionRepository repository) {
        this.repository = repository;
    }

    // DTO minimale usando record (Java 17)
    public static record CreateSubscriptionRequest(Long courseId, String userId) {}

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateSubscriptionRequest body) {
        if (body == null || body.courseId() == null || body.userId() == null || body.userId().isBlank()) {
            return ResponseEntity.badRequest().body("courseId e userId sono obbligatori");
        }
        CourseSubscription saved = repository.save(new CourseSubscription(body.courseId(), body.userId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/course/{courseId}")
    public List<CourseSubscription> byCourse(@PathVariable Long courseId) {
        return repository.findByCourseId(courseId);
    }

    @GetMapping
    public List<CourseSubscription> all() {
        return repository.findAll();
    }
}
