package com.hosteleria.controller;

import com.hosteleria.model.Evaluacion;
import com.hosteleria.model.Objetivo;
import com.hosteleria.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de acceso a datos para Evaluaciones y Objetivos.
 * Extiende BaseController para reutilizar la gestión de sesión y transacciones.
 */
public class EvaluacionController extends BaseController {

    // ══════════════════════════════════════════════════════════════════
    // EVALUACIONES — consultas
    // ══════════════════════════════════════════════════════════════════

    /** Todas las evaluaciones con empleado y evaluador cargados */
    public List<Evaluacion> getAllEvaluaciones() {
        return executeQuery(
            "SELECT e FROM Evaluacion e JOIN FETCH e.empleado JOIN FETCH e.evaluador ORDER BY e.fecha DESC",
            Evaluacion.class, "evaluaciones"
        );
    }

    /** Historial de un empleado ordenado por fecha descendente */
    public List<Evaluacion> getEvaluacionesPorEmpleado(int idEmpleado) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Evaluacion> result = session.createQuery(
                "SELECT e FROM Evaluacion e " +
                "JOIN FETCH e.empleado " +
                "JOIN FETCH e.evaluador " +
                "WHERE e.empleado.idEmpleado = :id " +
                "ORDER BY e.fecha DESC",
                Evaluacion.class
            ).setParameter("id", idEmpleado).list();
            tx.commit();
            return result;
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("Error getEvaluacionesPorEmpleado: " + ex.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Evaluaciones realizadas por un evaluador concreto */
    public List<Evaluacion> getEvaluacionesPorEvaluador(int idEvaluador) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Evaluacion> result = session.createQuery(
                "SELECT e FROM Evaluacion e " +
                "JOIN FETCH e.empleado " +
                "JOIN FETCH e.evaluador " +
                "WHERE e.evaluador.idEmpleado = :id " +
                "ORDER BY e.fecha DESC",
                Evaluacion.class
            ).setParameter("id", idEvaluador).list();
            tx.commit();
            return result;
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("Error getEvaluacionesPorEvaluador: " + ex.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Evaluaciones en un rango de fechas (para informes de periodo) */
    public List<Evaluacion> getEvaluacionesPorPeriodo(LocalDate desde, LocalDate hasta) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Evaluacion> result = session.createQuery(
                "SELECT e FROM Evaluacion e " +
                "JOIN FETCH e.empleado " +
                "JOIN FETCH e.evaluador " +
                "WHERE e.fecha BETWEEN :desde AND :hasta " +
                "ORDER BY e.puntuacionTotal DESC",
                Evaluacion.class
            ).setParameter("desde", desde).setParameter("hasta", hasta).list();
            tx.commit();
            return result;
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("Error getEvaluacionesPorPeriodo: " + ex.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Evaluaciones completadas / revisadas de un periodo (excluye borradores) */
    public List<Evaluacion> getEvaluacionesCompletadasPorPeriodo(LocalDate desde, LocalDate hasta) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Evaluacion> result = session.createQuery(
                "SELECT e FROM Evaluacion e " +
                "JOIN FETCH e.empleado JOIN FETCH e.evaluador " +
                "WHERE e.fecha BETWEEN :desde AND :hasta " +
                "AND e.estado <> :borrador " +
                "ORDER BY e.puntuacionTotal DESC",
                Evaluacion.class
            ).setParameter("desde", desde)
             .setParameter("hasta", hasta)
             .setParameter("borrador", Evaluacion.EstadoEvaluacion.borrador)
             .list();
            tx.commit();
            return result;
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("Error getEvaluacionesCompletadasPorPeriodo: " + ex.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Evaluación por ID (con empleado, evaluador y objetivos) */
    public Optional<Evaluacion> getEvaluacionPorId(int idEvaluacion) {
        return executeQuerySingle(
            "SELECT e FROM Evaluacion e " +
            "JOIN FETCH e.empleado JOIN FETCH e.evaluador " +
            "WHERE e.idEvaluacion = :id",
            Evaluacion.class, "id", idEvaluacion, "evaluación"
        );
    }

    /** Guarda una nueva evaluación */
    public boolean guardarEvaluacion(Evaluacion evaluacion) {
        return persistir(evaluacion, "evaluación");
    }

    /** Actualiza una evaluación existente */
    public boolean actualizarEvaluacion(Evaluacion evaluacion) {
        return fusionar(evaluacion, "evaluación");
    }

    /** Elimina una evaluación por ID */
    public boolean eliminarEvaluacion(int idEvaluacion) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Evaluacion ev = session.get(Evaluacion.class, idEvaluacion);
            if (ev == null) return false;
            session.remove(ev);
            tx.commit();
            return true;
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("Error eliminarEvaluacion: " + ex.getMessage());
            return false;
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // OBJETIVOS — consultas
    // ══════════════════════════════════════════════════════════════════

    /** Todos los objetivos de un empleado ordenados por fecha límite */
    public List<Objetivo> getObjetivosPorEmpleado(int idEmpleado) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Objetivo> result = session.createQuery(
                "SELECT o FROM Objetivo o " +
                "JOIN FETCH o.empleado " +
                "LEFT JOIN FETCH o.responsable " +
                "WHERE o.empleado.idEmpleado = :id " +
                "ORDER BY CASE o.estado " +
                "  WHEN 'en_progreso' THEN 0 WHEN 'pendiente' THEN 1 " +
                "  WHEN 'completado' THEN 2 ELSE 3 END, " +
                "o.fechaLimite ASC NULLS LAST",
                Objetivo.class
            ).setParameter("id", idEmpleado).list();
            tx.commit();
            return result;
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("Error getObjetivosPorEmpleado: " + ex.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Objetivos activos (pendiente o en_progreso) de todos los empleados */
    public List<Objetivo> getObjetivosActivos() {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Objetivo> result = session.createQuery(
                "SELECT o FROM Objetivo o " +
                "JOIN FETCH o.empleado LEFT JOIN FETCH o.responsable " +
                "WHERE o.estado IN (:p, :ep) " +
                "ORDER BY o.fechaLimite ASC NULLS LAST",
                Objetivo.class
            ).setParameter("p",  Objetivo.EstadoObjetivo.pendiente)
             .setParameter("ep", Objetivo.EstadoObjetivo.en_progreso)
             .list();
            tx.commit();
            return result;
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("Error getObjetivosActivos: " + ex.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Objetivos vencidos (fecha_limite < hoy y estado != completado/cancelado) */
    public List<Objetivo> getObjetivosVencidos() {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Objetivo> result = session.createQuery(
                "SELECT o FROM Objetivo o " +
                "JOIN FETCH o.empleado LEFT JOIN FETCH o.responsable " +
                "WHERE o.fechaLimite < :hoy " +
                "AND o.estado IN (:p, :ep) " +
                "ORDER BY o.fechaLimite ASC",
                Objetivo.class
            ).setParameter("hoy", LocalDate.now())
             .setParameter("p",   Objetivo.EstadoObjetivo.pendiente)
             .setParameter("ep",  Objetivo.EstadoObjetivo.en_progreso)
             .list();
            tx.commit();
            return result;
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("Error getObjetivosVencidos: " + ex.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Guarda un nuevo objetivo */
    public boolean guardarObjetivo(Objetivo objetivo) {
        return persistir(objetivo, "objetivo");
    }

    /** Actualiza un objetivo existente */
    public boolean actualizarObjetivo(Objetivo objetivo) {
        return fusionar(objetivo, "objetivo");
    }

    /** Elimina un objetivo por ID */
    public boolean eliminarObjetivo(int idObjetivo) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Objetivo o = session.get(Objetivo.class, idObjetivo);
            if (o == null) return false;
            session.remove(o);
            tx.commit();
            return true;
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("Error eliminarObjetivo: " + ex.getMessage());
            return false;
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }
}
