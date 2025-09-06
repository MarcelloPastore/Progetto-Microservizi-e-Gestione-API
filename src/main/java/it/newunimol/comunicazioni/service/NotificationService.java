package it.newunimol.comunicazioni.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import it.newunimol.comunicazioni.model.Notifica;
import it.newunimol.comunicazioni.model.NotificationType;
import it.newunimol.comunicazioni.model.ReadStatus;
import it.newunimol.comunicazioni.repository.NotificaRepository;

@Service
public class NotificationService {

    private final NotificaRepository notificaRepository;

    public NotificationService(NotificaRepository notificaRepository) {
        this.notificaRepository = notificaRepository;
    }

    public Notifica createSimpleNotification(String userId, NotificationType type,
                                             String title, String message,
                                             Long referenceId, String link) {
        Notifica n = new Notifica(userId, type, title, message, referenceId, link);
        return notificaRepository.save(n);
    }

    public List<Notifica> createBulkNotifications(List<String> userIds, NotificationType type,
                                                  String title, String message,
                                                  Long referenceId, String link) {
        List<Notifica> list = new ArrayList<>();
        for (String uid : userIds) {
            list.add(new Notifica(uid, type, title, message, referenceId, link));
        }
    List<Notifica> saved = notificaRepository.saveAll(list);
    System.out.println("[NOTIFICHE] Salvate " + saved.size() + " notifiche (type=" + type + ")");
    return saved;
    }

    public Page<Notifica> getNotifications(String userId, ReadStatus rs, Pageable pageable) {
        return rs == null
                ? notificaRepository.findByUserId(userId, pageable)
                : notificaRepository.findByUserIdAndReadStatus(userId, rs, pageable);
    }

    public long countUnread(String userId) {
        return notificaRepository.countByUserIdAndReadStatus(userId, ReadStatus.UNREAD);
    }

    public boolean markRead(String userId, Long id) {
        return notificaRepository.findById(id)
                .map(n -> {
                    if (!n.getUserId().equals(userId)) return false;
                    if (n.getReadStatus() != ReadStatus.READ) {
                        n.setReadStatus(ReadStatus.READ);
                        notificaRepository.save(n);
                    }
                    return true;
                }).orElse(false);
    }

    public void markAllRead(String userId) {
        List<Notifica> unread = notificaRepository.findByUserIdAndReadStatus(userId, ReadStatus.UNREAD);
        if (!unread.isEmpty()) {
            unread.forEach(n -> n.setReadStatus(ReadStatus.READ));
            notificaRepository.saveAll(unread);
        }
    }
}
