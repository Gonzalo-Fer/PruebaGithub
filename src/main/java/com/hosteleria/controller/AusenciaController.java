package com.hosteleria.controller;

import com.hosteleria.model.Ausencia;
import com.hosteleria.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Controlador de acceso a datos para Ausencias.
 */
public class AusenciaController extends BaseController {

    /** SELECT * FROM ausencias */
    public List<Ausencia> getAllAusencias() {
        return executeQuery("FROM Ausencia", Ausencia.class, "ausencias");
    }

    /** Ausencias con el empleado cargado */
    public List<Ausencia> getAusenciasConEmpleado() {
        return executeQuery(
            "SELECT a FROM Ausencia a JOIN FETCH a.empleado",
            Ausencia.class, "ausencias con empleado"
        );
    }

    /** Ausencias pendientes de aprobar */
    public List<Ausencia> getAusenciasPendientes() {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Ausencia> resultado = session.createQuery(
                "SELECT a FROM Ausencia a JOIN FETCH a.empleado " +
                "WHERE a.estado = :estado " +
                "ORDER BY a.fechaInicio ASC",
                Ausencia.class
            ).setParameter("estado", Ausencia.EstadoAusencia.solicitado).list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener ausencias pendientes: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Ausencias activas en una fecha concreta (empleados fuera ese día) */
    public List<Ausencia> getAusenciasActivasEnFecha(LocalDate fecha) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Ausencia> resultado = session.createQuery(
                "SELECT a FROM Ausencia a JOIN FETCH a.empleado " +
                "WHERE a.estado = 'aprobado' " +
                "AND a.fechaInicio <= :fecha AND a.fechaFin >= :fecha",
                Ausencia.class
            ).setParameter("fecha", fecha).list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener ausencias activas en fecha: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Ausencias de un empleado concreto */
    public List<Ausencia> getAusenciasPorEmpleado(int idEmpleado) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Ausencia> resultado = session.createQuery(
                "SELECT a FROM Ausencia a JOIN FETCH a.empleado " +
                "WHERE a.empleado.idEmpleado = :id " +
                "ORDER BY a.fechaInicio DESC",
                Ausencia.class
            ).setParameter("id", idEmpleado).list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener ausencias por empleado: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Guarda una nueva ausencia */
    public boolean guardarAusencia(Ausencia ausencia) {
        return persistir(ausencia, "ausencia");
    }

    /** Actualiza una ausencia existente (aprobar, rechazar, cancelar...) */
    public boolean actualizarAusencia(Ausencia ausencia) {
        return fusionar(ausencia, "ausencia");
    }
}
