package com.legacyvault.legacyvault.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecretKeySpec secretKey;

    public EncryptionService() {

        String encodedKey = System.getenv("LEGACYVAULT_MASTER_KEY");

        if (encodedKey == null || encodedKey.isBlank()) {
            throw new IllegalStateException(
                    "LEGACYVAULT_MASTER_KEY environment variable is missing"
            );
        }

        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);

        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "LEGACYVAULT_MASTER_KEY must be a 256-bit key"
            );
        }

        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {

        try {

            byte[] iv = new byte[IV_LENGTH];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(TAG_LENGTH, iv);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    parameterSpec
            );

            byte[] encrypted =
                    cipher.doFinal(
                            plaintext.getBytes(StandardCharsets.UTF_8)
                    );

            byte[] combined =
                    new byte[iv.length + encrypted.length];

            System.arraycopy(
                    iv,
                    0,
                    combined,
                    0,
                    iv.length
            );

            System.arraycopy(
                    encrypted,
                    0,
                    combined,
                    iv.length,
                    encrypted.length
            );

            return Base64.getEncoder()
                    .encodeToString(combined);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Encryption failed",
                    e
            );
        }
    }

    public String decrypt(String encryptedData) {

        try {

            byte[] combined =
                    Base64.getDecoder()
                            .decode(encryptedData);

            byte[] iv = new byte[IV_LENGTH];

            byte[] encrypted =
                    new byte[combined.length - IV_LENGTH];

            System.arraycopy(
                    combined,
                    0,
                    iv,
                    0,
                    IV_LENGTH
            );

            System.arraycopy(
                    combined,
                    IV_LENGTH,
                    encrypted,
                    0,
                    encrypted.length
            );

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(TAG_LENGTH, iv);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    parameterSpec
            );

            byte[] decrypted =
                    cipher.doFinal(encrypted);

            return new String(
                    decrypted,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Decryption failed",
                    e
            );
        }
    }
}