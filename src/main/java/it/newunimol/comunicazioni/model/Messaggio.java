package it.newunimol.comunicazioni.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messaggi")
public class Messaggio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="sender_id", nullable = false, length = 255)
    private String senderId;

    @Column(name="receiver_id", nullable = false, length = 255)
    private String receiverId;

    @Column(name="course_context_id")
    private Long courseContextId;

    @Column(name="subject", nullable = false, length = 255)
    private String subject;

    @Column(name="body", nullable = false, columnDefinition = "TEXT")
    private String body;

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

    // Costruttori
    public Messaggio() {}

    public Messaggio(String senderId, String receiverId, Long courseContextId, String subject, String body) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.courseContextId = courseContextId;
        this.subject = subject;
        this.body = body;
    }

    // Getter & Setter
    public Long getId() { return id; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public Long getCourseContextId() { return courseContextId; }
    public void setCourseContextId(Long courseContextId) { this.courseContextId = courseContextId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public ReadStatus getReadStatus() { return readStatus; }
    public void setReadStatus(ReadStatus readStatus) { this.readStatus = readStatus; }
}
