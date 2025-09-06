package it.newunimol.comunicazioni.model;

import jakarta.persistence.*;

@Entity
@Table(name = "course_subscriptions", indexes = {
        @Index(name = "idx_course_subscriptions_course", columnList = "course_id"),
        @Index(name = "idx_course_subscriptions_user", columnList = "user_id")
})
public class CourseSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    public CourseSubscription() {}
    public CourseSubscription(Long courseId, String userId) {
        this.courseId = courseId;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
