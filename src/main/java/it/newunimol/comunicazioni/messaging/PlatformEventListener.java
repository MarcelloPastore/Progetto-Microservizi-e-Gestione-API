package it.newunimol.comunicazioni.messaging;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.newunimol.comunicazioni.model.NotificationType;
import it.newunimol.comunicazioni.repository.UserAccountRepository; // utenti per broadcast
import it.newunimol.comunicazioni.service.NotificationService;

@Component
public class PlatformEventListener {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final UserAccountRepository userAccountRepository;

    public PlatformEventListener(ObjectMapper objectMapper,
                                 NotificationService notificationService,
                                 UserAccountRepository userAccountRepository) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
        this.userAccountRepository = userAccountRepository;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.materiale}")
    public void onMateriale(String raw) { handleEvent(raw); }

    @RabbitListener(queues = "${app.rabbitmq.queue.compiti}")
    public void onCompiti(String raw) { handleEvent(raw); }

    @RabbitListener(queues = "${app.rabbitmq.queue.esami}")
    public void onEsami(String raw) { handleEvent(raw); }

    private void handleEvent(String rawJson) {
        try {
            System.out.println("[EVENT] RAW=" + rawJson);
            PlatformEvent evt = parseEvent(rawJson);
            var type = mapType(evt.eventType());
            String title = evt.title() != null ? evt.title() : defaultTitle(type);
            String message = evt.message() != null ? evt.message() : defaultMessage(type);

            System.out.println("[EVENT] type=" + evt.eventType() + " courseId=" + evt.courseId() + " ref=" + evt.referenceId());

            if (evt.targetUserIds() != null && !evt.targetUserIds().isEmpty()) {
                var uniq = evt.targetUserIds().stream().filter(s -> s != null && !s.isBlank()).distinct().toList();
                if (!uniq.isEmpty()) {
                    System.out.println("[EVENT] Notifiche dirette a utenti: " + uniq);
                    notificationService.createBulkNotifications(uniq, type, title, message, evt.referenceId(), buildLink(type, evt.courseId(), evt.referenceId()));
                }
                return;
            }

            // Nuova logica broadcast: tutti gli utenti tranne i teacher
            var targets = userAccountRepository.findByRoleNotIgnoreCase("teacher").stream()
                    .map(u -> u.getUserId())
                    .distinct()
                    .toList();
            System.out.println("[EVENT] Broadcast -> utenti notificati (no teacher): " + targets.size());
            if (!targets.isEmpty()) {
                notificationService.createBulkNotifications(targets, type, title, message, evt.referenceId(), buildLink(type, evt.courseId(), evt.referenceId()));
            } else {
                System.out.println("[EVENT] Nessun utente disponibile per broadcast (aggiungerli via /api/v1/dev/users)");
            }
        } catch (Exception e) {
            System.err.println("Errore elaborando evento piattaforma: " + e.getMessage());
        }
    }

    private PlatformEvent parseEvent(String raw) throws Exception {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("Evento vuoto");
        if (trimmed.startsWith("{")) {
            // JSON standard
            return objectMapper.readValue(trimmed, PlatformEvent.class);
        }
        // Possibili formati legacy: key=value,key=value oppure key:value,key:value
        if (!trimmed.contains("{")) {
            String[] parts = trimmed.split("[,;]\s*");
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (String p : parts) {
                String[] kv = p.split("[=:]",2);
                if (kv.length==2) {
                    String k = kv[0].trim();
                    String v = kv[1].trim();
                    if (!first) json.append(',');
                    first = false;
                    boolean numeric = v.matches("[0-9]+");
                    if (numeric) {
                        json.append('"').append(k).append('\"').append(':').append(v);
                    } else {
                        // strip surrounding quotes if any
                        if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
                            v = v.substring(1, v.length()-1);
                        }
                        v = v.replace("\\","\\\\").replace("\"","\\\"");
                        json.append('"').append(k).append('\"').append(':').append('"').append(v).append('"');
                    }
                }
            }
            json.append('}');
            String built = json.toString();
            System.out.println("[EVENT][PARSE-FALLBACK] Converted to JSON: " + built);
            return objectMapper.readValue(built, PlatformEvent.class);
        }
        // Ultimo tentativo: parse generico
        JsonNode node = objectMapper.readTree(trimmed);
        return objectMapper.treeToValue(node, PlatformEvent.class);
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

    @JsonIgnoreProperties(ignoreUnknown = true)
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
