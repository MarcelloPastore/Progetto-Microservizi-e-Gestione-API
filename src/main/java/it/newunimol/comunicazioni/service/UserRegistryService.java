package it.newunimol.comunicazioni.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.newunimol.comunicazioni.model.UserAccount;
import it.newunimol.comunicazioni.repository.UserAccountRepository;

@Service
public class UserRegistryService {

    private final UserAccountRepository userAccountRepository;

    @Autowired
    public UserRegistryService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public void ensureUser(String userId, String role) {
        if (userId == null || userId.isBlank()) return;
        if (!userAccountRepository.existsById(userId)) {
            // fallback role se nullo
            String effectiveRole = (role == null || role.isBlank()) ? "student" : role;
            userAccountRepository.save(new UserAccount(userId, effectiveRole));
        }
    }
}