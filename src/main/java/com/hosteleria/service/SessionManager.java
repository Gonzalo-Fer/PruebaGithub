package com.hosteleria.service;

import com.hosteleria.model.Usuario;

/**
 * Gestiona la sesión del usuario actual en la aplicación de escritorio.
 * Singleton para acceso global desde la UI.
 */
public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private Usuario usuarioActual;
    private long inicioSesion;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void iniciarSesion(Usuario usuario) {
        this.usuarioActual = usuario;
        this.inicioSesion = System.currentTimeMillis();
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public boolean haySesionActiva() {
        return usuarioActual != null;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public long getInicioSesion() {
        return inicioSesion;
    }

    /** Comprueba si el usuario actual tiene rol ADMIN o GERENTE. */
    public boolean esAdminOGerente() {
        return usuarioActual != null
            && (usuarioActual.getRol() == Usuario.Rol.ADMIN || usuarioActual.getRol() == Usuario.Rol.GERENTE);
    }

    /** Comprueba si el usuario actual es ADMIN. */
    public boolean esAdmin() {
        return usuarioActual != null && usuarioActual.getRol() == Usuario.Rol.ADMIN;
    }
}
