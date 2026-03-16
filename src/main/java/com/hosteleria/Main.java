package com.hosteleria;

import com.hosteleria.util.HibernateUtil;
import com.hosteleria.util.WebSocketServer;
import com.hosteleria.websocket.AuthService;

/**
 * Punto de entrada de la aplicación.
 * Arranca Hibernate y el servidor WebSocket.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // 1. Inicializar Hibernate (valida el esquema y crea tablas si no existen)
        System.out.println("[BOOT] Inicializando Hibernate...");
        HibernateUtil.getSessionFactory(); // fuerza la inicialización

        // 2. Arrancar servidor WebSocket
        WebSocketServer wsServer = new WebSocketServer();
        wsServer.iniciar();

        // 3. Hook de apagado limpio al recibir SIGTERM / Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SHUTDOWN] Apagando servidor...");
            wsServer.detener();
            HibernateUtil.shutdown();
            System.out.println("[SHUTDOWN] Servidor detenido correctamente.");
        }));

        // ── Ejemplo: generar un token para el empleado con ID 1 ───────
        // En producción esto lo haría un endpoint HTTP de login
        String tokenEjemplo = AuthService.generarToken(1);
        System.out.println("\n[DEV] Token de prueba para empleado #1: " + tokenEjemplo);
        System.out.println("[DEV] Úsalo en el primer mensaje AUTH desde la app.");
        System.out.println("\nServidor en marcha. Ctrl+C para detener.\n");

        // Mantener vivo el hilo principal
        Thread.currentThread().join();
    }
}
