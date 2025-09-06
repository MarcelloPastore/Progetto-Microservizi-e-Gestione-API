package it.newunimol.comunicazioni.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.newunimol.comunicazioni.model.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    List<UserAccount> findByRoleNotIgnoreCase(String role);
}
