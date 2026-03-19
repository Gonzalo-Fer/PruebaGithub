package com.hosteleria.service;

import com.hosteleria.controller.HosteleriaController;
import com.hosteleria.model.Usuario;
import com.hosteleria.util.PasswordUtil;

import java.util.Optional;

/**
 * Servicio de autenticación: login, registro y validación de usuarios.
 */
public class LoginService {

    private final HosteleriaController ctrl = new HosteleriaController();

    /**
     * Intenta iniciar sesión con username y contraseña.
     * Si es correcto, registra la sesión en SessionManager y actualiza último acceso.
     */
    public ResultadoLogin login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return new ResultadoLogin(false, "Usuario y contraseña son obligatorios.", null);
        }
        Optional<Usuario> opt = ctrl.getUsuarioPorUsername(username.trim());
        if (opt.isEmpty()) {
            return new ResultadoLogin(false, "Usuario o contraseña incorrectos.", null);
        }
        Usuario u = opt.get();
        if (!u.isActivo()) {
            return new ResultadoLogin(false, "La cuenta está desactivada. Contacte con el administrador.", null);
        }
        if (!PasswordUtil.verificar(password, u.getPasswordHash())) {
            return new ResultadoLogin(false, "Usuario o contraseña incorrectos.", null);
        }
        ctrl.registrarUltimoAcceso(u.getIdUsuario());
        SessionManager.getInstance().iniciarSesion(u);
        return new ResultadoLogin(true, null, u);
    }

    /**
     * Registra un nuevo usuario (una vez registrado un administrador debe permitir el acceso).
     * El username y email deben ser únicos.
     */
    public ResultadoRegistro registrar(String nombre, String username, String email, String password,
                                       Usuario.Rol rol, boolean accesoPC, Integer idEmpleado) {
        if (nombre == null || nombre.isBlank()) {
            return new ResultadoRegistro(false, "El nombre es obligatorio.");
        }
        if (username == null || username.isBlank()) {
            return new ResultadoRegistro(false, "El nombre de usuario es obligatorio.");
        }
        if (email == null || email.isBlank()) {
            return new ResultadoRegistro(false, "El email es obligatorio.");
        }
        if (password == null || password.length() < 6) {
            return new ResultadoRegistro(false, "La contraseña debe tener al menos 6 caracteres.");
        }
        username = username.trim();
        email = email.trim().toLowerCase();

        if (ctrl.getUsuarioPorUsername(username).isPresent()) {
            return new ResultadoRegistro(false, "Ya existe un usuario con ese nombre de usuario.");
        }
        if (ctrl.getUsuarioPorEmail(email).isPresent()) {
            return new ResultadoRegistro(false, "Ya existe un usuario con ese email.");
        }

        Usuario u = new Usuario();
        u.setNombre(nombre.trim());
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(PasswordUtil.hash(password));
        u.setRol(rol != null ? rol : Usuario.Rol.EMPLEADO);
        u.setAccesoPC(accesoPC);
        u.setActivo(true);
        if (idEmpleado != null && idEmpleado > 0) {
            ctrl.getEmpleadoCompleto(idEmpleado).ifPresent(u::setEmpleado);
        }

        boolean guardado = ctrl.guardarUsuario(u);
        return guardado
            ? new ResultadoRegistro(true, null)
            : new ResultadoRegistro(false, "Error al guardar el usuario.");
    }

    /**
     * Cierra la sesión actual.
     */
    public void logout() {
        SessionManager.getInstance().cerrarSesion();
    }


    public static final class ResultadoLogin {
        private final boolean ok;
        private final String mensajeError;
        private final Usuario usuario;

        public ResultadoLogin(boolean ok, String mensajeError, Usuario usuario) {
            this.ok = ok;
            this.mensajeError = mensajeError;
            this.usuario = usuario;
        }
        public boolean isOk() { return ok; }
        public String getMensajeError() { return mensajeError; }
        public Usuario getUsuario() { return usuario; }
    }

    public static final class ResultadoRegistro {
        private final boolean ok;
        private final String mensajeError;

        public ResultadoRegistro(boolean ok, String mensajeError) {
            this.ok = ok;
            this.mensajeError = mensajeError;
        }
        public boolean isOk() { return ok; }
        public String getMensajeError() { return mensajeError; }
    }
}
