package com.hosteleria.util;

import com.hosteleria.websocket.ChatEndpoint;
import com.hosteleria.websocket.FichajeWifiEndpoint;
import org.glassfish.tyrus.server.Server;

import java.util.HashMap;
import java.util.Map;

/**
 * Servidor WebSocket (EN DESARROLLO).
 * Endpoints registrados:
 *   /chat    → ChatEndpoint           — chat en tiempo real entre empleados
 *   /fichaje → FichajeWifiEndpoint    — fichaje desde app móvil vía WiFi corporativa (CAMBIAR POR PETICIONES HTTP)
 */
public class WebSocketServer {

    private static final String HOST    = "0.0.0.0";
    private static final int    PORT    = 8080;
    private static final String CONTEXT = "/";

    private Server server;

    public void iniciar() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("org.glassfish.tyrus.incomingBufferSize", 65536);

        server = new Server(HOST, PORT, CONTEXT, properties,
            ChatEndpoint.class,
            FichajeWifiEndpoint.class);
        server.start();

        System.out.println("══════════════════════════════════════════════════");
        System.out.println("  Servidor WebSocket arrancado en puerto " + PORT);
        System.out.println("  ws://localhost:" + PORT + "/chat    → Chat empleados");
        System.out.println("  ws://localhost:" + PORT + "/fichaje → Fichaje WiFi (app móvil)");
        System.out.println("══════════════════════════════════════════════════");
    }

    public void detener() {
        if (server != null) {
            server.stop();
            System.out.println("[WS] Servidor detenido.");
        }
    }
}
