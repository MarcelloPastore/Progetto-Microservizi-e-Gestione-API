package it.newunimol.comunicazioni.dto;

import java.time.LocalDateTime;

import it.newunimol.comunicazioni.model.ReadStatus;

public class MessaggioDTO {
    private Long id;
    private String senderId;
    private String receiverId;
    private Long courseContextId;
    private String subject;
    private String body;
    private LocalDateTime timestamp;
    private ReadStatus readStatus;

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
