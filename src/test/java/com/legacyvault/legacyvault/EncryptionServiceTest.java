package com.legacyvault.legacyvault;

import com.legacyvault.legacyvault.service.EncryptionService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    // TEST 1
    // Password should encrypt and decrypt correctly
    @Test
    void shouldEncryptAndDecryptPassword() {

        EncryptionService encryptionService = new EncryptionService();

        String originalPassword = "MySecretPassword123!";

        String encrypted =
                encryptionService.encrypt(originalPassword);

        String decrypted =
                encryptionService.decrypt(encrypted);

        assertNotEquals(originalPassword, encrypted);

        assertEquals(originalPassword, decrypted);
    }


    // TEST 2
    // Same password should produce different ciphertext
    // because AES-GCM uses a random IV
    @Test
    void shouldGenerateDifferentCiphertextForSamePassword() {

        EncryptionService encryptionService = new EncryptionService();

        String password = "SamePassword123!";

        String encrypted1 =
                encryptionService.encrypt(password);

        String encrypted2 =
                encryptionService.encrypt(password);

        assertNotEquals(encrypted1, encrypted2);

        assertEquals(
                password,
                encryptionService.decrypt(encrypted1)
        );

        assertEquals(
                password,
                encryptionService.decrypt(encrypted2)
        );
    }


    // TEST 3
    // Modified encrypted data must NOT decrypt successfully
    @Test
    void shouldRejectTamperedCiphertext() {

        EncryptionService encryptionService = new EncryptionService();

        String password = "SensitivePassword123!";

        String encrypted =
                encryptionService.encrypt(password);

        // Corrupt one character of the encrypted data
        char replacement =
                encrypted.charAt(encrypted.length() - 1) == 'A'
                        ? 'B'
                        : 'A';

        String tampered =
                encrypted.substring(0, encrypted.length() - 1)
                        + replacement;

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> encryptionService.decrypt(tampered)
                );

        assertEquals(
                "Decryption failed",
                exception.getMessage()
        );
    }
}