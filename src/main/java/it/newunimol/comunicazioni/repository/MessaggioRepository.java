package it.newunimol.comunicazioni.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import it.newunimol.comunicazioni.model.Messaggio;

public interface MessaggioRepository extends JpaRepository<Messaggio, Long> {
    Page<Messaggio> findByReceiverId(String receiverId, Pageable pageable);
    Page<Messaggio> findBySenderId(String senderId, Pageable pageable);

    @Query("SELECT m FROM Messaggio m WHERE m.courseContextId = :courseId AND (m.senderId = :userId OR m.receiverId = :userId)")
    Page<Messaggio> findByCourseAndUser(Long courseId, String userId, Pageable pageable);
}
