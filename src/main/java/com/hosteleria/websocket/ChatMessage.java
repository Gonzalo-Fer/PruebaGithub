package com.hosteleria.websocket;

/**
 * DTO que se serializa/deserializa como JSON en el WebSocket.
 *
 * Tipos de mensaje:
 *   AUTH    → primer mensaje que envía el cliente para autenticarse
 *   TEXT    → mensaje de chat normal
 *   ACK     → confirmación del servidor (mensaje recibido/guardado)
 *   READ    → el receptor marcó los mensajes como leídos
 *   ERROR   → el servidor informa de un error
 *   HISTORY → el servidor devuelve el historial al conectarse
 */
public class ChatMessage {

    public enum Tipo { AUTH, TEXT, ACK, READ, ERROR, HISTORY }

    private Tipo    tipo;
    private Integer idEmisor;
    private Integer idReceptor;
    private String  token;        // usado solo en AUTH
    private String  contenido;
    private Long    idMensaje;    // usado en ACK y READ
    private String  timestamp;    // ISO-8601, lo pone el servidor

    // ── Constructores de fábrica (evitan new + setters en el código) ──

    public static ChatMessage auth(int idEmpleado, String token) {
        ChatMessage m = new ChatMessage();
        m.tipo      = Tipo.AUTH;
        m.idEmisor  = idEmpleado;
        m.token     = token;
        return m;
    }

    public static ChatMessage text(int idEmisor, int idReceptor, String contenido) {
        ChatMessage m = new ChatMessage();
        m.tipo       = Tipo.TEXT;
        m.idEmisor   = idEmisor;
        m.idReceptor = idReceptor;
        m.contenido  = contenido;
        return m;
    }

    public static ChatMessage ack(long idMensaje) {
        ChatMessage m = new ChatMessage();
        m.tipo      = Tipo.ACK;
        m.idMensaje = idMensaje;
        return m;
    }

    public static ChatMessage error(String motivo) {
        ChatMessage m = new ChatMessage();
        m.tipo      = Tipo.ERROR;
        m.contenido = motivo;
        return m;
    }

    // ── Getters y setters ─────────────────────────────────────────────

    public Tipo    getTipo()       { return tipo; }
    public void    setTipo(Tipo t) { this.tipo = t; }

    public Integer getIdEmisor()             { return idEmisor; }
    public void    setIdEmisor(Integer id)   { this.idEmisor = id; }

    public Integer getIdReceptor()           { return idReceptor; }
    public void    setIdReceptor(Integer id) { this.idReceptor = id; }

    public String  getToken()                { return token; }
    public void    setToken(String token)    { this.token = token; }

    public String  getContenido()            { return contenido; }
    public void    setContenido(String c)    { this.contenido = c; }

    public Long    getIdMensaje()            { return idMensaje; }
    public void    setIdMensaje(Long id)     { this.idMensaje = id; }

    public String  getTimestamp()            { return timestamp; }
    public void    setTimestamp(String ts)   { this.timestamp = ts; }
}
