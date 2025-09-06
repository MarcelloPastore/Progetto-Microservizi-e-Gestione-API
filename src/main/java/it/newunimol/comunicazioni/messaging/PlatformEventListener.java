package it.newunimol.comunicazioni.messaging;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.newunimol.comunicazioni.model.NotificationType;
import it.newunimol.comunicazioni.repository.CourseSubscriptionRepository;
import it.newunimol.comunicazioni.service.NotificationService;

@Component
public class PlatformEventListener {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final CourseSubscriptionRepository subscriptionRepository;

    public PlatformEventListener(ObjectMapper objectMapper,
                                 NotificationService notificationService,
                                 CourseSubscriptionRepository subscriptionRepository) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
        this.subscriptionRepository = subscriptionRepository;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.materiale}")
    public void onMateriale(String raw) { handleEvent(raw); }

    @RabbitListener(queues = "${app.rabbitmq.queue.compiti}")
    public void onCompiti(String raw) { handleEvent(raw); }

    @RabbitListener(queues = "${app.rabbitmq.queue.esami}")
    public void onEsami(String raw) { handleEvent(raw); }

    private void handleEvent(String rawJson) {
        try {
            PlatformEvent evt = objectMapper.readValue(rawJson, PlatformEvent.class);
            var type = mapType(evt.eventType());
            String title = evt.title() != null ? evt.title() : defaultTitle(type);
            String message = evt.message() != null ? evt.message() : defaultMessage(type);

            if (evt.targetUserIds() != null && !evt.targetUserIds().isEmpty()) {
                var uniq = evt.targetUserIds().stream().filter(s -> s != null && !s.isBlank()).distinct().toList();
                if (!uniq.isEmpty()) {
                    notificationService.createBulkNotifications(uniq, type, title, message, evt.referenceId(), buildLink(type, evt.courseId(), evt.referenceId()));
                }
                return;
            }

            if (evt.courseId() != null) {
                var users = subscriptionRepository.findByCourseId(evt.courseId()).stream()
                        .map(sub -> sub.getUserId())
                        .distinct()
                        .collect(Collectors.toList());
                if (!users.isEmpty()) {
                    notificationService.createBulkNotifications(users, type, title, message, evt.referenceId(), buildLink(type, evt.courseId(), evt.referenceId()));
                }
            }
        } catch (Exception e) {
            System.err.println("Errore elaborando evento piattaforma: " + e.getMessage());
        }
    }

    private NotificationType mapType(String eventType) {
        if (eventType == null) return NotificationType.GENERAL;
        return switch (eventType) {
            case "MATERIAL_UPLOADED" -> NotificationType.NEW_MATERIAL;
            case "ASSIGNMENT_CREATED" -> NotificationType.NEW_ASSIGNMENT;
            case "ASSIGNMENT_SUBMITTED" -> NotificationType.ASSIGNMENT_SUBMITTED;
            case "EXAM_SCHEDULED" -> NotificationType.EXAM_SCHEDULED;
            case "FEEDBACK_PROVIDED" -> NotificationType.FEEDBACK_RECEIVED;
            case "COURSE_UPDATED" -> NotificationType.COURSE_UPDATE;
            default -> NotificationType.GENERAL;
        };
    }

    private String defaultTitle(NotificationType type) {
        return switch (type) {
            case NEW_MATERIAL -> "Nuovo materiale";
            case NEW_ASSIGNMENT -> "Nuovo compito";
            case EXAM_SCHEDULED -> "Esame programmato";
            case ASSIGNMENT_SUBMITTED -> "Consegna compito";
            case FEEDBACK_RECEIVED -> "Nuovo feedback";
            case COURSE_UPDATE -> "Aggiornamento corso";
            case NEW_MESSAGE -> "Nuovo messaggio";
            default -> "Notifica";
        };
    }

    private String defaultMessage(NotificationType type) {
        return switch (type) {
            case NEW_MATERIAL -> "È stato caricato nuovo materiale didattico.";
            case NEW_ASSIGNMENT -> "È stato assegnato un nuovo compito.";
            case EXAM_SCHEDULED -> "È stato pianificato o aggiornato un esame.";
            case ASSIGNMENT_SUBMITTED -> "Un compito è stato consegnato.";
            case FEEDBACK_RECEIVED -> "Hai ricevuto un nuovo feedback.";
            case COURSE_UPDATE -> "Ci sono aggiornamenti sul corso.";
            case NEW_MESSAGE -> "Hai ricevuto un nuovo messaggio.";
            default -> "Nuova notifica.";
        };
    }

    private String buildLink(NotificationType type, Long courseId, Long refId) {
        return switch (type) {
            case NEW_MATERIAL -> courseId != null ? "/courses/" + courseId + "/materials" : null;
            case NEW_ASSIGNMENT -> courseId != null ? "/courses/" + courseId + "/assignments" : null;
            case EXAM_SCHEDULED -> courseId != null ? "/courses/" + courseId + "/exams" : null;
            case ASSIGNMENT_SUBMITTED -> refId != null ? "/assignments/submissions/" + refId : null;
            case FEEDBACK_RECEIVED -> refId != null ? "/feedback/" + refId : null;
            case COURSE_UPDATE -> courseId != null ? "/courses/" + courseId : null;
            default -> null;
        };
    }

    static record PlatformEvent(
            String source,
            String eventType,
            Long courseId,
            Long referenceId,
            String title,
            String message,
            List<String> targetUserIds
    ) {}
}
