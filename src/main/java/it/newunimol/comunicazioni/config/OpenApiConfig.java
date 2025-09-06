package it.newunimol.comunicazioni.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "Comunicazioni & Notifiche API",
                version = "1.1.0",
                description = "API per gestione messaggi privati e notifiche piattaforma (versione 1.1.0).\n\n" +
                        "Funzionalità principali:\n" +
                        "- Invio e ricezione messaggi tra utenti autenticati (JWT)\n" +
                        "- Notifiche automatiche generate da eventi RabbitMQ (materiale, compiti, esami, ecc.)\n" +
                        "- Consultazione, conteggio e marcatura notifiche come lette\n" +
                        "- Broadcast eventi a tutti gli utenti (eccetto ruolo 'teacher')\n\n" +
                        "Convenzioni:\n" +
                        "- Tutti gli endpoint (eccetto /actuator e dev profile) richiedono JWT Bearer valido\n" +
                        "- Campi temporali sono in formato ISO-8601 (LocalDateTime)\n" +
                        "- Paginazione: parametri ?page=0&size=10&sort=campo,ASC|DESC (default: ultimi 10 per timestamp discendente)\n\n" +
                        "Codici di errore comuni:\n" +
                        "- 400: richiesta non valida / campi mancanti\n" +
                        "- 401: token assente o non valido\n" +
                        "- 403: accesso negato a risorsa non appartenente all'utente\n" +
                        "- 404: risorsa non trovata\n",
                contact = @Contact(name = "Team Comunicazioni", email = "team-comunicazioni@example.com"),
                license = @License(name = "Proprietary - Internal Use Only")
        ),
        security = { @SecurityRequirement(name = "bearer-jwt") }
)
@SecurityScheme(
        name = "bearer-jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
    // Solo annotazioni; nessun codice necessario.
}
