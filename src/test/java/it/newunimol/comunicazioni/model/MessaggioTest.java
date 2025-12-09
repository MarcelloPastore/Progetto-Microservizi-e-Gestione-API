package it.newunimol.comunicazioni.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessaggioTest {

    @Test
    void testMessaggioCreation() {
        String senderId = "sender123";
        String receiverId = "receiver456";
        Long courseContextId = 101L;
        String subject = "Test Subject";
        String body = "Test Body";

        Messaggio messaggio = new Messaggio(senderId, receiverId, courseContextId, subject, body);

        assertNotNull(messaggio);
        assertEquals(senderId, messaggio.getSenderId());
        assertEquals(receiverId, messaggio.getReceiverId());
        assertEquals(courseContextId, messaggio.getCourseContextId());
        assertEquals(subject, messaggio.getSubject());
        assertEquals(body, messaggio.getBody());
    }
    
    @Test
    void testPrePersist() {
        Messaggio messaggio = new Messaggio();
        messaggio.onCreate();
        
        assertNotNull(messaggio.getTimestamp());
        assertEquals(ReadStatus.UNREAD, messaggio.getReadStatus());
    }
}
