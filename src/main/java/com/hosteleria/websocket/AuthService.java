package com.hosteleria.websocket;

import com.hosteleria.model.Empleado;
import com.hosteleria.util.HibernateUtil;
import org.hibernate.Session;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de autenticación ligero para el chat.
 *
 * Estrategia:
 *   - Al hacer login en la app, el servidor genera un token = SHA-256(idEmpleado + secreto + timestamp).
 *   - El token se entrega al móvil y se almacena en memoria (tokens activos).
 *   - El primer mensaje WebSocket de tipo AUTH lleva ese token.
 *   - Si el token es válido, la sesión WS queda asociada al idEmpleado.
 *
 * En producción sustituir por JWT (jjwt) o integrar con Spring Security.
 */
public class AuthService {

    // Secreto compartido — en producción leer de variable de entorno
    private static final String SECRET = System.getenv()
            .getOrDefault("CHAT_SECRET", "hosteleria_secret_2024");

    // Tokens activos: token → idEmpleado
    private static final Map<String, Integer> tokensActivos = new ConcurrentHashMap<>();

    /** Genera y registra un token para un empleado (llamar al hacer login HTTP) */
    public static String generarToken(int idEmpleado) {
        String raw   = idEmpleado + ":" + SECRET + ":" + System.currentTimeMillis();
        String token = sha256Base64(raw);
        tokensActivos.put(token, idEmpleado);
        return token;
    }

    /**
     * Valida el token recibido en el mensaje AUTH.
     * Devuelve el idEmpleado si es válido, -1 si no lo es.
     */
    public static int validarToken(String token) {
        if (token == null || token.isBlank()) return -1;
        Integer idEmpleado = tokensActivos.get(token);
        if (idEmpleado == null) return -1;

        // Comprobar que el empleado existe y está activo en BBDD
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Empleado emp = session.get(Empleado.class, idEmpleado);
            if (emp == null || emp.getEstado() != Empleado.EstadoEmpleado.activo) {
                tokensActivos.remove(token);
                return -1;
            }
        }
        return idEmpleado;
    }

    /** Invalida el token al cerrar sesión o desconectarse */
    public static void invalidarToken(String token) {
        if (token != null) tokensActivos.remove(token);
    }

    // ── Utilidad ──────────────────────────────────────────────────────

    private static String sha256Base64(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error generando token", e);
        }
    }
}
