package com.hosteleria.controller;

import com.hosteleria.model.Nomina;
import com.hosteleria.model.Propina;
import com.hosteleria.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Collections;
import java.util.List;

/**
 * Controlador de acceso a datos para Nóminas y Propinas.
 */
public class NominaController extends BaseController {

    // ══════════════════════════════════════════════════════
    // NÓMINAS
    // ══════════════════════════════════════════════════════

    /** SELECT * FROM nominas */
    public List<Nomina> getAllNominas() {
        return executeQuery("FROM Nomina", Nomina.class, "nóminas");
    }

    /** Nóminas con el empleado cargado */
    public List<Nomina> getNominasConEmpleado() {
        return executeQuery(
            "SELECT n FROM Nomina n JOIN FETCH n.empleado",
            Nomina.class, "nóminas con empleado"
        );
    }

    /** Nóminas de un empleado concreto ordenadas cronológicamente */
    public List<Nomina> getNominasPorEmpleado(int idEmpleado) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Nomina> resultado = session.createQuery(
                "SELECT n FROM Nomina n JOIN FETCH n.empleado " +
                "WHERE n.empleado.idEmpleado = :id " +
                "ORDER BY n.anio DESC, n.mes DESC",
                Nomina.class
            ).setParameter("id", idEmpleado).list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener nóminas por empleado: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Nóminas pendientes de pago de todos los empleados */
    public List<Nomina> getNominasPendientes() {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Nomina> resultado = session.createQuery(
                "SELECT n FROM Nomina n JOIN FETCH n.empleado " +
                "WHERE n.estado = :estado " +
                "ORDER BY n.anio ASC, n.mes ASC",
                Nomina.class
            ).setParameter("estado", Nomina.EstadoNomina.pendiente).list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener nóminas pendientes: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Guarda o actualiza una nómina */
    public boolean guardarNomina(Nomina nomina) {
        return persistir(nomina, "nómina");
    }

    /** Actualiza una nómina existente */
    public boolean actualizarNomina(Nomina nomina) {
        return fusionar(nomina, "nómina");
    }

    // ══════════════════════════════════════════════════════
    // PROPINAS
    // ══════════════════════════════════════════════════════

    /** SELECT * FROM propinas */
    public List<Propina> getAllPropinas() {
        return executeQuery("FROM Propina", Propina.class, "propinas");
    }

    /** Propinas con el empleado cargado */
    public List<Propina> getPropinasConEmpleado() {
        return executeQuery(
            "SELECT p FROM Propina p JOIN FETCH p.empleado",
            Propina.class, "propinas con empleado"
        );
    }

    /** Propinas de un empleado en un mes y año concretos */
    public List<Propina> getPropinasDeEmpleadoEnMes(int idEmpleado, int mes, int anio) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Propina> resultado = session.createQuery(
                "SELECT p FROM Propina p JOIN FETCH p.empleado " +
                "WHERE p.empleado.idEmpleado = :id " +
                "AND MONTH(p.fecha) = :mes AND YEAR(p.fecha) = :anio",
                Propina.class
            ).setParameter("id", idEmpleado)
             .setParameter("mes", mes)
             .setParameter("anio", anio)
             .list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener propinas del mes: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Guarda una nueva propina */
    public boolean guardarPropina(Propina propina) {
        return persistir(propina, "propina");
    }
}
