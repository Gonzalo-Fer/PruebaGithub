package com.hosteleria.controller;

import com.hosteleria.model.Usuario;
import com.hosteleria.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de acceso a datos para Usuarios.
 */
public class UsuarioController extends BaseController {

    /** SELECT * FROM usuarios */
    public List<Usuario> getAllUsuarios() {
        return executeQuery("FROM Usuario", Usuario.class, "usuarios");
    }

    /** Usuarios con su empleado asociado cargado */
    public List<Usuario> getUsuariosConEmpleado() {
        return executeQuery(
            "SELECT u FROM Usuario u LEFT JOIN FETCH u.empleado",
            Usuario.class, "usuarios con empleado"
        );
    }

    /** Solo usuarios activos con acceso a la aplicación de PC */
    public List<Usuario> getUsuariosConAccesoPC() {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Usuario> resultado = session.createQuery(
                "SELECT u FROM Usuario u LEFT JOIN FETCH u.empleado " +
                "WHERE u.accesoPC = true AND u.activo = true " +
                "ORDER BY u.rol ASC, u.nombre ASC",
                Usuario.class
            ).list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener usuarios con acceso PC: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Busca un usuario activo por username (para login) */
    public Optional<Usuario> getUsuarioPorUsername(String username) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Usuario resultado = session.createQuery(
                "SELECT u FROM Usuario u LEFT JOIN FETCH u.empleado " +
                "WHERE u.username = :username AND u.activo = true",
                Usuario.class
            ).setParameter("username", username).uniqueResult();
            tx.commit();
            return Optional.ofNullable(resultado);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al buscar usuario por username: " + e.getMessage());
            return Optional.empty();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Busca un usuario por email */
    public Optional<Usuario> getUsuarioPorEmail(String email) {
        return executeQuerySingle(
            "SELECT u FROM Usuario u LEFT JOIN FETCH u.empleado WHERE u.email = :id",
            Usuario.class, "id", email, "usuario por email"
        );
    }

    /** Usuarios filtrados por rol */
    public List<Usuario> getUsuariosPorRol(Usuario.Rol rol) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Usuario> resultado = session.createQuery(
                "SELECT u FROM Usuario u LEFT JOIN FETCH u.empleado " +
                "WHERE u.rol = :rol ORDER BY u.nombre ASC",
                Usuario.class
            ).setParameter("rol", rol).list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener usuarios por rol: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Guarda o actualiza un usuario (INSERT si es nuevo, UPDATE si ya existe) */
    public boolean guardarUsuario(Usuario usuario) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            if (usuario.getIdUsuario() == null) {
                session.persist(usuario);
            } else {
                session.merge(usuario);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al guardar usuario: " + e.getMessage());
            return false;
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Actualiza el campo ultimoAcceso al hacer login */
    public void registrarUltimoAcceso(int idUsuario) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.createMutationQuery(
                "UPDATE Usuario u SET u.ultimoAcceso = :ahora WHERE u.idUsuario = :id"
            ).setParameter("ahora", LocalDateTime.now())
             .setParameter("id", idUsuario)
             .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al registrar último acceso: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }
}
