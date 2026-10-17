package com.fooddelivery;

import com.fooddelivery.service.JwtService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Lightweight test that does not need MySQL
class FoodDeliveryBackendApplicationTests {

    private static final String SECRET =
            "zDKWiTInqb47nDfFtrfNtuzBS1fExyZ7FfTqLPsRejc=";

    @Test
    void tokenContainsEmailAndRole() {
        JwtService jwt = new JwtService(SECRET, 60_000);
        String token = jwt.generateToken("a@b.com", "ADMIN");

        assertEquals("a@b.com", jwt.extractEmail(token));
        assertEquals("ADMIN", jwt.extractRole(token));
        assertTrue(jwt.isTokenValid(token, "a@b.com"));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, -1000);
        String token = jwt.generateToken("a@b.com", "CUSTOMER");

        assertFalse(jwt.isTokenValid(token, "a@b.com"));
    }
}
