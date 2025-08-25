package it.newunimol.comunicazioni.model; 

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifiche")
public class Notifica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false, length = 255)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name="type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name="title", nullable = false, length = 255)
    private String title;

    @Column(name="message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name="reference_id")
    private Long referenceId;

    @Column(name="link", length = 255)
    private String link;

    @Column(name="timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name="read_status", nullable = false, length = 20)
    private ReadStatus readStatus = ReadStatus.UNREAD;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) timestamp = LocalDateTime.now();
        if (readStatus == null) readStatus = ReadStatus.UNREAD;
    }

    public Notifica() {}

    public Notifica(String userId, NotificationType type, String title, String message, Long referenceId, String link) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.link = link;
    }

    // Getter & Setter
    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public ReadStatus getReadStatus() { return readStatus; }
    public void setReadStatus(ReadStatus readStatus) { this.readStatus = readStatus; }
}
