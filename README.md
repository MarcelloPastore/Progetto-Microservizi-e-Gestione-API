# Microservizio Comunicazioni e Notifiche

**Autore**: MarcelloPastore
**Data Creazione**: 2025-05-23
**Data Ultima Modifica**: 2025-05-23

## Indice
1. [Panoramica](#panoramica)
2. [Tech Stack](#tech-stack)
3. [Modello Dati](#modello-dati)
   - [DTO (Data Transfer Objects)](#dto)
     - [MessaggioDTO](#messaggiodto)
     - [NotificaDTO](#notificadto)
     - [NuovoMessaggioRequestDTO](#nuovomessaggiorequestdto)
   - [Entità Principali JPA](#entità-principali-jpa)
     - [Messaggio (Message)](#messaggio-message)
     - [Notifica (Notification)](#notifica-notification)
   - [Enumerazioni](#enumerazioni)
     - [TipoNotifica (NotificationType)](#tiponotifica-notificationtype)
     - [StatoLettura (ReadStatus)](#statolettura-readstatus)
4. [API REST](#api-rest)
    - [Messaggi Endpoint](#messaggi-endpoint)
    - [Notifiche Endpoint](#notifiche-endpoint)
5. [Integrazione Microservizi Esterni](#integrazione-microservizi-esterni)
   - [Panoramica Generale](#panoramica-generale)
   - [RabbitMQ - Published Events](#rabbitmq---published-events)
   - [RabbitMQ - Consumed Events](#rabbitmq---consumed-events)
6. [Sicurezza e Autorizzazioni](#sicurezza-e-autorizzazioni)

---

## Panoramica
Questo Microservizio è responsabile della gestione dell'invio e della ricezione di messaggi tra utenti (studenti e docenti) e dell'invio di notifiche relative alle attività accademiche all'interno della piattaforma.

Le funzionalità principali includono:
- **(Studenti e Docenti)** Invio e ricezione di messaggi diretti all'interno del contesto di specifici corsi.
- **(Tutti gli Utenti)** Ricezione di notifiche automatiche relative a:
    - Nuovi compiti assegnati.
    - Nuovo materiale didattico caricato.
    - Pianificazione o aggiornamenti di esami.
    - Ricezione di nuovi messaggi.
    - Altri eventi rilevanti della piattaforma.

## Tech Stack
- **Framework**: Spring Boot
- **Linguaggio**: Java
- **Build Tool**: Maven
- **Database**: SQL MariaDB
  - *Motivazione*: Il prof ha detto così
- **Message Broker**: RabbitMQ (per la futura gestione asincrona delle notifiche)

## Modello Dati

### DTO (Data Transfer Objects)
_DTO utilizzati per trasferire dati tra il client e il server, e potenzialmente tra microservizi._

#### MessaggioDTO
Rappresenta un messaggio scambiato tra utenti.
```java
public class MessaggioDTO {
    private Long id;
    private Long senderId;
    private String senderName; // Nome visualizzato del mittente
    private Long receiverId;
    private String receiverName; // Nome visualizzato del destinatario
    private Long courseContextId; // ID del corso a cui il messaggio è associato (opzionale)
    private String subject;
    private String body;
    private LocalDateTime timestamp;
    private ReadStatus readStatus; // ENUM: UNREAD, READ
}
```

#### NotificaDTO
Rappresenta una notifica inviata a un utente.
```java
public class NotificaDTO {
    private Long id;
    private Long userId; // ID dell'utente che riceve la notifica
    private NotificationType type; // ENUM: e.g., NEW_ASSIGNMENT, NEW_MATERIAL, EXAM_SCHEDULED, NEW_MESSAGE, GENERAL
    private String title; // Titolo breve della notifica
    private String message; // Messaggio dettagliato della notifica
    private Long referenceId; // ID dell'entità a cui si riferisce la notifica (es. ID compito, ID messaggio)
    private String link; // Link diretto all'entità (es. /tasks/{referenceId}) (opzionale)
    private LocalDateTime timestamp;
    private ReadStatus readStatus; // ENUM: UNREAD, READ
}
```

#### NuovoMessaggioRequestDTO
DTO specifico per la creazione di un nuovo messaggio.
```java
public class NuovoMessaggioRequestDTO {
    private Long receiverId;
    private Long courseContextId; // Opzionale
    private String subject;
    private String body;
}
```

---

### Entità Principali JPA
*Tabelle nel database MariaDB che rappresentano le strutture dati principali del microservizio.*

#### Messaggio (Message)
   - `id` (BIGINT, PK) - ID univoco del messaggio
   - `sender_id` (BIGINT) - ID dell'utente mittente (riferimento a Microservizio Utenti)
   - `receiver_id` (BIGINT) - ID dell'utente destinatario (riferimento a Microservizio Utenti)
   - `course_context_id` (BIGINT, nullable) - ID del corso (riferimento a Microservizio Corsi)
   - `subject` (VARCHAR(255)) - Oggetto del messaggio
   - `body` (TEXT) - Contenuto del messaggio
   - `timestamp` (TIMESTAMP) - Data e ora di invio
   - `read_status` (VARCHAR(20)) - Stato di lettura (UNREAD, READ)

#### Notifica (Notification)
   - `id` (BIGINT, PK) - ID univoco della notifica
   - `user_id` (BIGINT) - ID dell'utente destinatario della notifica (riferimento a Microservizio Utenti)
   - `type` (VARCHAR(50)) - Tipo di notifica (NEW_ASSIGNMENT, NEW_MATERIAL, EXAM_SCHEDULED, NEW_MESSAGE, GENERAL)
   - `title` (VARCHAR(255)) - Titolo della notifica
   - `message` (TEXT) - Testo della notifica
   - `reference_id` (BIGINT, nullable) - ID dell'entità correlata (es. compito, materiale)
   - `link` (VARCHAR(255), nullable) - Link per accedere direttamente alla risorsa correlata
   - `timestamp` (TIMESTAMP) - Data e ora di creazione della notifica
   - `read_status` (VARCHAR(20)) - Stato di lettura (UNREAD, READ)

---

### Enumerazioni

#### TipoNotifica (NotificationType)
```java
public enum NotificationType {
    NEW_ASSIGNMENT,       // Nuovo compito assegnato
    NEW_MATERIAL,         // Nuovo materiale didattico caricato
    EXAM_SCHEDULED,       // Esame pianificato o aggiornato
    NEW_MESSAGE,          // Nuovo messaggio ricevuto
    ASSIGNMENT_SUBMITTED, // Compito consegnato (notifica per il docente)
    FEEDBACK_RECEIVED,    // Feedback ricevuto per un compito/esame (notifica per lo studente)
    COURSE_UPDATE,        // Aggiornamenti generali sul corso
    GENERAL               // Notifica generica
}
```

#### StatoLettura (ReadStatus)
```java
public enum ReadStatus {
    UNREAD,
    READ
}
```

---

## API REST

### Messaggi Endpoint

BasePath: `/api/v1/messages`

```bash
#############################################
# Invia un nuovo messaggio
# @func: sendMessage()
# @param: NuovoMessaggioRequestDTO messageRequest
# @return: ResponseEntity<MessaggioDTO>
#############################################
POST    /

#############################################
# Recupera la casella di posta in arrivo per l'utente autenticato
# (messaggi ricevuti, paginati e ordinati per data decrescente)
# @func: getInboxMessages()
# @param: Pageable pageable
# @return: ResponseEntity<Page<MessaggioDTO>>
#############################################
GET     /inbox

#############################################
# Recupera i messaggi inviati dall'utente autenticato
# (paginati e ordinati per data decrescente)
# @func: getSentMessages()
# @param: Pageable pageable
# @return: ResponseEntity<Page<MessaggioDTO>>
#############################################
GET     /sent

#############################################
# Recupera un messaggio specifico tramite ID
# (verifica che l'utente autenticato sia mittente o destinatario)
# @func: getMessageById()
# @param: Long messageId
# @return: ResponseEntity<MessaggioDTO>
#############################################
GET     /{messageId}

#############################################
# Segna un messaggio come letto
# @func: markMessageAsRead()
# @param: Long messageId
# @return: ResponseEntity<Void>
#############################################
PUT     /{messageId}/read

#############################################
# Recupera i messaggi relativi a un corso specifico
# (per l'utente autenticato, paginati)
# @func: getMessagesByCourse()
# @param: Long courseId
# @param: Pageable pageable
# @return: ResponseEntity<Page<MessaggioDTO>>
#############################################
GET     /course/{courseId}

# (Opzionale: DELETE /{messageId} - se si vuole permettere la cancellazione logica)
```

---

### Notifiche Endpoint

BasePath: `/api/v1/notifications`

```bash
#############################################
# Recupera le notifiche per l'utente autenticato
# (paginate e ordinate per data decrescente, opzionalmente filtrabili per stato lettura)
# @func: getUserNotifications()
# @param: Pageable pageable
# @param: ReadStatus readStatus (opzionale)
# @return: ResponseEntity<Page<NotificaDTO>>
#############################################
GET     /

#############################################
# Recupera il conteggio delle notifiche non lette per l'utente autenticato
# @func: getUnreadNotificationsCount()
# @param: none
# @return: ResponseEntity<Long>
#############################################
GET     /unread/count

#############################################
# Segna una notifica specifica come letta
# @func: markNotificationAsRead()
# @param: Long notificationId
# @return: ResponseEntity<Void>
#############################################
PUT     /{notificationId}/read

#############################################
# Segna tutte le notifiche non lette dell'utente come lette
# @func: markAllNotificationsAsRead()
# @param: none
# @return: ResponseEntity<Void>
#############################################
PUT     /read-all

# (Opzionale: DELETE /{notificationId} - se si vuole permettere la cancellazione logica)
```

---

## Integrazione Microservizi Esterni

### Panoramica Generale

Il microservizio **Comunicazioni e Notifiche** interagisce con i seguenti microservizi per recuperare informazioni contestuali e per essere triggerato da eventi di sistema:

- **Gestione Utenti e Ruoli**: Per ottenere ID e nomi degli utenti (mittenti/destinatari) e per indirizzare correttamente le notifiche.
- **Gestione Corsi**: Per associare i messaggi a un contesto di corso specifico e per notifiche relative ai corsi.
- **Gestione Compiti**: Per generare notifiche alla creazione di nuovi compiti o alla consegna da parte degli studenti.
- **Gestione Materiale Didattico**: Per generare notifiche al caricamento di nuovo materiale.
- **Gestione Esami**: Per generare notifiche relative alla pianificazione o modifica degli esami.
- **Valutazione e Feedback**: Per generare notifiche quando un feedback è disponibile per uno studente.

---

### RabbitMQ - Published Events

**Area in allestimento**

_(Questa sezione verrà dettagliata quando l'implementazione di RabbitMQ per gli eventi pubblicati da questo microservizio sarà completata. Potenziali eventi potrebbero includere `message.sent` o `notification.generated`.)_

---

### RabbitMQ - Consumed Events

**Area in allestimento**

_(Questa sezione verrà dettagliata quando l'implementazione di RabbitMQ per il consumo di eventi da altri microservizi sarà completata. Questo microservizio ascolterà eventi come `assignment.created`, `material.uploaded`, `exam.scheduled`, `feedback.provided` per generare notifiche appropriate.)_
