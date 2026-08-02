package com.visibleai.brasstacks.ai;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Encrypts and decrypts AI API keys using AES-256-GCM.
 * The encryption key is derived from the user's password via PBKDF2.
 */
public class ApiKeyEncryptor {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 256;
    private static final int PBKDF2_ITERATIONS = 100_000;
    private static final int SALT_LENGTH = 16;
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "enc:";

    /**
     * Derives a Base64-encoded encryption key from the user's plaintext password.
     * Uses PBKDF2 with a stable salt derived from the user's email so the same
     * password always produces the same derived key for the same user.
     */
    public static String deriveKey(String password, String email) {
        try {
            // Use email as a stable salt so the derived key is deterministic per user
            byte[] salt = stableSalt(email);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(keyBytes);
        } catch (Exception e) {
            throw new RuntimeException("Key derivation failed", e);
        }
    }

    /**
     * Encrypts a plaintext API key using the derived key.
     * Output format: "enc:" + Base64(IV + ciphertext + GCM tag)
     */
    public static String encrypt(String plaintext, String derivedKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(derivedKeyBase64);
            SecretKey key = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

            byte[] combined = ByteBuffer.allocate(IV_LENGTH + ciphertext.length)
                    .put(iv)
                    .put(ciphertext)
                    .array();

            return PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts an encrypted API key using the derived key.
     */
    public static String decrypt(String encrypted, String derivedKeyBase64) {
        try {
            if (!encrypted.startsWith(PREFIX)) {
                throw new IllegalArgumentException("Not an encrypted value");
            }

            byte[] combined = Base64.getDecoder().decode(encrypted.substring(PREFIX.length()));
            byte[] keyBytes = Base64.getDecoder().decode(derivedKeyBase64);
            SecretKey key = new SecretKeySpec(keyBytes, "AES");

            ByteBuffer buffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);

            return new String(plaintext);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Returns true if the value is encrypted (has the "enc:" prefix).
     */
    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    /**
     * Produces a stable 16-byte salt from the user's email using SHA-256.
     */
    private static byte[] stableSalt(String email) {
        try {
            byte[] hash = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(email.toLowerCase().getBytes());
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(hash, 0, salt, 0, SALT_LENGTH);
            return salt;
        } catch (Exception e) {
            throw new RuntimeException("Salt generation failed", e);
        }
    }
}
