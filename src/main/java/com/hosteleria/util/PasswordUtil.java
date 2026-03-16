package com.hosteleria.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Utilidad para hashear y verificar contraseñas con PBKDF2-HMAC-SHA256.
 * Seguro para almacenar password_hash en la base de datos.
 */
public final class PasswordUtil {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 65536;
    private static final int LONGITUD_CLAVE = 256;
    private static final int LONGITUD_SALT = 16;

    private PasswordUtil() {}

    /**
     * Genera un hash seguro de la contraseña (incluye salt).
     * Formato: base64(salt) + ":" + base64(hash).
     */
    public static String hash(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía");
        }
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[LONGITUD_SALT];
        random.nextBytes(salt);

        byte[] hash = pbkdf2(password.toCharArray(), salt);
        String saltB64 = Base64.getEncoder().encodeToString(salt);
        String hashB64 = Base64.getEncoder().encodeToString(hash);
        return saltB64 + ":" + hashB64;
    }

    /**
     * Verifica si la contraseña en texto plano coincide con el hash almacenado.
     */
    public static boolean verificar(String password, String storedHash) {
        if (password == null || storedHash == null || !storedHash.contains(":")) {
            return false;
        }
        String[] partes = storedHash.split(":", 2);
        byte[] salt = Base64.getDecoder().decode(partes[0]);
        byte[] hashAlmacenado = Base64.getDecoder().decode(partes[1]);

        byte[] hashCalculado = pbkdf2(password.toCharArray(), salt);
        return java.security.MessageDigest.isEqual(hashAlmacenado, hashCalculado);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERACIONES, LONGITUD_CLAVE);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITMO);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error al hashear contraseña", e);
        }
    }
}
