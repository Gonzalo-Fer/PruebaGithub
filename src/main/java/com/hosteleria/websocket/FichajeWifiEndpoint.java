package com.hosteleria.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hosteleria.service.PresenciaService;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket endpoint para fichaje desde la app móvil vía WiFi corporativa.
 *
 * URL del servidor: ws://[ip-servidor]:8080/fichaje
 *
 * ──────────────────────────────────────────────────────────────────────
 * PROTOCOLO JSON
 * ──────────────────────────────────────────────────────────────────────
 *
 * → Autenticación (primer mensaje obligatorio):
 *   { "tipo": "AUTH", "idEmpleado": 5, "token": "sha256hex..." }
 *   ← { "ok": true, "tipo": "AUTH", "mensaje": "Autenticado." }
 *
 * → Fichaje de entrada:
 *   { "tipo": "ENTRADA", "hora": "08:32" }   // hora opcional, usa now() si falta
 *   ← { "ok": true, "tipo": "ENTRADA", "mensaje": "Entrada registrada.", "retraso": false }
 *
 * → Fichaje de salida:
 *   { "tipo": "SALIDA", "hora": "17:05" }
 *   ← { "ok": true, "tipo": "SALIDA", "mensaje": "Salida registrada. Trabajadas: 8.55h" }
 *
 * → En caso de error:
 *   ← { "ok": false, "mensaje": "Descripción del error" }
 *
 * ──────────────────────────────────────────────────────────────────────
 * FLUJO DE LA APP MÓVIL
 * ──────────────────────────────────────────────────────────────────────
 * 1. App detecta SSID corporativo (BroadcastReceiver de WiFi)
 * 2. Abre conexión WebSocket a ws://servidor:8080/fichaje
 * 3. Envía AUTH con token del empleado
 * 4. Si es entrada al trabajo → ENTRADA
 *    Si es salida del trabajo → SALIDA
 * 5. Muestra confirmación al empleado
 * 6. Cierra la conexión
 */
@ServerEndpoint("/fichaje")
public class FichajeWifiEndpoint {

    private static final Gson GSON = new Gson();

    // sesionId → idEmpleado (ya autenticado)
    private static final ConcurrentHashMap<String, Integer> SESIONES_AUTH =
            new ConcurrentHashMap<>();

    private static final PresenciaService presencia = new PresenciaService();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("[FichajeWifi] Conexión abierta: " + session.getId());
    }

    @OnMessage
    public void onMessage(String mensaje, Session session) {
        try {
            JsonObject req = GSON.fromJson(mensaje, JsonObject.class);
            String tipo = req.has("tipo") ? req.get("tipo").getAsString().toUpperCase() : "";

            switch (tipo) {
                case "AUTH" -> manejarAuth(req, session);
                case "ENTRADA" -> manejarEntrada(req, session);
                case "SALIDA"  -> manejarSalida(req, session);
                default -> responder(session, false, tipo, "Tipo de mensaje desconocido: " + tipo, false);
            }
        } catch (Exception e) {
            System.err.println("[FichajeWifi] Error procesando mensaje: " + e.getMessage());
            responder(session, false, "ERROR", "Error interno del servidor.", false);
        }
    }

    @OnClose
    public void onClose(Session session) {
        SESIONES_AUTH.remove(session.getId());
        System.out.println("[FichajeWifi] Conexión cerrada: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable t) {
        System.err.println("[FichajeWifi] Error en sesión " + session.getId() + ": " + t.getMessage());
    }

    // ── Handlers ─────────────────────────────────────────────────────

    private void manejarAuth(JsonObject req, Session session) {
        if (!req.has("idEmpleado") || !req.has("token")) {
            responder(session, false, "AUTH", "Faltan campos idEmpleado o token.", false);
            return;
        }
        String token     = req.get("token").getAsString();
        int idEmpleado   = AuthService.validarToken(token);

        if (idEmpleado == -1) {
            responder(session, false, "AUTH", "Token inválido o expirado.", false);
            return;
        }
        SESIONES_AUTH.put(session.getId(), idEmpleado);
        responder(session, true, "AUTH", "Autenticado correctamente.", false);
    }

    private void manejarEntrada(JsonObject req, Session session) {
        Integer idEmpleado = SESIONES_AUTH.get(session.getId());
        if (idEmpleado == null) {
            responder(session, false, "ENTRADA", "No autenticado. Envía AUTH primero.", false);
            return;
        }
        LocalTime hora = parsearHora(req);
        PresenciaService.ResultadoFichaje r = presencia.registrarEntrada(idEmpleado, hora);
        responder(session, r.ok, "ENTRADA", r.mensaje, r.hayRetraso);
    }

    private void manejarSalida(JsonObject req, Session session) {
        Integer idEmpleado = SESIONES_AUTH.get(session.getId());
        if (idEmpleado == null) {
            responder(session, false, "SALIDA", "No autenticado. Envía AUTH primero.", false);
            return;
        }
        LocalTime hora = parsearHora(req);
        PresenciaService.ResultadoFichaje r = presencia.registrarSalida(idEmpleado, hora);
        responder(session, r.ok, "SALIDA", r.mensaje, false);
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private LocalTime parsearHora(JsonObject req) {
        if (req.has("hora") && !req.get("hora").isJsonNull()) {
            try { return LocalTime.parse(req.get("hora").getAsString()); }
            catch (DateTimeParseException ignored) { /* usa now() */ }
        }
        return LocalTime.now();
    }

    private static void responder(Session session, boolean ok, String tipo,
                                  String mensaje, boolean retraso) {
        try {
            JsonObject resp = new JsonObject();
            resp.addProperty("ok",      ok);
            resp.addProperty("tipo",    tipo);
            resp.addProperty("mensaje", mensaje);
            resp.addProperty("retraso", retraso);
            session.getBasicRemote().sendText(GSON.toJson(resp));
        } catch (IOException e) {
            System.err.println("[FichajeWifi] No se pudo enviar respuesta: " + e.getMessage());
        }
    }
}