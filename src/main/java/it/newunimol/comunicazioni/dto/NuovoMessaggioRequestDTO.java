package it.newunimol.comunicazioni.dto;

public class NuovoMessaggioRequestDTO {
    private String receiverId;
    private Long courseContextId;
    private String subject;
    private String body;

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public Long getCourseContextId() { return courseContextId; }
    public void setCourseContextId(Long courseContextId) { this.courseContextId = courseContextId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
