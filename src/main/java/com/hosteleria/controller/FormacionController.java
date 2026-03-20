package com.hosteleria.controller;

import com.hosteleria.model.Evaluacion;
import com.hosteleria.model.Formacion;
import com.hosteleria.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Controlador de acceso a datos para Formación y Evaluaciones.
 */
public class FormacionController extends BaseController {

    // FORMACIÓN
    

    /** SELECT * FROM formacion */
    public List<Formacion> getAllFormaciones() {
        return executeQuery("FROM Formacion", Formacion.class, "formaciones");
    }

    /** Formaciones con el empleado cargado */
    public List<Formacion> getFormacionesConEmpleado() {
        return executeQuery(
            "SELECT f FROM Formacion f JOIN FETCH f.empleado",
            Formacion.class, "formaciones con empleado"
        );
    }

    /** Formaciones cuyo certificado está próximo a caducar */
    public List<Formacion> getFormacionesProximasACaducar(LocalDate hasta) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Formacion> resultado = session.createQuery(
                "SELECT f FROM Formacion f JOIN FETCH f.empleado " +
                "WHERE f.certificado = true " +
                "AND f.fechaCaducidad <= :hasta " +
                "ORDER BY f.fechaCaducidad ASC",
                Formacion.class
            ).setParameter("hasta", hasta).list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener formaciones próximas a caducar: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Formaciones de un tipo concreto (ej: manipulador_alimentos) */
    public List<Formacion> getFormacionesPorTipo(Formacion.TipoFormacion tipo) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Formacion> resultado = session.createQuery(
                "SELECT f FROM Formacion f JOIN FETCH f.empleado WHERE f.tipo = :tipo",
                Formacion.class
            ).setParameter("tipo", tipo).list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener formaciones por tipo: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Guarda una nueva formación */
    public boolean guardarFormacion(Formacion formacion) {
        return persistir(formacion, "formación");
    }

    /** Actualiza una formación existente */
    public boolean actualizarFormacion(Formacion formacion) {
        return fusionar(formacion, "formación");
    }

    
    // EVALUACIONES


    /** SELECT * FROM evaluaciones */
    public List<Evaluacion> getAllEvaluaciones() {
        return executeQuery("FROM Evaluacion", Evaluacion.class, "evaluaciones");
    }

    /** Evaluaciones con empleado evaluado y evaluador cargados */
    public List<Evaluacion> getEvaluacionesConEmpleadoYEvaluador() {
        return executeQuery(
            "SELECT e FROM Evaluacion e JOIN FETCH e.empleado JOIN FETCH e.evaluador",
            Evaluacion.class, "evaluaciones con empleado y evaluador"
        );
    }

    /** Historial de evaluaciones de un empleado concreto */
    public List<Evaluacion> getEvaluacionesPorEmpleado(int idEmpleado) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Evaluacion> resultado = session.createQuery(
                "SELECT e FROM Evaluacion e " +
                "JOIN FETCH e.empleado " +
                "JOIN FETCH e.evaluador " +
                "WHERE e.empleado.idEmpleado = :id " +
                "ORDER BY e.fecha DESC",
                Evaluacion.class
            ).setParameter("id", idEmpleado).list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener evaluaciones por empleado: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Guarda una nueva evaluación */
    public boolean guardarEvaluacion(Evaluacion evaluacion) {
        return persistir(evaluacion, "evaluación");
    }

    /** Actualiza una evaluación existente */
    public boolean actualizarEvaluacion(Evaluacion evaluacion) {
        return fusionar(evaluacion, "evaluación");
    }
}
