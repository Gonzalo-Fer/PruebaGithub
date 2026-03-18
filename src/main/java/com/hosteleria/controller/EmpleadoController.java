package com.hosteleria.controller;

import com.hosteleria.model.*;
import com.hosteleria.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de acceso a datos para Empleados, Áreas, Puestos,
 * Contratos e Historial Laboral.
 */
public class EmpleadoController extends BaseController {

    // ══════════════════════════════════════════════════════
    // ÁREAS
    // ══════════════════════════════════════════════════════

    /** SELECT * FROM areas */
    public List<Area> getAllAreas() {
        return executeQuery("FROM Area", Area.class, "áreas");
    }

    /** Áreas con su lista de empleados cargada */
    public List<Area> getAreasConEmpleados() {
        return executeQuery(
            "SELECT DISTINCT a FROM Area a LEFT JOIN FETCH a.empleados",
            Area.class, "áreas con empleados"
        );
    }

    /** Un área concreta con sus empleados */
    public Optional<Area> getAreaCompleta(int idArea) {
        return executeQuerySingle(
            "SELECT a FROM Area a LEFT JOIN FETCH a.empleados WHERE a.idArea = :id",
            Area.class, "id", idArea, "área completa"
        );
    }

    // ══════════════════════════════════════════════════════
    // PUESTOS
    // ══════════════════════════════════════════════════════

    /** SELECT * FROM puestos */
    public List<Puesto> getAllPuestos() {
        return executeQuery("FROM Puesto", Puesto.class, "puestos");
    }

    /** Puestos con sus empleados asignados */
    public List<Puesto> getPuestosConEmpleados() {
        return executeQuery(
            "SELECT DISTINCT p FROM Puesto p LEFT JOIN FETCH p.empleados",
            Puesto.class, "puestos con empleados"
        );
    }

    // ══════════════════════════════════════════════════════
    // EMPLEADOS
    // ══════════════════════════════════════════════════════

    /** SELECT * FROM empleados (sin relaciones — para listados rápidos) */
    public List<Empleado> getAllEmpleados() {
        return executeQuery("FROM Empleado", Empleado.class, "empleados");
    }

    /** Empleados con su área y puesto cargados (ficha básica) */
    public List<Empleado> getEmpleadosConAreaYPuesto() {
        return executeQuery(
            "SELECT e FROM Empleado e LEFT JOIN FETCH e.area LEFT JOIN FETCH e.puesto",
            Empleado.class, "empleados con área y puesto"
        );
    }

    /** Solo empleados activos con área y puesto */
    public List<Empleado> getEmpleadosActivosConAreaYPuesto() {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Empleado> resultado = session.createQuery(
                "SELECT e FROM Empleado e " +
                "LEFT JOIN FETCH e.area " +
                "LEFT JOIN FETCH e.puesto " +
                "WHERE e.estado = :estado",
                Empleado.class
            ).setParameter("estado", Empleado.EstadoEmpleado.activo).list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener empleados activos: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Empleados con sus turnos asignados */
    public List<Empleado> getEmpleadosConTurnos() {
        return executeQuery(
            "SELECT DISTINCT e FROM Empleado e LEFT JOIN FETCH e.turnos",
            Empleado.class, "empleados con turnos"
        );
    }

    /** Empleados con sus nóminas */
    public List<Empleado> getEmpleadosConNominas() {
        return executeQuery(
            "SELECT DISTINCT e FROM Empleado e LEFT JOIN FETCH e.nominas",
            Empleado.class, "empleados con nóminas"
        );
    }

    /** Empleados con sus ausencias */
    public List<Empleado> getEmpleadosConAusencias() {
        return executeQuery(
            "SELECT DISTINCT e FROM Empleado e LEFT JOIN FETCH e.ausencias",
            Empleado.class, "empleados con ausencias"
        );
    }

    /** Empleados con su historial de formaciones */
    public List<Empleado> getEmpleadosConFormacion() {
        return executeQuery(
            "SELECT DISTINCT e FROM Empleado e LEFT JOIN FETCH e.formaciones",
            Empleado.class, "empleados con formación"
        );
    }

    /** Empleados con sus evaluaciones de desempeño */
    public List<Empleado> getEmpleadosConEvaluaciones() {
        return executeQuery(
            "SELECT DISTINCT e FROM Empleado e LEFT JOIN FETCH e.evaluaciones",
            Empleado.class, "empleados con evaluaciones"
        );
    }

    /**
     * Ficha completa de un empleado: área, puesto, turnos, fichajes,
     * nóminas, ausencias, formaciones y evaluaciones.
     * Solo recomendado para consultas individuales (pantalla de detalle).
     */
    public Optional<Empleado> getEmpleadoCompleto(int idEmpleado) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Empleado empleado = session.createQuery(
                "SELECT e FROM Empleado e " +
                "LEFT JOIN FETCH e.area " +
                "LEFT JOIN FETCH e.puesto " +
                "WHERE e.idEmpleado = :id",
                Empleado.class
            ).setParameter("id", idEmpleado).uniqueResult();

            if (empleado != null) {
                session.createQuery(
                    "SELECT e FROM Empleado e LEFT JOIN FETCH e.turnos WHERE e.idEmpleado = :id",
                    Empleado.class).setParameter("id", idEmpleado).uniqueResult();
                session.createQuery(
                    "SELECT e FROM Empleado e LEFT JOIN FETCH e.fichajes WHERE e.idEmpleado = :id",
                    Empleado.class).setParameter("id", idEmpleado).uniqueResult();
                session.createQuery(
                    "SELECT e FROM Empleado e LEFT JOIN FETCH e.nominas WHERE e.idEmpleado = :id",
                    Empleado.class).setParameter("id", idEmpleado).uniqueResult();
                session.createQuery(
                    "SELECT e FROM Empleado e LEFT JOIN FETCH e.ausencias WHERE e.idEmpleado = :id",
                    Empleado.class).setParameter("id", idEmpleado).uniqueResult();
                session.createQuery(
                    "SELECT e FROM Empleado e LEFT JOIN FETCH e.formaciones WHERE e.idEmpleado = :id",
                    Empleado.class).setParameter("id", idEmpleado).uniqueResult();
                session.createQuery(
                    "SELECT e FROM Empleado e LEFT JOIN FETCH e.evaluaciones WHERE e.idEmpleado = :id",
                    Empleado.class).setParameter("id", idEmpleado).uniqueResult();
            }

            tx.commit();
            return Optional.ofNullable(empleado);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener empleado completo: " + e.getMessage());
            return Optional.empty();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Persiste un nuevo empleado (alta) */
    public boolean guardarEmpleado(Empleado empleado) {
        return persistir(empleado, "empleado");
    }

    /** Actualiza un empleado existente (baja, reactivación, edición...) */
    public boolean actualizarEmpleado(Empleado empleado) {
        return fusionar(empleado, "empleado");
    }

    // ══════════════════════════════════════════════════════
    // HISTORIAL LABORAL
    // ══════════════════════════════════════════════════════

    /** Historial laboral de un empleado ordenado por fecha descendente */
    public List<HistorialLaboral> getHistorialLaboralPorEmpleado(int idEmpleado) {
        return executeQuery(
            "FROM HistorialLaboral h WHERE h.empleado.idEmpleado = " + idEmpleado + " ORDER BY h.fecha DESC",
            HistorialLaboral.class, "historial laboral"
        );
    }

    /** Guarda una nueva entrada de historial laboral */
    public boolean guardarHistorial(HistorialLaboral h) {
        return persistir(h, "historial laboral");
    }

    // ══════════════════════════════════════════════════════
    // CONTRATOS
    // ══════════════════════════════════════════════════════

    /** Contratos de un empleado ordenados por fecha de inicio descendente */
    public List<Contrato> getContratosPorEmpleado(int idEmpleado) {
        return executeQuery(
            "FROM Contrato c WHERE c.empleado.idEmpleado = " + idEmpleado + " ORDER BY c.fechaInicio DESC",
            Contrato.class, "contratos"
        );
    }

    /** Guarda o actualiza un contrato (INSERT si es nuevo, UPDATE si ya existe) */
    public boolean guardarContrato(Contrato c) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            if (c.getIdContrato() == null) {
                session.persist(c);
            } else {
                session.merge(c);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al guardar contrato: " + e.getMessage());
            return false;
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }
}
