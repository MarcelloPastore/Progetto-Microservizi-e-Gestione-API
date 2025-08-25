package it.newunimol.comunicazioni.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import it.newunimol.comunicazioni.model.Notifica;
import it.newunimol.comunicazioni.model.ReadStatus;

public interface NotificaRepository extends JpaRepository<Notifica, Long> {
    Page<Notifica> findByUserId(String userId, Pageable pageable);
    Page<Notifica> findByUserIdAndReadStatus(String userId, ReadStatus readStatus, Pageable pageable);
    long countByUserIdAndReadStatus(String userId, ReadStatus readStatus);
    List<Notifica> findByUserIdAndReadStatus(String userId, ReadStatus readStatus);
}
