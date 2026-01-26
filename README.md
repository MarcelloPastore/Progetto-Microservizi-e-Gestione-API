# Microservizio Comunicazioni e Notifiche 
**Autore**: MarcelloPastore
**Data Creazione**: 2025-05-23
**Data Ultima Modifica**: 2025-09-08

## Indice
1. [Panoramica](#panoramica)
2. [Contesto Applicativo & Scope](#contesto-applicativo--scope)
3. [Tech Stack](#tech-stack)
4. [Modello Dati](#modello-dati)
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
5. [API REST](#api-rest)
6. [Integrazione Microservizi Esterni](#integrazione-microservizi-esterni)
7. [Sicurezza e Autorizzazioni](#sicurezza-e-autorizzazioni)
8. [Accesso Swagger / OpenAPI](#accesso-swagger--openapi)

---

## Panoramica (Versione 1.1.2)
Questo Microservizio è responsabile della gestione dell'invio e della ricezione di messaggi tra utenti (studenti e docenti) e dell'invio di notifiche relative alle attività accademiche all'interno della piattaforma.  

Le funzionalità principali includono:
- **(Studenti e Docenti)** Invio e ricezione di messaggi diretti all'interno del contesto di specifici corsi.
- **(Tutti gli Utenti)** Ricezione di notifiche automatiche relative a:
    - Nuovi compiti assegnati.
    - Nuovo materiale didattico caricato.
    - Pianificazione o aggiornamenti di esami.
    - Ricezione di nuovi messaggi.
    - Altri eventi rilevanti della piattaforma.

## Contesto Applicativo & Scope
Questo repository fa parte di una piattaforma universitaria più ampia composta da molteplici microservizi (Corsi, Materiale Didattico, Compiti, Esami, Utenti & Ruoli, Presenze, Feedback, Pianificazione Orari, Iscrizioni, Reportistica, Tasse, Help Desk, Biblioteca, ecc.).
Questo microservizio copre esclusivamente:
- Scambio di messaggi tra utenti (studenti / docenti) con riferimento opzionale a un corso.
- Generazione e consultazione di notifiche (es. nuova ricezione messaggio).
Tutto il resto (autenticazione reale, validazioni ruoli, eventi esterni, orchestrazione) è demandato ai microservizi dedicati o sarà oggetto di evoluzioni future.

## Tech Stack
- **Framework**: Spring Boot
- **Linguaggio**: Java
- **Build Tool**: Maven
- **Containerizzazione**: Docker & Docker Compose
- **CI/CD**: GitHub Actions
- **Quality Assurance**:
  - **JaCoCo**: Code Coverage
  - **Checkstyle**: Code Style Analysis
  - **SpotBugs**: Static Analysis for Bugs
  - **PMD**: Source Code Analyzer
- **Database**: SQL MariaDB
  - *Motivazione*: MariaDB è stato scelto come sistema di gestione di database relazionale (RDBMS) per questo microservizio in quanto è un database SQL open-source robusto, performante e ampiamente compatibile con MySQL. La familiarità con la sua architettura e il suo utilizzo pregresso nel contesto del progetto lo rendono una scelta efficiente e affidabile per la persistenza dei dati relativi a comunicazioni e notifiche.
- **Message Broker**: RabbitMQ (per la gestione asincrona delle notifiche)

## Setup e Avvio con Docker

Per avviare il progetto in locale utilizzando Docker, seguire questi passaggi:

1.  **Clonare il repository**.
2.  **Configurare le variabili d'ambiente**:
    Il file `.env` contenente le password non è incluso nel repository per sicurezza.
    Creare un file `.env` nella root del progetto copiando il template fornito:
    ```bash
    cp .env.example .env
    ```
    (Opzionale) Modificare il file `.env` con le proprie password se necessario.
3.  **Avviare i container**:
    ```bash
    docker-compose up --build
    ```
    Questo avvierà:
    - Il microservizio (porta 8080)
    - MariaDB (porta 3306)
    - RabbitMQ (porta 5672, UI gestione su 15672)

## CI/CD & Quality Assurance

Il progetto utilizza **GitHub Actions** per l'automazione dei processi di build, test e release.

### Workflow Configurati
1.  **Java CI with Maven** (`maven.yml`):
    - Si attiva ad ogni push o pull request su `main`.
    - Esegue la build del progetto.
    - Esegue i test unitari.
    - Esegue l'analisi statica del codice con Checkstyle, SpotBugs e PMD.
    - Genera il report di coverage con JaCoCo.

2.  **Create Release on Version Update** (`release.yml`):
    - Si attiva quando un commit message contiene la parola "update" (case-insensitive) e un numero di versione (es. `1.2.3`).
    - Esempio messaggio commit: `Update 1.2.3: fix bug importanti`.
    - Crea automaticamente una Release su GitHub taggata con la versione specificata (es. `v1.2.3`).

### Analisi Statica e Test
Per eseguire manualmente i controlli di qualità in locale:
```bash
./mvnw clean install
```
Questo comando eseguirà:
- Compilazione
- Test Unitari
- Checkstyle (verifica stile codice Google)
- PMD (analisi qualità codice)
- JaCoCo (report coverage in `target/site/jacoco/index.html`)

## Modello Dati

### DTO (Data Transfer Objects)
_DTO utilizzati per trasferire dati tra il client e il server, e potenzialmente tra microservizi._

#### MessaggioDTO
Rappresenta un messaggio scambiato tra utenti.
```java
public class MessaggioDTO {
    private Long id; // ID interno del messaggio
    private String senderId; // ID Utente (Stringa, da servizio Utenti)
    private String senderName; // Nome visualizzato del mittente (potenzialmente da servizio Utenti o contesto evento)
    private String receiverId; // ID Utente (Stringa, da servizio Utenti)
    private String receiverName; // Nome visualizzato del destinatario (potenzialmente da servizio Utenti)
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
    private Long id; // ID interno della notifica
    private String userId; // ID dell'utente che riceve la notifica (Stringa, da servizio Utenti)
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
    private String receiverId; // ID Utente destinatario (Stringa)
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
   - `sender_id` (VARCHAR(255)) - ID dell'utente mittente (riferimento a Microservizio Utenti)
   - `receiver_id` (VARCHAR(255)) - ID dell'utente destinatario (riferimento a Microservizio Utenti)
   - `course_context_id` (BIGINT, nullable) - ID del corso (riferimento a Microservizio Corsi)
   - `subject` (VARCHAR(255)) - Oggetto del messaggio
   - `body` (TEXT) - Contenuto del messaggio
   - `timestamp` (TIMESTAMP) - Data e ora di invio
   - `read_status` (VARCHAR(20)) - Stato di lettura (UNREAD, READ)

#### Notifica (Notification)
   - `id` (BIGINT, PK) - ID univoco della notifica
   - `user_id` (VARCHAR(255)) - ID dell'utente destinatario della notifica (riferimento a Microservizio Utenti)
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

- **Gestione Utenti e Ruoli**: Per ottenere ID utente (Stringa), ruoli (es. `user`, `teach`, `admin`) dal token JWT, e potenzialmente per recuperare dettagli utente come nome e cognome tramite API.
- **Gestione Corsi**: Per associare i messaggi a un contesto di corso specifico e per notifiche relative ai corsi.
- **Gestione Compiti**: Per generare notifiche alla creazione di nuovi compiti o alla consegna da parte degli studenti.
- **Gestione Materiale Didattico**: Per generare notifiche al caricamento di nuovo materiale.
- **Gestione Esami**: Per generare notifiche relative alla pianificazione o modifica degli esami.

---

### RabbitMQ - Published Events

**Area in allestimento**

_(Questa sezione verrà dettagliata quando l'implementazione di RabbitMQ per gli eventi pubblicati da questo microservizio sarà completata. Potenziali eventi potrebbero includere `message.sent` o `notification.generated`.)_

---

### RabbitMQ - Consumed Events

**Area in allestimento**

_(Questa sezione verrà dettagliata quando l'implementazione di RabbitMQ per il consumo di eventi da altri microservizi sarà completata. Questo microservizio ascolterà eventi come `assignment.created`, `material.uploaded`, `exam.scheduled`, `feedback.provided` per generare notifiche appropriate.)_

---

## Sicurezza e Autorizzazioni

L'accesso alle API e le funzionalità del microservizio sono regolate da autorizzazioni basate sui ruoli degli utenti (es. `user`, `teach`, `admin`), gestiti dal microservizio **Gestione Utenti e Ruoli**.

- **Ruolo `teach` (Docente)**:
    - Può inviare messaggi agli studenti iscritti ai propri corsi (`user`).
    - Può ricevere messaggi dagli studenti dei propri corsi (`user`).
    - Può visualizzare le proprie notifiche.
    - Può marcare i propri messaggi e notifiche come letti.
- **Ruolo `user` (Studente)**:
    - Può inviare messaggi ai docenti (`teach`) dei corsi a cui è iscritto.
    - Può ricevere messaggi dai docenti (`teach`).
    - Può visualizzare le proprie notifiche.
    - Può marcare i propri messaggi e notifiche come letti.
- **Ruolo `admin` (Amministrativo)**:
    - Potrebbe avere accesso a funzionalità di monitoraggio o invio di notifiche di sistema (da definire in base a requisiti specifici, per ora non implementato con endpoint dedicati).
    - Riceve notifiche rilevanti per il ruolo amministrativo.

Tutte le richieste API devono essere autenticate. L'identità dell'utente (ID utente dal campo `sub` del token JWT) e il suo ruolo (dal campo `role` del token JWT) vengono estratti dall'header `Authorization: Bearer <JWT_TOKEN>` per autorizzare le operazioni e personalizzare le risposte (es. recuperare solo i messaggi/notifiche dell'utente autenticato).

---

## Accesso Swagger / OpenAPI
Dopo aver clonato il repository ed avviato l’app (mvn spring-boot:run), è possibile esplorare e testare le API via interfaccia Swagger:

- UI: http://localhost:8080/swagger-ui
- Documentazione JSON: http://localhost:8080/api-docs
- (Opzionale) Puoi anche caricare manualmente il file comunicazioni-notifiche-api-stub-v1.yaml su https://editor.swagger.io

Autenticazione su Swagger: cliccare "Authorize" e incollare il JWT (senza prefisso Bearer). Le richieste useranno automaticamente l'intestazione Authorization: Bearer <token>. Versione API: 1.1.2.

### Novità dalla 1.1.0 alla 1.1.2
- Auto-registrazione utente: al primo accesso con un nuovo JWT viene creato automaticamente un record `UserAccount` (ruolo default `student` se non presente) senza necessità di endpoint separati.
- Limitazione sicurezza mittente: il campo opzionale `senderId` nel body di creazione messaggio è ignorato in ambienti non `dev` — in produzione il mittente coincide sempre con il `sub` del token.
- Test minimal: aggiunti test puri JUnit sui model (`Messaggio`, `Notifica`, `UserAccount`) senza caricare il contesto Spring per mantenere semplicità.

> NOTA: Gli endpoint `dev` restano disponibili solo con profilo attivo e servono per simulazioni manuali (creazione utenti, broadcast eventi). In produzione non abilitarli.

Dev profile endpoints utili:
- POST /api/v1/dev/users {"userId":"alice","role":"student"}
- PATCH /api/v1/dev/users/{userId}/role {"role":"teacher"}
- POST /api/v1/dev/events/broadcast per simulare un evento piattaforma.

Broadcast notifiche: ogni evento Rabbit MQ (code materiale/compiti/esami) genera notifiche per tutti gli utenti tranne quelli con role=teacher. Modificare la logica in PlatformEventListener se necessario.

---

## Setup e Avvio con Docker

Per avviare il progetto in locale utilizzando Docker, seguire questi passaggi:

1.  **Clonare il repository**.
2.  **Configurare le variabili d'ambiente**:
    Il file `.env` contenente le password non è incluso nel repository per sicurezza.
    Creare un file `.env` nella root del progetto copiando il template fornito:
    ```bash
    cp .env.example .env
    ```
    (Opzionale) Modificare il file `.env` con le proprie password se necessario.
3.  **Avviare i container**:
    ```bash
    docker-compose up --build
    ```
