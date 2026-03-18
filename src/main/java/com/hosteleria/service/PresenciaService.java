package com.hosteleria.service;

import com.hosteleria.controller.HosteleriaController;
import com.hosteleria.model.Empleado;
import com.hosteleria.model.Fichaje;
import com.hosteleria.model.Turno;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de Control de Presencia.
 *
 * Cubre todas las funciones del módulo 🕐:
 *   · Crear / modificar / eliminar turnos
 *   · Registrar entrada y salida (llamado desde FichajeWifiEndpoint — app móvil vía WiFi)
 *   · Cálculo automático de horas trabajadas y horas extra
 *   · Detección de retrasos e incidencias
 *   · Alertas de ausencia no justificada
 *   · Cierre de fichajes abiertos
 *
 */
public class PresenciaService {

    /** Minutos de tolerancia que no se contabilizan como retraso */
    public static final int MARGEN_PUNTUALIDAD_MIN = 5;
    /** Retraso mínimo (min) para que se registre como incidencia */
    public static final int UMBRAL_INCIDENCIA_MIN  = 15;

    private final HosteleriaController ctrl = new HosteleriaController();

    // ══════════════════════════════════════════════════════════════════
    // GESTIÓN DE TURNOS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Crea un turno nuevo.
     * horasTrabajadas se calcula automáticamente a partir de inicio y fin.
     */
    public Resultado crearTurno(int idEmpleado, LocalDate fecha,
                                LocalTime horaInicio, LocalTime horaFin,
                                Turno.TipoTurno tipo, String area) {
        if (horaInicio == null || horaFin == null)
            return Resultado.error("Hora inicio y fin son obligatorias.");
        if (!horaFin.isAfter(horaInicio))
            return Resultado.error("La hora de fin debe ser posterior a la de inicio.");

        Optional<Empleado> optEmp = ctrl.getEmpleadoCompleto(idEmpleado);
        if (optEmp.isEmpty())
            return Resultado.error("Empleado no encontrado (id=" + idEmpleado + ").");

        Turno t = new Turno();
        t.setEmpleado(optEmp.get());
        t.setFecha(fecha);
        t.setHoraInicio(horaInicio);
        t.setHoraFin(horaFin);
        t.setTipoTurno(tipo != null ? tipo : Turno.TipoTurno.completo);
        t.setAreaAsignada(area != null && !area.isBlank() ? area : null);
        t.setHorasTrabajadas(minutosAHoras(ChronoUnit.MINUTES.between(horaInicio, horaFin)));
        t.setEstado(Turno.EstadoTurno.programado);

        return ctrl.guardarTurno(t)
            ? Resultado.ok("Turno creado correctamente.")
            : Resultado.error("Error al guardar el turno en la base de datos.");
    }

    /** Modifica los campos de un turno existente. Solo actualiza los campos no nulos. */
    public Resultado modificarTurno(int idTurno, LocalDate fecha,
                                    LocalTime horaInicio, LocalTime horaFin,
                                    Turno.TipoTurno tipo, String area,
                                    Turno.EstadoTurno estado) {
        Optional<Turno> opt = ctrl.getTurnoPorId(idTurno);
        if (opt.isEmpty()) return Resultado.error("Turno no encontrado (id=" + idTurno + ").");
        Turno t = opt.get();
        if (fecha      != null) t.setFecha(fecha);
        if (horaInicio != null) t.setHoraInicio(horaInicio);
        if (horaFin    != null) t.setHoraFin(horaFin);
        if (tipo       != null) t.setTipoTurno(tipo);
        if (area       != null) t.setAreaAsignada(area.isBlank() ? null : area);
        if (estado     != null) t.setEstado(estado);
        if (t.getHoraInicio() != null && t.getHoraFin() != null)
            t.setHorasTrabajadas(minutosAHoras(ChronoUnit.MINUTES.between(t.getHoraInicio(), t.getHoraFin())));
        return ctrl.actualizarTurno(t)
            ? Resultado.ok("Turno actualizado correctamente.")
            : Resultado.error("Error al actualizar el turno.");
    }

    /** Elimina un turno (solo si está en estado programado). */
    public Resultado eliminarTurno(int idTurno) {
        Optional<Turno> opt = ctrl.getTurnoPorId(idTurno);
        if (opt.isEmpty()) return Resultado.error("Turno no encontrado.");
        if (opt.get().getEstado() == Turno.EstadoTurno.completado)
            return Resultado.error("No se puede eliminar un turno ya completado.");
        return ctrl.eliminarTurno(idTurno)
            ? Resultado.ok("Turno eliminado.")
            : Resultado.error("Error al eliminar el turno.");
    }

    // ── Consultas para el cuadrante ───────────────────────────────────

    /** Todos los turnos de una semana (lunes → domingo) con empleado cargado. */
    public List<Turno> getCuadranteSemana(LocalDate lunes) {
        return ctrl.getTurnosPorRangoConEmpleado(lunes, lunes.plusDays(6));
    }

    /** Todos los turnos de un mes completo con empleado cargado. */
    public List<Turno> getCuadranteMes(int anio, int mes) {
        LocalDate ini = LocalDate.of(anio, mes, 1);
        return ctrl.getTurnosPorRangoConEmpleado(ini, ini.withDayOfMonth(ini.lengthOfMonth()));
    }

    // ══════════════════════════════════════════════════════════════════
    // FICHAJE VÍA WIFI (llamado desde FichajeWifiEndpoint)
    // ══════════════════════════════════════════════════════════════════

    /**
     * Registra la ENTRADA de un empleado.
     *
     * La app móvil detecta automáticamente el SSID corporativo y envía
     * el evento al servidor WebSocket en /fichaje.
     * FichajeWifiEndpoint llama a este método con la hora del dispositivo.
     *
     * @param idEmpleado  ID del empleado que ficha
     * @param horaEntrada hora enviada por la app (LocalTime.now() si null)
     */
    public ResultadoFichaje registrarEntrada(int idEmpleado, LocalTime horaEntrada) {
        LocalDate hoy = LocalDate.now();
        if (horaEntrada == null) horaEntrada = LocalTime.now();

        // ¿Ya hay un fichaje abierto hoy?
        if (ctrl.getFichajeAbiertoHoy(idEmpleado, hoy).isPresent())
            return ResultadoFichaje.error("Ya hay un fichaje de entrada registrado para hoy.");

        Optional<Empleado> optEmp = ctrl.getEmpleadoCompleto(idEmpleado);
        if (optEmp.isEmpty())
            return ResultadoFichaje.error("Empleado no encontrado (id=" + idEmpleado + ").");

        Fichaje f = new Fichaje();
        f.setEmpleado(optEmp.get());
        f.setFecha(hoy);
        f.setHoraEntrada(horaEntrada);
        f.setHorasExtra(BigDecimal.ZERO);
        f.setRetrasoMinutos(0);

        // Buscar turno del día para calcular retraso
        Optional<Turno> optTurno = ctrl.getTurnoDeEmpleadoEnFecha(idEmpleado, hoy);
        boolean hayRetraso = false;
        int minRetraso = 0;

        if (optTurno.isPresent()) {
            f.setTurno(optTurno.get());
            long diff = ChronoUnit.MINUTES.between(optTurno.get().getHoraInicio(), horaEntrada);
            if (diff > MARGEN_PUNTUALIDAD_MIN) {
                minRetraso = (int) diff;
                hayRetraso = true;
                f.setRetrasoMinutos(minRetraso);
                if (minRetraso >= UMBRAL_INCIDENCIA_MIN)
                    f.setObservaciones("⚠️ Retraso de " + minRetraso + " min (fichaje WiFi).");
            }
        } else {
            f.setObservaciones("Sin turno asignado para hoy.");
        }

        if (!ctrl.guardarFichaje(f))
            return ResultadoFichaje.error("Error al guardar el fichaje en la base de datos.");

        String msg = hayRetraso
            ? "Entrada registrada. Retraso: " + minRetraso + " min."
            : "Entrada registrada correctamente.";
        return new ResultadoFichaje(true, msg, f, hayRetraso);
    }

    /**
     * Registra la SALIDA de un empleado.
     * Calcula horas trabajadas y horas extra respecto al turno planificado.
     */
    public ResultadoFichaje registrarSalida(int idEmpleado, LocalTime horaSalida) {
        LocalDate hoy = LocalDate.now();
        if (horaSalida == null) horaSalida = LocalTime.now();

        Optional<Fichaje> optF = ctrl.getFichajeAbiertoHoy(idEmpleado, hoy);
        if (optF.isEmpty())
            return ResultadoFichaje.error("No hay fichaje de entrada abierto para hoy.");

        Fichaje f = optF.get();
        f.setHoraSalida(horaSalida);

        long minTrabajados = ChronoUnit.MINUTES.between(f.getHoraEntrada(), horaSalida);
        f.setHorasTrabajadas(minutosAHoras(minTrabajados));

        // Calcular horas extra respecto al turno
        if (f.getTurno() != null && f.getTurno().getHorasTrabajadas() != null) {
            BigDecimal extra = f.getHorasTrabajadas().subtract(f.getTurno().getHorasTrabajadas());
            f.setHorasExtra(extra.compareTo(BigDecimal.ZERO) > 0 ? extra : BigDecimal.ZERO);
            // Marcar turno como completado
            f.getTurno().setEstado(Turno.EstadoTurno.completado);
            ctrl.actualizarTurno(f.getTurno());
        }

        return ctrl.actualizarFichaje(f)
            ? new ResultadoFichaje(true,
                "Salida registrada. Horas trabajadas: " + f.getHorasTrabajadas() + "h", f, false)
            : ResultadoFichaje.error("Error al registrar la salida.");
    }

    /**
     * Corrige manualmente un fichaje desde la UI de escritorio
     * (p.ej. cuando la app no pudo conectar al WiFi).
     */
    public Resultado corregirFichaje(int idFichaje, LocalTime entrada,
                                     LocalTime salida, String observaciones) {
        Optional<Fichaje> opt = ctrl.getFichajePorId(idFichaje);
        if (opt.isEmpty()) return Resultado.error("Fichaje no encontrado (id=" + idFichaje + ").");
        Fichaje f = opt.get();
        if (entrada != null) f.setHoraEntrada(entrada);
        if (salida  != null) {
            f.setHoraSalida(salida);
            if (f.getHoraEntrada() != null) {
                long min = ChronoUnit.MINUTES.between(f.getHoraEntrada(), salida);
                f.setHorasTrabajadas(minutosAHoras(min));
                if (f.getTurno() != null && f.getTurno().getHorasTrabajadas() != null) {
                    BigDecimal extra = f.getHorasTrabajadas().subtract(f.getTurno().getHorasTrabajadas());
                    f.setHorasExtra(extra.compareTo(BigDecimal.ZERO) > 0 ? extra : BigDecimal.ZERO);
                }
            }
        }
        if (observaciones != null) f.setObservaciones(observaciones);
        return ctrl.actualizarFichaje(f)
            ? Resultado.ok("Fichaje corregido.")
            : Resultado.error("Error al actualizar el fichaje.");
    }

    // ══════════════════════════════════════════════════════════════════
    // ALERTAS Y AUSENCIAS NO JUSTIFICADAS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Devuelve los turnos del día que no tienen fichaje de entrada.
     * → Empleados que no han aparecido sin justificación.
     * Llamar ~30 min después de la hora de apertura del establecimiento.
     */
    public List<Turno> detectarAusenciasNoJustificadas(LocalDate fecha) {
        return ctrl.getTurnosSinFichaje(fecha);
    }

    /** Fichajes con retraso ≥ umbral en un rango de fechas (para el panel de incidencias). */
    public List<Fichaje> getIncidenciasRetraso(LocalDate desde, LocalDate hasta) {
        return ctrl.getFichajesConRetrasoEnRango(desde, hasta, UMBRAL_INCIDENCIA_MIN);
    }

    /**
     * Cierra automáticamente los fichajes que quedaron abiertos al final del día.
     * Tambien puede usarse desde un boton en la UI.
     *
     * @param fecha  normalmente LocalDate.now().minusDays(1) (día anterior)
     * @return número de fichajes cerrados
     */
    public int cerrarFichajesAbiertos(LocalDate fecha) {
        List<Fichaje> abiertos = ctrl.getFichajesAbiertos(fecha);
        int cerrados = 0;
        for (Fichaje f : abiertos) {
            LocalTime cierre = (f.getTurno() != null && f.getTurno().getHoraFin() != null)
                ? f.getTurno().getHoraFin() : LocalTime.of(23, 59);
            f.setHoraSalida(cierre);
            f.setHorasTrabajadas(minutosAHoras(ChronoUnit.MINUTES.between(f.getHoraEntrada(), cierre)));
            f.setHorasExtra(BigDecimal.ZERO);
            String obs = (f.getObservaciones() != null ? f.getObservaciones() + " | " : "")
                + "⚠️ Cierre automático — sin fichaje de salida.";
            f.setObservaciones(obs);
            if (ctrl.actualizarFichaje(f)) cerrados++;
        }
        return cerrados;
    }

    // ══════════════════════════════════════════════════════════════════
    // RESUMEN DE HORAS (para la UI)
    // ══════════════════════════════════════════════════════════════════

    public ResumenHoras getResumenHoras(int idEmpleado, LocalDate desde, LocalDate hasta) {
        List<Fichaje> fs = ctrl.getFichajesPorEmpleadoYRango(idEmpleado, desde, hasta);
        double total    = fs.stream().filter(f -> f.getHorasTrabajadas() != null)
                            .mapToDouble(f -> f.getHorasTrabajadas().doubleValue()).sum();
        double extra    = fs.stream().filter(f -> f.getHorasExtra()      != null)
                            .mapToDouble(f -> f.getHorasExtra().doubleValue()).sum();
        int retrasoMin  = fs.stream().filter(f -> f.getRetrasoMinutos()  != null)
                            .mapToInt(Fichaje::getRetrasoMinutos).sum();
        long conRetraso = fs.stream().filter(f -> f.getRetrasoMinutos()  != null
                            && f.getRetrasoMinutos() >= UMBRAL_INCIDENCIA_MIN).count();
        return new ResumenHoras(idEmpleado, desde, hasta, total, extra,
                                retrasoMin, (int) conRetraso, fs.size());
    }

    // ══════════════════════════════════════════════════════════════════
    // HELPER PRIVADO
    // ══════════════════════════════════════════════════════════════════

    private static BigDecimal minutosAHoras(long minutos) {
        return BigDecimal.valueOf(minutos)
                         .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    // ══════════════════════════════════════════════════════════════════
    // DTOs DE RESULTADO
    // ══════════════════════════════════════════════════════════════════

    public static final class Resultado {
        public final boolean ok;
        public final String  mensaje;
        private Resultado(boolean ok, String msg) { this.ok = ok; this.mensaje = msg; }
        public static Resultado ok(String msg)    { return new Resultado(true,  msg); }
        public static Resultado error(String msg) { return new Resultado(false, msg); }
    }

    public static final class ResultadoFichaje {
        public final boolean ok;
        public final String  mensaje;
        public final Fichaje fichaje;
        public final boolean hayRetraso;
        public ResultadoFichaje(boolean ok, String msg, Fichaje f, boolean ret) {
            this.ok = ok; this.mensaje = msg; this.fichaje = f; this.hayRetraso = ret;
        }
        public static ResultadoFichaje error(String msg) {
            return new ResultadoFichaje(false, msg, null, false);
        }
    }

    public static final class ResumenHoras {
        public final int       idEmpleado;
        public final LocalDate desde, hasta;
        public final double    totalHoras, horasExtra;
        public final int       retrasoTotalMin, fichajesConRetraso, totalFichajes;
        public ResumenHoras(int id, LocalDate d, LocalDate h,
                            double t, double e, int rm, int cr, int tf) {
            idEmpleado = id; desde = d; hasta = h;
            totalHoras = t; horasExtra = e;
            retrasoTotalMin = rm; fichajesConRetraso = cr; totalFichajes = tf;
        }
    }
}
