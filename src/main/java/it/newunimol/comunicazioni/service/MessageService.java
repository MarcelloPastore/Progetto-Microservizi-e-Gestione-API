package it.newunimol.comunicazioni.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import it.newunimol.comunicazioni.dto.NuovoMessaggioRequestDTO;
import it.newunimol.comunicazioni.model.Messaggio;
import it.newunimol.comunicazioni.model.NotificationType;
import it.newunimol.comunicazioni.model.ReadStatus;
import it.newunimol.comunicazioni.repository.MessaggioRepository;

@Service
public class MessageService {

    private final MessaggioRepository messaggioRepository;
    private final NotificationService notificationService;

    public MessageService(MessaggioRepository messaggioRepository,
                          NotificationService notificationService) {
        this.messaggioRepository = messaggioRepository;
        this.notificationService = notificationService;
    }

    public Messaggio sendMessage(String senderId, NuovoMessaggioRequestDTO body) {
        Messaggio m = new Messaggio(senderId,
                body.receiverId(),
                body.courseContextId(),
                body.subject(),
                body.body());
        m.setReadStatus(ReadStatus.UNREAD);
        Messaggio saved = messaggioRepository.save(m);

        notificationService.createSimpleNotification(
                body.receiverId(),
                NotificationType.NEW_MESSAGE,
                "Nuovo messaggio",
                body.subject() != null ? body.subject() : "Hai ricevuto un nuovo messaggio",
                saved.getId(),
                "/messages/" + saved.getId()
        );
        return saved;
    }

    public Page<Messaggio> inbox(String userId, Pageable pageable) {
        return messaggioRepository.findByReceiverId(userId, pageable);
    }

    public Page<Messaggio> sent(String userId, Pageable pageable) {
        return messaggioRepository.findBySenderId(userId, pageable);
    }

    public Optional<Messaggio> findVisible(String userId, Long id) {
        return messaggioRepository.findById(id)
                .filter(m -> m.getSenderId().equals(userId) || m.getReceiverId().equals(userId));
    }

    public boolean markRead(String userId, Long id) {
        return messaggioRepository.findById(id)
                .map(m -> {
                    if (!m.getReceiverId().equals(userId)) return false;
                    if (m.getReadStatus() != ReadStatus.READ) {
                        m.setReadStatus(ReadStatus.READ);
                        messaggioRepository.save(m);
                    }
                    return true;
                }).orElse(false);
    }

    public Page<Messaggio> byCourse(String userId, Long courseId, Pageable pageable) {
        return messaggioRepository.findByCourseAndUser(courseId, userId, pageable);
    }
}
