package it.newunimol.comunicazioni.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class NotificaPrePersistTest {

    @Test
    void costruttoreImpostaCampi() {
        Notifica n = new Notifica("user", NotificationType.NEW_MESSAGE, "Titolo","Messaggio", 5L, "/link");
        assertEquals("user", n.getUserId());
        assertEquals(NotificationType.NEW_MESSAGE, n.getType());
        assertEquals("Titolo", n.getTitle());
        assertEquals("Messaggio", n.getMessage());
        assertEquals(5L, n.getReferenceId());
        assertEquals("/link", n.getLink());
        assertNull(n.getTimestamp());
    }

    @Test
    void prePersistImpostaTimestampEReadStatusSeNull() {
        Notifica n = new Notifica("u", NotificationType.NEW_MESSAGE, "T","M", null, null);
        n.onCreate();
        assertNotNull(n.getTimestamp());
        assertEquals(ReadStatus.UNREAD, n.getReadStatus());
    }
}
