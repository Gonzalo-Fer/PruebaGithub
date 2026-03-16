package com.hosteleria.controller;

import com.hosteleria.model.*;
import com.hosteleria.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de acceso a datos para el sistema de gestión de hostelería.
 *
 * Organizado en dos niveles por entidad:
 *   - getAll*()             → SELECT simple, sin relaciones (ligero, para listados)
 *   - get*Con*()            → JOIN FETCH explícito, solo carga lo que se necesita
 *   - get*Completo*(id)     → Carga completa de un único registro con todas sus relaciones
 */
public class HosteleriaController {

    // ÁREAS

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

    // PUESTOS

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

    // EMPLEADOS
    

    /** SELECT * FROM empleados (sin relaciones — para listados rápidos) */
    public List<Empleado> getAllEmpleados() {
        return executeQuery("FROM Empleado", Empleado.class, "empleados");
    }

    /** Empleados con su área y puesto cargados (ficha básica) */
    public List<Empleado> getEmpleadosConAreaYPuesto() {
        return executeQuery(
            "SELECT e FROM Empleado e " +
            "LEFT JOIN FETCH e.area " +
            "LEFT JOIN FETCH e.puesto",
            Empleado.class, "empleados con área y puesto"
        );
    }

    /** Guarda un nuevo empleado (alta). */
    public boolean guardarEmpleado(Empleado empleado) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.persist(empleado);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al guardar empleado: " + e.getMessage());
            return false;
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /** Actualiza un empleado existente (baja, reactivación, etc.). */
    public boolean actualizarEmpleado(Empleado empleado) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.merge(empleado);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al actualizar empleado: " + e.getMessage());
            return false;
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

    /** Solo empleados activos con área y puesto (caso de uso habitual en gestión de turnos) */
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

    /**
     * Ficha completa de un empleado: área, puesto, turnos, fichajes,
     * nóminas, ausencias, formaciones y evaluaciones.
     * Solo recomendado para consultas individuales (pantalla de detalle).
     * Nunca usar en listados — usar métodos específicos en su lugar.
     */
    public Optional<Empleado> getEmpleadoCompleto(int idEmpleado) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Relaciones ManyToOne en una sola query
            Empleado empleado = session.createQuery(
                "SELECT e FROM Empleado e " +
                "LEFT JOIN FETCH e.area " +
                "LEFT JOIN FETCH e.puesto " +
                "WHERE e.idEmpleado = :id",
                Empleado.class
            ).setParameter("id", idEmpleado).uniqueResult();

            if (empleado != null) {
                // Colecciones en queries separadas para evitar producto cartesiano
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

    // Historial laboral y contratos

    public List<HistorialLaboral> getHistorialLaboralPorEmpleado(int idEmpleado) {
        return executeQuery(
            "FROM HistorialLaboral h WHERE h.empleado.idEmpleado = " + idEmpleado + " ORDER BY h.fecha DESC",
            HistorialLaboral.class, "historial laboral");
    }

    public List<Contrato> getContratosPorEmpleado(int idEmpleado) {
        return executeQuery(
            "FROM Contrato c WHERE c.empleado.idEmpleado = " + idEmpleado + " ORDER BY c.fechaInicio DESC",
            Contrato.class, "contratos");
    }

    public boolean guardarHistorial(HistorialLaboral h) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.persist(h);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al guardar historial laboral: " + e.getMessage());
            return false;
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

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

    
    // TURNOS
    

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

    
    // FICHAJES
    

    /** SELECT * FROM fichajes */
    public List<Fichaje> getAllFichajes() {
        return executeQuery("FROM Fichaje", Fichaje.class, "fichajes");
    }

    /** Fichajes con empleado y turno cargados */
    public List<Fichaje> getFichajesConEmpleadoYTurno() {
        return executeQuery(
            "SELECT f FROM Fichaje f " +
            "JOIN FETCH f.empleado " +
            "LEFT JOIN FETCH f.turno",
            Fichaje.class, "fichajes con empleado y turno"
        );
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

    // ══════════════════════════════════════════════════════════════════
    // PROPINAS
    // ══════════════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════════════
    // NÓMINAS
    // ══════════════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════════════
    // EVALUACIONES
    // ══════════════════════════════════════════════════════════════════

    /** SELECT * FROM evaluaciones */
    public List<Evaluacion> getAllEvaluaciones() {
        return executeQuery("FROM Evaluacion", Evaluacion.class, "evaluaciones");
    }

    /** Evaluaciones con empleado evaluado y evaluador cargados */
    public List<Evaluacion> getEvaluacionesConEmpleadoYEvaluador() {
        return executeQuery(
            "SELECT e FROM Evaluacion e " +
            "JOIN FETCH e.empleado " +
            "JOIN FETCH e.evaluador",
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

    // ══════════════════════════════════════════════════════════════════
    // FORMACIÓN
    // ══════════════════════════════════════════════════════════════════

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

    /** Formaciones de un tipo concreto (ej: carnet manipulador) */
    public List<Formacion> getFormacionesPorTipo(Formacion.TipoFormacion tipo) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Formacion> resultado = session.createQuery(
                "SELECT f FROM Formacion f JOIN FETCH f.empleado " +
                "WHERE f.tipo = :tipo",
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

    // ══════════════════════════════════════════════════════════════════
    // AUSENCIAS
    // ══════════════════════════════════════════════════════════════════

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


    // ══════════════════════════════════════════════════════════════════
    // USUARIOS
    // ══════════════════════════════════════════════════════════════════

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

    /** Solo usuarios con acceso a la aplicación de PC */
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

    /** Busca un usuario por username (para login) */
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

    /** Guarda o actualiza un usuario (para registro y edición). */
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
    // MÉTODOS AUXILIARES 

    /**
     * Ejecuta una HQL sin parámetros y devuelve la lista resultante.
     * Centraliza el manejo de sesión y errores para queries simples.
     */
    private <T> List<T> executeQuery(String hql, Class<T> clazz, String contexto) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<T> resultado = session.createQuery(hql, clazz).list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener " + contexto + ": " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /**
     * Ejecuta una HQL con un único parámetro y devuelve un Optional.
     * Útil para búsquedas por ID con JOIN FETCH.
     */
    private <T> Optional<T> executeQuerySingle(String hql, Class<T> clazz,
                                                String paramName, Object paramValue,
                                                String contexto) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            T resultado = session.createQuery(hql, clazz)
                                 .setParameter(paramName, paramValue)
                                 .uniqueResult();
            tx.commit();
            return Optional.ofNullable(resultado);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener " + contexto + ": " + e.getMessage());
            return Optional.empty();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    // 
    // PRESENCIA — métodos extra requeridos por PresenciaService
    // 

    /** Persiste un turno nuevo */
    public boolean guardarTurno(Turno turno) {
        Session session = null; Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.persist(turno);
            tx.commit(); return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) { try { tx.rollback(); } catch (Exception rb) {} }
            System.err.println("Error al guardar turno: " + e.getMessage()); return false;
        } finally { if (session != null && session.isOpen()) session.close(); }
    }

    /** Actualiza un turno existente */
    public boolean actualizarTurno(Turno turno) {
        Session session = null; Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.merge(turno);
            tx.commit(); return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) { try { tx.rollback(); } catch (Exception rb) {} }
            System.err.println("Error al actualizar turno: " + e.getMessage()); return false;
        } finally { if (session != null && session.isOpen()) session.close(); }
    }

    /** Elimina un turno por ID */
    public boolean eliminarTurno(int idTurno) {
        Session session = null; Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Turno t = session.get(Turno.class, idTurno);
            if (t != null) session.remove(t);
            tx.commit(); return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) { try { tx.rollback(); } catch (Exception rb) {} }
            System.err.println("Error al eliminar turno: " + e.getMessage()); return false;
        } finally { if (session != null && session.isOpen()) session.close(); }
    }

    /** Turno por ID con empleado cargado */
    public Optional<Turno> getTurnoPorId(int idTurno) {
        return executeQuerySingle(
            "SELECT t FROM Turno t JOIN FETCH t.empleado WHERE t.idTurno = :id",
            Turno.class, "id", idTurno, "turno por id");
    }

    /** Turno de un empleado en una fecha concreta */
    public Optional<Turno> getTurnoDeEmpleadoEnFecha(int idEmpleado, LocalDate fecha) {
        Session session = null; Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Turno t = session.createQuery(
                "SELECT t FROM Turno t JOIN FETCH t.empleado " +
                "WHERE t.empleado.idEmpleado = :id AND t.fecha = :fecha", Turno.class)
                .setParameter("id", idEmpleado).setParameter("fecha", fecha).uniqueResult();
            tx.commit(); return Optional.ofNullable(t);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) { try { tx.rollback(); } catch (Exception rb) {} }
            return Optional.empty();
        } finally { if (session != null && session.isOpen()) session.close(); }
    }

    /** Turnos en un rango de fechas con empleado cargado (cuadrante) */
    public List<Turno> getTurnosPorRangoConEmpleado(LocalDate desde, LocalDate hasta) {
        Session session = null; Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Turno> res = session.createQuery(
                "SELECT t FROM Turno t JOIN FETCH t.empleado " +
                "WHERE t.fecha BETWEEN :desde AND :hasta " +
                "ORDER BY t.fecha, t.horaInicio", Turno.class)
                .setParameter("desde", desde).setParameter("hasta", hasta).list();
            tx.commit(); return res;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) { try { tx.rollback(); } catch (Exception rb) {} }
            return Collections.emptyList();
        } finally { if (session != null && session.isOpen()) session.close(); }
    }

    /** Turnos programados del día que no tienen ningún fichaje de entrada */
    public List<Turno> getTurnosSinFichaje(LocalDate fecha) {
        Session session = null; Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Turno> res = session.createQuery(
                "SELECT t FROM Turno t JOIN FETCH t.empleado " +
                "WHERE t.fecha = :fecha AND t.estado = :estado " +
                "AND NOT EXISTS (SELECT f FROM Fichaje f WHERE f.turno = t)", Turno.class)
                .setParameter("fecha", fecha)
                .setParameter("estado", Turno.EstadoTurno.programado).list();
            tx.commit(); return res;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) { try { tx.rollback(); } catch (Exception rb) {} }
            return Collections.emptyList();
        } finally { if (session != null && session.isOpen()) session.close(); }
    }

    /** Persiste un fichaje nuevo */
    public boolean guardarFichaje(Fichaje fichaje) {
        Session session = null; Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.persist(fichaje);
            tx.commit(); return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) { try { tx.rollback(); } catch (Exception rb) {} }
            System.err.println("Error al guardar fichaje: " + e.getMessage()); return false;
        } finally { if (session != null && session.isOpen()) session.close(); }
    }

    /** Actualiza un fichaje existente */
    public boolean actualizarFichaje(Fichaje fichaje) {
        Session session = null; Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.merge(fichaje);
            tx.commit(); return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) { try { tx.rollback(); } catch (Exception rb) {} }
            System.err.println("Error al actualizar fichaje: " + e.getMessage()); return false;
        } finally { if (session != null && session.isOpen()) session.close(); }
    }

    /** Fichaje por ID con empleado y turno cargados */
    public Optional<Fichaje> getFichajePorId(int idFichaje) {
        return executeQuerySingle(
            "SELECT f FROM Fichaje f JOIN FETCH f.empleado LEFT JOIN FETCH f.turno WHERE f.idFichaje = :id",
            Fichaje.class, "id", idFichaje, "fichaje por id");
    }

    /** Fichaje abierto (sin horaSalida) de un empleado en una fecha */
    public Optional<Fichaje> getFichajeAbiertoHoy(int idEmpleado, LocalDate fecha) {
        Session session = null; Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Fichaje f = session.createQuery(
                "SELECT f FROM Fichaje f JOIN FETCH f.empleado LEFT JOIN FETCH f.turno " +
                "WHERE f.empleado.idEmpleado = :id AND f.fecha = :fecha AND f.horaSalida IS NULL",
                Fichaje.class)
                .setParameter("id", idEmpleado).setParameter("fecha", fecha).uniqueResult();
            tx.commit(); return Optional.ofNullable(f);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) { try { tx.rollback(); } catch (Exception rb) {} }
            return Optional.empty();
        } finally { if (session != null && session.isOpen()) session.close(); }
    }

    /** Todos los fichajes sin hora de salida en una fecha */
    public List<Fichaje> getFichajesAbiertos(LocalDate fecha) {
        Session session = null; Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Fichaje> res = session.createQuery(
                "SELECT f FROM Fichaje f JOIN FETCH f.empleado LEFT JOIN FETCH f.turno " +
                "WHERE f.fecha = :fecha AND f.horaSalida IS NULL", Fichaje.class)
                .setParameter("fecha", fecha).list();
            tx.commit(); return res;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) { try { tx.rollback(); } catch (Exception rb) {} }
            return Collections.emptyList();
        } finally { if (session != null && session.isOpen()) session.close(); }
    }

    /** Fichajes con retraso >= umbralMin en un rango de fechas */
    public List<Fichaje> getFichajesConRetrasoEnRango(LocalDate desde, LocalDate hasta, int umbralMin) {
        Session session = null; Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Fichaje> res = session.createQuery(
                "SELECT f FROM Fichaje f JOIN FETCH f.empleado LEFT JOIN FETCH f.turno " +
                "WHERE f.fecha BETWEEN :desde AND :hasta AND f.retrasoMinutos >= :umbral " +
                "ORDER BY f.fecha DESC, f.retrasoMinutos DESC", Fichaje.class)
                .setParameter("desde", desde).setParameter("hasta", hasta)
                .setParameter("umbral", umbralMin).list();
            tx.commit(); return res;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) { try { tx.rollback(); } catch (Exception rb) {} }
            return Collections.emptyList();
        } finally { if (session != null && session.isOpen()) session.close(); }
    }


    public static void main(String[] args) {
        HosteleriaController ctrl = new HosteleriaController();
        LocalDate hoy = LocalDate.now();

        // ── Listados básicos ──────────────────────────────────────────
        System.out.println("=== EMPLEADOS ACTIVOS CON ÁREA Y PUESTO ===");
        ctrl.getEmpleadosActivosConAreaYPuesto().forEach(e ->
            System.out.printf("  [%d] %s %s | Área: %s | Puesto: %s%n",
                e.getIdEmpleado(), e.getNombre(), e.getApellidos(),
                e.getArea()   != null ? e.getArea().getNombre()    : "—",
                e.getPuesto() != null ? e.getPuesto().getTitulo()  : "—")
        );

        // ── Turnos del día ────────────────────────────────────────────
        System.out.println("\n=== TURNOS DE HOY ===");
        ctrl.getTurnosPorFechaConEmpleado(hoy).forEach(t ->
            System.out.printf("  [%d] %s %s | %s → %s | %s%n",
                t.getIdTurno(),
                t.getEmpleado().getNombre(), t.getEmpleado().getApellidos(),
                t.getHoraInicio(), t.getHoraFin(), t.getEstado())
        );

        // ── Nóminas pendientes ────────────────────────────────────────
        System.out.println("\n=== NÓMINAS PENDIENTES DE PAGO ===");
        ctrl.getNominasPendientes().forEach(n ->
            System.out.printf("  [%d] %s %s | %02d/%d | Neto: %.2f€%n",
                n.getIdNomina(),
                n.getEmpleado().getNombre(), n.getEmpleado().getApellidos(),
                n.getMes(), n.getAnio(), n.getTotalNeto())
        );

        // ── Ausencias pendientes de aprobar ───────────────────────────
        System.out.println("\n=== AUSENCIAS PENDIENTES DE APROBACIÓN ===");
        ctrl.getAusenciasPendientes().forEach(a ->
            System.out.printf("  [%d] %s %s | %s | %s → %s%n",
                a.getIdAusencia(),
                a.getEmpleado().getNombre(), a.getEmpleado().getApellidos(),
                a.getTipo(), a.getFechaInicio(), a.getFechaFin())
        );

        // ── Formaciones próximas a caducar (próximos 3 meses) ─────────
        System.out.println("\n=== CERTIFICACIONES QUE CADUCAN EN 3 MESES ===");
        ctrl.getFormacionesProximasACaducar(hoy.plusMonths(3)).forEach(f ->
            System.out.printf("  %s %s | %s | Caduca: %s%n",
                f.getEmpleado().getNombre(), f.getEmpleado().getApellidos(),
                f.getCurso(), f.getFechaCaducidad())
        );

        // ── Quién está de baja o vacaciones hoy ───────────────────────
        System.out.println("\n=== EMPLEADOS FUERA HOY ===");
        ctrl.getAusenciasActivasEnFecha(hoy).forEach(a ->
            System.out.printf("  %s %s | Motivo: %s%n",
                a.getEmpleado().getNombre(), a.getEmpleado().getApellidos(),
                a.getTipo())
        );

        // ── Ficha completa de empleado con ID 1 ───────────────────────
        System.out.println("\n=== FICHA COMPLETA EMPLEADO #1 ===");
        ctrl.getEmpleadoCompleto(1).ifPresentOrElse(
            e -> {
                System.out.printf("  Nombre  : %s %s%n",   e.getNombre(), e.getApellidos());
                System.out.printf("  Área    : %s%n",       e.getArea()   != null ? e.getArea().getNombre()   : "—");
                System.out.printf("  Puesto  : %s%n",       e.getPuesto() != null ? e.getPuesto().getTitulo() : "—");
                System.out.printf("  Turnos  : %d%n",       e.getTurnos().size());
                System.out.printf("  Fichajes: %d%n",       e.getFichajes().size());
                System.out.printf("  Nóminas : %d%n",       e.getNominas().size());
                System.out.printf("  Ausencias: %d%n",      e.getAusencias().size());
                System.out.printf("  Formaciones: %d%n",    e.getFormaciones().size());
                System.out.printf("  Evaluaciones: %d%n",   e.getEvaluaciones().size());
            },
            () -> System.out.println("  Empleado no encontrado.")
        );

        HibernateUtil.shutdown();
    }
}
