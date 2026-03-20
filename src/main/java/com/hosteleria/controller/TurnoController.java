package com.hosteleria.controller;

import com.hosteleria.model.Fichaje;
import com.hosteleria.model.Turno;
import com.hosteleria.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de acceso a datos para Turnos y Fichajes.
 */
public class TurnoController extends BaseController {

    // ══════════════════════════════════════════════════════
    // TURNOS
    // ══════════════════════════════════════════════════════

    /** SELECT * FROM turnos */
    public List<Turno> getAllTurnos() {
        return executeQuery("FROM Turno", Turno.class, "turnos");
    }

    /** Turnos con el empleado asignado cargado */
    public List<Turno> getTurnosConEmpleado() {
        return executeQuery(
            "SELECT t FROM Turno t JOIN FETCH t.empleado",
            Turno.class, "turnos con empleado"
        );
    }

    /** Turnos de una fecha concreta con el empleado */
    public List<Turno> getTurnosPorFechaConEmpleado(LocalDate fecha) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Turno> resultado = session.createQuery(
                "SELECT t FROM Turno t JOIN FETCH t.empleado WHERE t.fecha = :fecha",
                Turno.class
            ).setParameter("fecha", fecha).list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener turnos por fecha: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Turnos de un empleado concreto con sus fichajes */
    public List<Turno> getTurnosConFichajesPorEmpleado(int idEmpleado) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Turno> resultado = session.createQuery(
                "SELECT DISTINCT t FROM Turno t " +
                "JOIN FETCH t.empleado " +
                "LEFT JOIN FETCH t.fichajes " +
                "WHERE t.empleado.idEmpleado = :id",
                Turno.class
            ).setParameter("id", idEmpleado).list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener turnos con fichajes: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Turnos en un rango de fechas con empleado cargado (cuadrante) */
    public List<Turno> getTurnosPorRangoConEmpleado(LocalDate desde, LocalDate hasta) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Turno> res = session.createQuery(
                "SELECT t FROM Turno t JOIN FETCH t.empleado " +
                "WHERE t.fecha BETWEEN :desde AND :hasta " +
                "ORDER BY t.fecha, t.horaInicio",
                Turno.class
            ).setParameter("desde", desde).setParameter("hasta", hasta).list();
            tx.commit();
            return res;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Turno por ID con empleado cargado */
    public Optional<Turno> getTurnoPorId(int idTurno) {
        return executeQuerySingle(
            "SELECT t FROM Turno t JOIN FETCH t.empleado WHERE t.idTurno = :id",
            Turno.class, "id", idTurno, "turno por id"
        );
    }

    /** Turno de un empleado en una fecha concreta */
    public Optional<Turno> getTurnoDeEmpleadoEnFecha(int idEmpleado, LocalDate fecha) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Turno t = session.createQuery(
                "SELECT t FROM Turno t JOIN FETCH t.empleado " +
                "WHERE t.empleado.idEmpleado = :id AND t.fecha = :fecha",
                Turno.class
            ).setParameter("id", idEmpleado).setParameter("fecha", fecha).uniqueResult();
            tx.commit();
            return Optional.ofNullable(t);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            return Optional.empty();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Turnos programados del día que no tienen ningún fichaje de entrada */
    public List<Turno> getTurnosSinFichaje(LocalDate fecha) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Turno> res = session.createQuery(
                "SELECT t FROM Turno t JOIN FETCH t.empleado " +
                "WHERE t.fecha = :fecha AND t.estado = :estado " +
                "AND NOT EXISTS (SELECT f FROM Fichaje f WHERE f.turno = t)",
                Turno.class
            ).setParameter("fecha", fecha)
             .setParameter("estado", Turno.EstadoTurno.programado)
             .list();
            tx.commit();
            return res;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Persiste un turno nuevo */
    public boolean guardarTurno(Turno turno) {
        return persistir(turno, "turno");
    }

    /** Actualiza un turno existente */
    public boolean actualizarTurno(Turno turno) {
        return fusionar(turno, "turno");
    }

    /** Elimina un turno por ID */
    public boolean eliminarTurno(int idTurno) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Turno t = session.get(Turno.class, idTurno);
            if (t != null) session.remove(t);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al eliminar turno: " + e.getMessage());
            return false;
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    // FICHAJES

    /** SELECT * FROM fichajes */
    public List<Fichaje> getAllFichajes() {
        return executeQuery("FROM Fichaje", Fichaje.class, "fichajes");
    }

    /** Fichajes de un empleado en un rango de fechas */
    public List<Fichaje> getFichajesPorEmpleadoYRango(int idEmpleado, LocalDate desde, LocalDate hasta) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Fichaje> resultado = session.createQuery(
                "SELECT f FROM Fichaje f " +
                "JOIN FETCH f.empleado " +
                "WHERE f.empleado.idEmpleado = :id " +
                "AND f.fecha BETWEEN :desde AND :hasta " +
                "ORDER BY f.fecha ASC",
                Fichaje.class
            ).setParameter("id", idEmpleado)
             .setParameter("desde", desde)
             .setParameter("hasta", hasta)
             .list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener fichajes por rango: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Fichaje abierto (sin horaSalida) de un empleado en una fecha */
    public Optional<Fichaje> getFichajeAbiertoHoy(int idEmpleado, LocalDate fecha) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Fichaje f = session.createQuery(
                "SELECT f FROM Fichaje f JOIN FETCH f.empleado LEFT JOIN FETCH f.turno " +
                "WHERE f.empleado.idEmpleado = :id AND f.fecha = :fecha AND f.horaSalida IS NULL",
                Fichaje.class
            ).setParameter("id", idEmpleado).setParameter("fecha", fecha).uniqueResult();
            tx.commit();
            return Optional.ofNullable(f);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            return Optional.empty();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Todos los fichajes sin hora de salida en una fecha */
    public List<Fichaje> getFichajesAbiertos(LocalDate fecha) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Fichaje> res = session.createQuery(
                "SELECT f FROM Fichaje f JOIN FETCH f.empleado LEFT JOIN FETCH f.turno " +
                "WHERE f.fecha = :fecha AND f.horaSalida IS NULL",
                Fichaje.class
            ).setParameter("fecha", fecha).list();
            tx.commit();
            return res;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Fichajes con retraso >= umbralMin en un rango de fechas */
    public List<Fichaje> getFichajesConRetrasoEnRango(LocalDate desde, LocalDate hasta, int umbralMin) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Fichaje> res = session.createQuery(
                "SELECT f FROM Fichaje f JOIN FETCH f.empleado LEFT JOIN FETCH f.turno " +
                "WHERE f.fecha BETWEEN :desde AND :hasta AND f.retrasoMinutos >= :umbral " +
                "ORDER BY f.fecha DESC, f.retrasoMinutos DESC",
                Fichaje.class
            ).setParameter("desde", desde).setParameter("hasta", hasta)
             .setParameter("umbral", umbralMin).list();
            tx.commit();
            return res;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Fichajes con empleado y turno cargados */
    public List<Fichaje> getFichajesConEmpleadoConTurno() {
        return executeQuery(
            "SELECT f FROM Fichaje f JOIN FETCH f.empleado LEFT JOIN FETCH f.turno",
            Fichaje.class, "fichajes con empleado y turno"
        );
}

    /** Fichaje por ID con empleado y turno cargados */
    public Optional<Fichaje> getFichajePorId(int idFichaje) {
        return executeQuerySingle(
            "SELECT f FROM Fichaje f JOIN FETCH f.empleado LEFT JOIN FETCH f.turno WHERE f.idFichaje = :id",
            Fichaje.class, "id", idFichaje, "fichaje por id"
        );
    }

    /** Persiste un fichaje nuevo */
    public boolean guardarFichaje(Fichaje fichaje) {
        return persistir(fichaje, "fichaje");
    }

    /** Actualiza un fichaje existente */
    public boolean actualizarFichaje(Fichaje fichaje) {
        return fusionar(fichaje, "fichaje");
    }
}
