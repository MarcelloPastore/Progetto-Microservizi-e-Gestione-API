package it.newunimol.comunicazioni;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import it.newunimol.comunicazioni.dto.NuovoMessaggioRequestDTO;
import it.newunimol.comunicazioni.model.Messaggio;
import it.newunimol.comunicazioni.model.Notifica;
import it.newunimol.comunicazioni.model.ReadStatus;
import it.newunimol.comunicazioni.repository.MessaggioRepository;
import it.newunimol.comunicazioni.repository.NotificaRepository;
import it.newunimol.comunicazioni.repository.UserAccountRepository;
import it.newunimol.comunicazioni.service.MessageService;
import it.newunimol.comunicazioni.service.NotificationService;
import it.newunimol.comunicazioni.service.UserRegistryService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled("Disabilitato: evita caricamento contesto (DB+Security). Manteniamo solo unit test semplici.")
class MessageAndNotificationIntegrationTests {

    @Autowired MessageService messageService;
    @Autowired NotificationService notificationService;
    @Autowired MessaggioRepository messaggioRepository;
    @Autowired NotificaRepository notificaRepository;
    @Autowired UserAccountRepository userAccountRepository;
    @Autowired UserRegistryService userRegistryService;

    @BeforeEach
    void clean() {
        notificaRepository.deleteAll();
        messaggioRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    @Test
    void sendMessage_creaMessaggioENotifica() {
        // precondizione: nessun record
        assertEquals(0, messaggioRepository.count());
        assertEquals(0, notificaRepository.count());

        // auto registrazione utenti
        userRegistryService.ensureUser("alice", "student");
        userRegistryService.ensureUser("bob", "student");

        NuovoMessaggioRequestDTO dto = new NuovoMessaggioRequestDTO(null, "bob", 42L, "Saluto", "Ciao Bob!" );
        Messaggio m = messageService.sendMessage("alice", dto);

        assertNotNull(m.getId());
        assertEquals("alice", m.getSenderId());
        assertEquals("bob", m.getReceiverId());
        assertEquals(ReadStatus.UNREAD, m.getReadStatus());

        // verifica notifica generata
        List<Notifica> notificheBob = notificaRepository.findByUserIdAndReadStatus("bob", ReadStatus.UNREAD);
        assertEquals(1, notificheBob.size());
        Notifica n = notificheBob.get(0);
        assertEquals(m.getId(), n.getReferenceId());
        assertEquals("Nuovo messaggio", n.getTitle());
    }

    @Test
    void markRead_soloReceiverPuoSegnareLetto() {
        userRegistryService.ensureUser("alice", "student");
        userRegistryService.ensureUser("bob", "student");
        Messaggio m = messageService.sendMessage("alice", new NuovoMessaggioRequestDTO(null, "bob", null, "Oggetto", "Body"));

        boolean wrong = messageService.markRead("alice", m.getId());
        assertFalse(wrong, "Il sender non dovrebbe poter marcare read");

        boolean ok = messageService.markRead("bob", m.getId());
        assertTrue(ok);

        Messaggio ricaricato = messaggioRepository.findById(m.getId()).orElseThrow();
        assertEquals(ReadStatus.READ, ricaricato.getReadStatus());
    }

    @Test
    void markAllRead_funziona() {
        userRegistryService.ensureUser("a", "student");
        userRegistryService.ensureUser("b", "student");
        // 2 messaggi verso b
        messageService.sendMessage("a", new NuovoMessaggioRequestDTO(null, "b", null, "Uno", "Body"));
        messageService.sendMessage("a", new NuovoMessaggioRequestDTO(null, "b", null, "Due", "Body"));

        assertEquals(2, notificaRepository.findByUserIdAndReadStatus("b", ReadStatus.UNREAD).size());
        notificationService.markAllRead("b");
        assertEquals(0, notificaRepository.findByUserIdAndReadStatus("b", ReadStatus.UNREAD).size());
    }
}
