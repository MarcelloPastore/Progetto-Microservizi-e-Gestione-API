package it.newunimol.comunicazioni.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.newunimol.comunicazioni.model.CourseSubscription;

public interface CourseSubscriptionRepository extends JpaRepository<CourseSubscription, Long> {
    List<CourseSubscription> findByCourseId(Long courseId);
}
