package it.newunimol.comunicazioni.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.newunimol.comunicazioni.model.CourseSubscription;
import it.newunimol.comunicazioni.repository.CourseSubscriptionRepository;
import it.newunimol.comunicazioni.security.CurrentUserService;

@RestController
@SecurityRequirement(name = "bearer-jwt")
@RequestMapping("/api/v1/subscriptions")
public class CourseSubscriptionController {

    private final CourseSubscriptionRepository repository;
    private final CurrentUserService currentUserService;

    public CourseSubscriptionController(CourseSubscriptionRepository repository, CurrentUserService currentUserService) {
        this.repository = repository;
        this.currentUserService = currentUserService;
    }

    // Iscrive l'utente autenticato ad un corso
    @PostMapping("/course/{courseId}")
    public ResponseEntity<?> subscribe(@PathVariable Long courseId) {
        String userId = currentUserService.userId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean exists = repository.findByCourseId(courseId).stream().anyMatch(s -> s.getUserId().equals(userId));
        if (exists) return ResponseEntity.ok("Già iscritto");
        CourseSubscription saved = repository.save(new CourseSubscription(courseId, userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Lista iscrizioni dell'utente
    @GetMapping("/me")
    public ResponseEntity<?> mySubscriptions() {
        String userId = currentUserService.userId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<CourseSubscription> all = repository.findAll().stream().filter(s -> s.getUserId().equals(userId)).toList();
        return ResponseEntity.ok(all);
    }
}