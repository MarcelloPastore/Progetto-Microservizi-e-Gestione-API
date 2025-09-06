package it.newunimol.comunicazioni.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.newunimol.comunicazioni.messaging.PlatformEventListener;

/**
 * Endpoint di test per generare eventi senza passare da RabbitMQ.
 */
@RestController
@RequestMapping("/api/v1/dev/events")
@Profile("dev")
@Tag(name = "Dev Events", description = "Simulazione eventi piattaforma (profilo dev) per generare notifiche")
public class EventDevController {

    private final PlatformEventListener listener;

    @Autowired
    public EventDevController(PlatformEventListener listener) {
        this.listener = listener;
    }

    public record TestEvent(String eventType, Long courseId, Long referenceId, String title, String message) {}

    @Operation(summary = "Simula evento", description = "Invia un evento fittizio che verrà processato come se provenisse da RabbitMQ.")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Accettato"),
        @ApiResponse(responseCode = "400", description = "eventType mancante")
    })
    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcast(@RequestBody TestEvent body) {
        if (body == null || body.eventType() == null) {
            return ResponseEntity.badRequest().body("eventType obbligatorio");
        }
        // Costruisco JSON manualmente per riusare lo stesso parsing del listener
        String json = "{" +
                "\"source\":\"DEV\"," +
                "\"eventType\":\"" + body.eventType() + "\"," +
                (body.courseId()!=null?"\"courseId\":"+body.courseId()+",":"") +
                (body.referenceId()!=null?"\"referenceId\":"+body.referenceId()+",":"") +
                (body.title()!=null?"\"title\":\""+body.title()+"\",":"") +
                (body.message()!=null?"\"message\":\""+body.message()+"\",":"") +
                "\"targetUserIds\":null}";
        listener.onMateriale(json); // chiama lo stesso flusso handleEvent
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Evento elaborato");
    }
}