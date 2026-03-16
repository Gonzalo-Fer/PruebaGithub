package com.hosteleria.service;

import com.hosteleria.controller.HosteleriaController;
import com.hosteleria.model.Ausencia;
import com.hosteleria.model.Ausencia.EstadoAusencia;
import com.hosteleria.model.Ausencia.TipoAusencia;
import com.hosteleria.model.Empleado;
import com.hosteleria.model.Usuario;
import com.hosteleria.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Servicio de ausencias y permisos.
 *
 * Cubre:
 *  - Solicitud de vacaciones por el empleado
 *  - Aprobación / rechazo por el responsable
 *  - Bajas médicas (IT) con seguimiento
 *  - Permisos retribuidos y no retribuidos
 *  - Calendario de ausencias (ausencias por rango de fechas)
 *  - Control de días disponibles por empleado
 */
public class AusenciasService {

    // Días de vacaciones anuales por convenio (hostelería)
    private static final int DIAS_VACACIONES_ANUALES = 30;

    private final HosteleriaController ctrl = new HosteleriaController();

    // ══════════════════════════════════════════════════════════════════
    // SOLICITUD DE VACACIONES / PERMISO
    // ══════════════════════════════════════════════════════════════════

    /**
     * El empleado solicita una ausencia (vacaciones, permiso retribuido,
     * permiso no retribuido...).
     *
     * @return ResultadoAusencia con ok/error y la ausencia creada si tiene éxito
     */
    public ResultadoAusencia solicitarAusencia(int idEmpleado,
                                               TipoAusencia tipo,
                                               LocalDate fechaInicio,
                                               LocalDate fechaFin,
                                               String observaciones) {
        if (fechaInicio == null || fechaFin == null)
            return new ResultadoAusencia(false, "Las fechas son obligatorias.", null);
        if (fechaFin.isBefore(fechaInicio))
            return new ResultadoAusencia(false, "La fecha fin no puede ser anterior a la fecha inicio.", null);
        if (tipo == null)
            return new ResultadoAusencia(false, "El tipo de ausencia es obligatorio.", null);

        // Las bajas médicas se registran aparte (registrarBajaMedica)
        if (tipo == TipoAusencia.baja_medica)
            return new ResultadoAusencia(false, "Para bajas médicas usa 'Registrar baja IT'.", null);

        Empleado empleado = ctrl.getAllEmpleados().stream()
                .filter(e -> e.getIdEmpleado().equals(idEmpleado))
                .findFirst().orElse(null);
        if (empleado == null)
            return new ResultadoAusencia(false, "Empleado no encontrado.", null);

        // Comprobar solapamiento con ausencias ya aprobadas
        List<Ausencia> existentes = ctrl.getAusenciasPorEmpleado(idEmpleado);
        for (Ausencia a : existentes) {
            if (a.getEstado() == EstadoAusencia.aprobado || a.getEstado() == EstadoAusencia.solicitado) {
                if (!fechaInicio.isAfter(a.getFechaFin()) && !fechaFin.isBefore(a.getFechaInicio())) {
                    return new ResultadoAusencia(false,
                            "Las fechas solapan con otra ausencia en estado: " + a.getEstado(), null);
                }
            }
        }

        // Comprobar días disponibles para vacaciones
        if (tipo == TipoAusencia.vacaciones) {
            int diasSolicitados = calcularDiasHabiles(fechaInicio, fechaFin);
            int diasUsados      = getDiasVacacionesUsados(idEmpleado, fechaInicio.getYear());
            int diasDisponibles = DIAS_VACACIONES_ANUALES - diasUsados;
            if (diasSolicitados > diasDisponibles)
                return new ResultadoAusencia(false,
                        "Solo quedan " + diasDisponibles + " días de vacaciones disponibles para " + fechaInicio.getYear(), null);
        }

        Ausencia ausencia = new Ausencia();
        ausencia.setEmpleado(empleado);
        ausencia.setTipo(tipo);
        ausencia.setFechaInicio(fechaInicio);
        ausencia.setFechaFin(fechaFin);
        ausencia.setDiasTotales(calcularDiasHabiles(fechaInicio, fechaFin));
        ausencia.setEstado(EstadoAusencia.solicitado);
        ausencia.setObservaciones(observaciones);
        ausencia.setRetribuido(esRetribuido(tipo));

        Session session = null;
        Transaction tx  = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.persist(ausencia);
            tx.commit();
            return new ResultadoAusencia(true, "Solicitud registrada correctamente.", ausencia);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            return new ResultadoAusencia(false, "Error al guardar: " + e.getMessage(), null);
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // APROBACIÓN / RECHAZO POR EL RESPONSABLE
    // ══════════════════════════════════════════════════════════════════

    /**
     * El responsable aprueba una solicitud de ausencia.
     */
    public ResultadoAusencia aprobarAusencia(int idAusencia, Usuario responsable) {
        return cambiarEstadoAusencia(idAusencia, EstadoAusencia.aprobado, responsable, null);
    }

    /**
     * El responsable rechaza una solicitud de ausencia indicando el motivo.
     */
    public ResultadoAusencia rechazarAusencia(int idAusencia, Usuario responsable, String motivo) {
        if (motivo == null || motivo.isBlank())
            return new ResultadoAusencia(false, "Debes indicar el motivo del rechazo.", null);
        return cambiarEstadoAusencia(idAusencia, EstadoAusencia.rechazado, responsable, motivo);
    }

    private ResultadoAusencia cambiarEstadoAusencia(int idAusencia, EstadoAusencia nuevoEstado,
                                                    Usuario responsable, String motivo) {
        Session session = null;
        Transaction tx  = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Ausencia ausencia = session.get(Ausencia.class, idAusencia);
            if (ausencia == null)
                return new ResultadoAusencia(false, "Ausencia no encontrada.", null);
            if (ausencia.getEstado() != EstadoAusencia.solicitado)
                return new ResultadoAusencia(false,
                        "Solo se pueden aprobar/rechazar solicitudes en estado 'solicitado'.", null);

            ausencia.setEstado(nuevoEstado);
            ausencia.setAprobadoPor(responsable);
            if (motivo != null) ausencia.setMotivoRechazo(motivo);
            session.merge(ausencia);
            tx.commit();
            return new ResultadoAusencia(true,
                    "Ausencia " + nuevoEstado.name() + " correctamente.", ausencia);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            return new ResultadoAusencia(false, "Error: " + e.getMessage(), null);
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // BAJA MÉDICA (IT) CON SEGUIMIENTO
    // ══════════════════════════════════════════════════════════════════

    /**
     * Registra el inicio de una baja médica (IT).
     * La fecha de fin se desconoce al inicio, se puede actualizar al recuperarse.
     */
    public ResultadoAusencia registrarBajaMedica(int idEmpleado, LocalDate fechaInicio,
                                                 String numeroBajaIT, String observaciones) {
        Empleado empleado = ctrl.getAllEmpleados().stream()
                .filter(e -> e.getIdEmpleado().equals(idEmpleado))
                .findFirst().orElse(null);
        if (empleado == null)
            return new ResultadoAusencia(false, "Empleado no encontrado.", null);

        Ausencia ausencia = new Ausencia();
        ausencia.setEmpleado(empleado);
        ausencia.setTipo(TipoAusencia.baja_medica);
        ausencia.setFechaInicio(fechaInicio);
        ausencia.setFechaFin(fechaInicio.plusMonths(6)); // fecha provisional
        ausencia.setDiasTotales(0);                      // se recalcula al cerrar
        ausencia.setEstado(EstadoAusencia.aprobado); // baja IT aprobada automáticamente
        ausencia.setNumeroBajaIT(numeroBajaIT);
        ausencia.setObservaciones(observaciones);
        ausencia.setRetribuido(true);                    // IT → retribuida por SS

        Session session = null;
        Transaction tx  = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.persist(ausencia);
            // Actualizar estado del empleado
            empleado.setEstado(Empleado.EstadoEmpleado.baja_temporal);
            session.merge(empleado);
            tx.commit();
            return new ResultadoAusencia(true, "Baja médica registrada.", ausencia);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            return new ResultadoAusencia(false, "Error: " + e.getMessage(), null);
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /**
     * Registra el alta médica: cierra la baja y reactiva al empleado.
     */
    public ResultadoAusencia registrarAltaMedica(int idAusencia, LocalDate fechaAlta) {
        Session session = null;
        Transaction tx  = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Ausencia ausencia = session.get(Ausencia.class, idAusencia);
            if (ausencia == null)
                return new ResultadoAusencia(false, "Baja no encontrada.", null);
            if (ausencia.getTipo() != TipoAusencia.baja_medica)
                return new ResultadoAusencia(false, "Esta ausencia no es una baja médica.", null);

            ausencia.setFechaFin(fechaAlta);
            ausencia.setFechaAltaMedica(fechaAlta);
            ausencia.setDiasTotales(calcularDiasHabiles(ausencia.getFechaInicio(), fechaAlta));
            ausencia.setEstado(EstadoAusencia.aprobado);

            Empleado empleado = ausencia.getEmpleado();
            empleado.setEstado(Empleado.EstadoEmpleado.activo);

            session.merge(ausencia);
            session.merge(empleado);
            tx.commit();
            return new ResultadoAusencia(true, "Alta médica registrada. Empleado reactivado.", ausencia);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            return new ResultadoAusencia(false, "Error: " + e.getMessage(), null);
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // CALENDARIO DE AUSENCIAS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Devuelve todas las ausencias aprobadas / en seguimiento entre dos fechas.
     * Útil para pintar el calendario mensual de ausencias.
     */
    public List<Ausencia> getCalendarioAusencias(LocalDate desde, LocalDate hasta) {
        Session session = null;
        Transaction tx  = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Ausencia> result = session.createQuery(
                            "SELECT a FROM Ausencia a JOIN FETCH a.empleado " +
                                    "WHERE a.estado IN ('aprobado') " +
                                    "AND a.fechaInicio <= :hasta AND a.fechaFin >= :desde " +
                                    "ORDER BY a.fechaInicio ASC",
                            Ausencia.class)
                    .setParameter("desde", desde)
                    .setParameter("hasta", hasta)
                    .list();
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("Error calendario ausencias: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // CONTROL DE DÍAS DISPONIBLES
    // ══════════════════════════════════════════════════════════════════

    /**
     * Días de vacaciones ya usados (aprobados) por un empleado en un año.
     */
    public int getDiasVacacionesUsados(int idEmpleado, int anio) {
        return ctrl.getAusenciasPorEmpleado(idEmpleado).stream()
                .filter(a -> a.getTipo() == TipoAusencia.vacaciones
                        && a.getEstado() == EstadoAusencia.aprobado
                        && a.getFechaInicio().getYear() == anio)
                .mapToInt(a -> a.getDiasTotales() != null ? a.getDiasTotales() : 0)
                .sum();
    }

    /**
     * Días de vacaciones disponibles para el empleado en el año indicado.
     */
    public int getDiasVacacionesDisponibles(int idEmpleado, int anio) {
        return DIAS_VACACIONES_ANUALES - getDiasVacacionesUsados(idEmpleado, anio);
    }

    /**
     * Resumen completo de días por tipo para mostrar en la ficha del empleado.
     */
    public ResumenDiasAusencia getResumenDias(int idEmpleado, int anio) {
        List<Ausencia> ausencias = ctrl.getAusenciasPorEmpleado(idEmpleado);
        int vacUsadas = 0, permRetUsados = 0, permNoRetUsados = 0, diasBajaMedica = 0;
        for (Ausencia a : ausencias) {
            if (a.getEstado() != EstadoAusencia.aprobado)
                continue;
            if (a.getFechaInicio().getYear() != anio) continue;
            int dias = a.getDiasTotales() != null ? a.getDiasTotales() : 0;
            switch (a.getTipo()) {
                case vacaciones          -> vacUsadas       += dias;
                case permiso_retribuido  -> permRetUsados   += dias;
                case permiso_no_retribuido -> permNoRetUsados += dias;
                case baja_medica         -> diasBajaMedica  += dias;
                default -> {}
            }
        }
        return new ResumenDiasAusencia(
                DIAS_VACACIONES_ANUALES, vacUsadas,
                permRetUsados, permNoRetUsados, diasBajaMedica
        );
    }

    // ══════════════════════════════════════════════════════════════════
    // UTILIDADES PRIVADAS
    // ══════════════════════════════════════════════════════════════════

    /** Calcula días hábiles (lun-vie) entre dos fechas, ambas inclusive. */
    private int calcularDiasHabiles(LocalDate inicio, LocalDate fin) {
        int dias = 0;
        LocalDate d = inicio;
        while (!d.isAfter(fin)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) dias++;
            d = d.plusDays(1);
        }
        return dias;
    }

    /** Determina si el tipo de ausencia es retribuido por defecto. */
    private boolean esRetribuido(TipoAusencia tipo) {
        return switch (tipo) {
            case vacaciones, baja_medica, permiso_retribuido,
                 maternidad_paternidad, permiso_personal -> true;
            case permiso_no_retribuido, asuntos_propios  -> false;
            default                                       -> true;
        };
    }

    // ══════════════════════════════════════════════════════════════════
    // DTOs DE RESULTADO
    // ══════════════════════════════════════════════════════════════════

    public static class ResultadoAusencia {
        public final boolean ok;
        public final String  mensaje;
        public final Ausencia ausencia;
        public ResultadoAusencia(boolean ok, String mensaje, Ausencia ausencia) {
            this.ok = ok; this.mensaje = mensaje; this.ausencia = ausencia;
        }
    }

    public static class ResumenDiasAusencia {
        public final int diasVacacionesTotales;
        public final int diasVacacionesUsados;
        public final int diasVacacionesDisponibles;
        public final int diasPermisosRetribuidos;
        public final int diasPermisosNoRetribuidos;
        public final int diasBajaMedica;
        public ResumenDiasAusencia(int total, int usados, int permRet, int permNoRet, int bajaMed) {
            this.diasVacacionesTotales     = total;
            this.diasVacacionesUsados      = usados;
            this.diasVacacionesDisponibles = total - usados;
            this.diasPermisosRetribuidos   = permRet;
            this.diasPermisosNoRetribuidos = permNoRet;
            this.diasBajaMedica            = bajaMed;
        }
    }
}