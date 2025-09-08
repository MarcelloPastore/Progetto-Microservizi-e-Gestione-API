package it.newunimol.comunicazioni.model;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class MessaggioPrePersistTest {

    @Test
    void costruttoreImpostaCampiBase() {
        Messaggio m = new Messaggio("sender","receiver", 10L, "Oggetto","Corpo");
        assertEquals("sender", m.getSenderId());
        assertEquals("receiver", m.getReceiverId());
        assertEquals(10L, m.getCourseContextId());
        assertEquals("Oggetto", m.getSubject());
        assertEquals("Corpo", m.getBody());
        assertNull(m.getTimestamp(), "Timestamp deve essere null prima del persist");
    }

    @Test
    void prePersistAssegnaTimestampEReadStatus() {
        Messaggio m = new Messaggio("s","r", null, "Subj","Body");
        m.onCreate(); // chiamiamo direttamente il metodo lifecycle
        assertNotNull(m.getTimestamp());
        assertEquals(ReadStatus.UNREAD, m.getReadStatus());
        LocalDateTime t1 = m.getTimestamp();
        m.onCreate(); // richiamo non deve cambiare lo stato
        assertEquals(t1, m.getTimestamp());
    }
}
