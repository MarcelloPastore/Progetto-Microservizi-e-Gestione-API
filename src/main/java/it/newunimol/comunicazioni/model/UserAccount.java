package it.newunimol.comunicazioni.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserAccount {

    @Id
    @Column(name = "user_id", length = 255)
    private String userId; // usiamo direttamente l'id esterno come PK

    @Column(name = "role", length = 64, nullable = false)
    private String role; // es: student, teacher, admin

    protected UserAccount() {}
    public UserAccount(String userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
