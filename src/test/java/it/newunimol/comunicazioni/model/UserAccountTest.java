package it.newunimol.comunicazioni.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class UserAccountTest {

    @Test
    void costruttoreEGetter() {
        UserAccount ua = new UserAccount("stud1", "student");
        assertEquals("stud1", ua.getUserId());
        assertEquals("student", ua.getRole());
    }

    @Test
    void setRoleAggiornaValore() {
        UserAccount ua = new UserAccount("teach1", "student");
        ua.setRole("teacher");
        assertEquals("teacher", ua.getRole());
    }
}
