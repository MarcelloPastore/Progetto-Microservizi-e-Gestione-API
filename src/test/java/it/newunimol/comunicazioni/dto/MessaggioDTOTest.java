package it.newunimol.comunicazioni.dto;

import it.newunimol.comunicazioni.model.ReadStatus;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class MessaggioDTOTest {

    @Test
    void testMessaggioDTOCreation() {
        Long id = 1L;
        String senderId = "sender123";
        String receiverId = "receiver456";
        Long courseContextId = 101L;
        String subject = "Test Subject";
        String body = "Test Body";
        LocalDateTime timestamp = LocalDateTime.now();
        ReadStatus readStatus = ReadStatus.UNREAD;

        MessaggioDTO dto = new MessaggioDTO(id, senderId, receiverId, courseContextId, subject, body, timestamp, readStatus);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals(senderId, dto.senderId());
        assertEquals(receiverId, dto.receiverId());
        assertEquals(courseContextId, dto.courseContextId());
        assertEquals(subject, dto.subject());
        assertEquals(body, dto.body());
        assertEquals(timestamp, dto.timestamp());
        assertEquals(readStatus, dto.readStatus());
    }
}
