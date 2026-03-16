package com.hosteleria.websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hosteleria.chat.ChatController;
import com.hosteleria.model.MensajeChat;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Endpoint WebSocket para el chat privado entre empleados.
 *
 * URL de conexión: ws://host:8080/chat
 *
 * Protocolo de autenticación:
 *   1. El cliente abre la conexión WS.
 *   2. Envía inmediatamente un mensaje AUTH: { "tipo":"AUTH", "idEmisor":3, "token":"xxx" }
 *   3. El servidor valida el token contra AuthService.
 *   4. Si es válido → registra la sesión y devuelve el historial reciente.
 *   5. Si no es válido → envía ERROR y cierra la conexión.
 *
 * Flujo de mensaje normal:
 *   Cliente envía: { "tipo":"TEXT", "idEmisor":3, "idReceptor":7, "contenido":"Hola" }
 *   Servidor:
 *     → persiste en BBDD
 *     → reenvía al receptor si está conectado
 *     → devuelve ACK al emisor con el idMensaje asignado
 */
@ServerEndpoint(value = "/chat")
public class ChatEndpoint {

    // Mapa de sesiones activas: idEmpleado → Session WebSocket
    private static final Map<Integer, Session> sesionesActivas = new ConcurrentHashMap<>();

    // Mapa inverso: sessionId → idEmpleado (para onClose/onError)
    private static final Map<String, Integer> sessionToEmpleado = new ConcurrentHashMap<>();

    // Token asociado a cada sesión (para invalidarlo al desconectar)
    private static final Map<String, String> sessionToToken = new ConcurrentHashMap<>();

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create();

    private static final ChatController chatController = new ChatController();
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ── Ciclo de vida ─────────────────────────────────────────────────

    @OnOpen
    public void onOpen(Session session) {
        // La sesión queda en espera hasta recibir AUTH
        session.setMaxIdleTimeout(30_000); // 30 seg para autenticarse
        System.out.println("[WS] Nueva conexión pendiente de AUTH: " + session.getId());
    }

    @OnMessage
    public void onMessage(String payload, Session session) {
        ChatMessage msg;
        try {
            msg = gson.fromJson(payload, ChatMessage.class);
        } catch (Exception e) {
            enviar(session, ChatMessage.error("JSON inválido"));
            return;
        }

        if (msg.getTipo() == null) {
            enviar(session, ChatMessage.error("Campo 'tipo' obligatorio"));
            return;
        }

        switch (msg.getTipo()) {
            case AUTH    -> manejarAuth(session, msg);
            case TEXT    -> manejarTexto(session, msg);
            case READ    -> manejarRead(session, msg);
            default      -> enviar(session, ChatMessage.error("Tipo de mensaje no reconocido"));
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        limpiarSesion(session);
        System.out.println("[WS] Conexión cerrada: " + session.getId() + " — " + reason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("[WS] Error en sesión " + session.getId() + ": " + error.getMessage());
        limpiarSesion(session);
    }

    // ── Handlers de cada tipo de mensaje ─────────────────────────────

    private void manejarAuth(Session session, ChatMessage msg) {
        int idEmpleado = AuthService.validarToken(msg.getToken());

        if (idEmpleado == -1) {
            enviar(session, ChatMessage.error("Token inválido o expirado"));
            cerrarSesion(session, "Autenticación fallida");
            return;
        }

        // Si el empleado ya tenía otra sesión abierta, la cerramos
        Session sesionAnterior = sesionesActivas.get(idEmpleado);
        if (sesionAnterior != null && sesionAnterior.isOpen()) {
            cerrarSesion(sesionAnterior, "Nueva sesión iniciada desde otro dispositivo");
        }

        // Registrar la nueva sesión
        sesionesActivas.put(idEmpleado, session);
        sessionToEmpleado.put(session.getId(), idEmpleado);
        sessionToToken.put(session.getId(), msg.getToken());
        session.setMaxIdleTimeout(0); // Sin timeout tras autenticarse

        System.out.println("[WS] Empleado " + idEmpleado + " autenticado — sesión " + session.getId());

        // Enviar historial reciente con el interlocutor indicado (si viene idReceptor)
        if (msg.getIdReceptor() != null) {
            List<MensajeChat> historial = chatController.getHistorial(idEmpleado, msg.getIdReceptor(), 50);
            for (MensajeChat m : historial) {
                ChatMessage histórico = new ChatMessage();
                histórico.setTipo(ChatMessage.Tipo.HISTORY);
                histórico.setIdEmisor(m.getEmisor().getIdEmpleado());
                histórico.setIdReceptor(m.getReceptor().getIdEmpleado());
                histórico.setContenido(m.getContenido());
                histórico.setIdMensaje(m.getIdMensaje());
                histórico.setTimestamp(m.getFechaEnvio().format(ISO));
                enviar(session, histórico);
            }

            // Marcar como leídos los mensajes recibidos de ese interlocutor
            chatController.marcarComoLeidos(msg.getIdReceptor(), idEmpleado);
        }
    }

    private void manejarTexto(Session session, ChatMessage msg) {
        Integer idEmisor = sessionToEmpleado.get(session.getId());

        // Verificar autenticación
        if (idEmisor == null) {
            enviar(session, ChatMessage.error("No autenticado. Envía AUTH primero"));
            return;
        }

        // Validar campos obligatorios
        if (msg.getIdReceptor() == null || msg.getContenido() == null || msg.getContenido().isBlank()) {
            enviar(session, ChatMessage.error("idReceptor y contenido son obligatorios"));
            return;
        }

        // Persistir en BBDD
        long idMensaje = chatController.guardarMensaje(idEmisor, msg.getIdReceptor(), msg.getContenido());

        if (idMensaje == -1) {
            enviar(session, ChatMessage.error("Error al guardar el mensaje"));
            return;
        }

        // Completar el mensaje para retransmitir
        msg.setIdEmisor(idEmisor);
        msg.setIdMensaje(idMensaje);
        msg.setTimestamp(LocalDateTime.now().format(ISO));

        // ACK al emisor
        enviar(session, ChatMessage.ack(idMensaje));

        // Entregar al receptor si está conectado
        Session sesionReceptor = sesionesActivas.get(msg.getIdReceptor());
        if (sesionReceptor != null && sesionReceptor.isOpen()) {
            enviar(sesionReceptor, msg);
        }
        // Si no está conectado, el mensaje queda en BBDD con leido=false
        // y se enviará como HISTORY cuando abra la conversación
    }

    private void manejarRead(Session session, ChatMessage msg) {
        Integer idReceptor = sessionToEmpleado.get(session.getId());

        if (idReceptor == null) {
            enviar(session, ChatMessage.error("No autenticado"));
            return;
        }

        if (msg.getIdEmisor() == null) {
            enviar(session, ChatMessage.error("idEmisor requerido para marcar como leído"));
            return;
        }

        chatController.marcarComoLeidos(msg.getIdEmisor(), idReceptor);

        // Notificar al emisor que sus mensajes fueron leídos (doble check)
        Session sesionEmisor = sesionesActivas.get(msg.getIdEmisor());
        if (sesionEmisor != null && sesionEmisor.isOpen()) {
            ChatMessage notifLeido = new ChatMessage();
            notifLeido.setTipo(ChatMessage.Tipo.READ);
            notifLeido.setIdEmisor(msg.getIdEmisor());
            notifLeido.setIdReceptor(idReceptor);
            notifLeido.setTimestamp(LocalDateTime.now().format(ISO));
            enviar(sesionEmisor, notifLeido);
        }
    }

    // ── Utilidades ────────────────────────────────────────────────────

    private void enviar(Session session, ChatMessage msg) {
        if (session == null || !session.isOpen()) return;
        try {
            session.getBasicRemote().sendText(gson.toJson(msg));
        } catch (IOException e) {
            System.err.println("[WS] Error enviando mensaje a " + session.getId() + ": " + e.getMessage());
        }
    }

    private void limpiarSesion(Session session) {
        String sessionId   = session.getId();
        Integer idEmpleado = sessionToEmpleado.remove(sessionId);
        String token       = sessionToToken.remove(sessionId);

        if (idEmpleado != null) {
            sesionesActivas.remove(idEmpleado);
            System.out.println("[WS] Sesión limpiada para empleado " + idEmpleado);
        }
        if (token != null) {
            AuthService.invalidarToken(token);
        }
    }

    private void cerrarSesion(Session session, String motivo) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, motivo));
        } catch (IOException e) {
            System.err.println("[WS] Error cerrando sesión: " + e.getMessage());
        }
    }

    /** Número de empleados conectados en este momento (útil para monitorización) */
    public static int sesionesActivas() {
        return sesionesActivas.size();
    }
}
