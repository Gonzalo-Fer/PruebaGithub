package com.hosteleria.service;

import com.hosteleria.controller.EvaluacionController;
import com.hosteleria.model.Empleado;
import com.hosteleria.model.Evaluacion;
import com.hosteleria.model.Objetivo;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lógica de negocio para evaluaciones de desempeño y objetivos.
 *
 * Responsabilidades:
 *   - Calcular y persistir la puntuación total ponderada.
 *   - Generar el DTO de resumen de rendimiento por empleado.
 *   - Generar el DTO de informe de equipo para un periodo.
 *   - Actualizar el progreso / estado de objetivos.
 */
public class EvaluacionService {

    private final EvaluacionController ctrl = new EvaluacionController();

    // ── Pesos de los criterios (suman 100) ────────────────────────────
    private static final int PESO_PUNTUALIDAD           = 20;
    private static final int PESO_ATENCION_CLIENTE      = 25;
    private static final int PESO_TRABAJO_EQUIPO        = 20;
    private static final int PESO_CONOCIMIENTO_PRODUCTO = 20;
    private static final int PESO_HIGIENE_PRESENTACION  = 15;

    // ══════════════════════════════════════════════════════════════════
    // EVALUACIONES
    // ══════════════════════════════════════════════════════════════════

    /**
     * Calcula la puntuación total ponderada (0–100) y la asigna al objeto.
     * Los criterios se puntúan de 1 a 10; el resultado se escala a 0–100.
     *
     * @return puntuación calculada (mismo valor asignado al objeto)
     */
    public int calcularPuntuacion(Evaluacion ev) {
        int suma =
            valorado(ev.getPuntualidad(),          PESO_PUNTUALIDAD)
          + valorado(ev.getAtencionCliente(),       PESO_ATENCION_CLIENTE)
          + valorado(ev.getTrabajoEquipo(),         PESO_TRABAJO_EQUIPO)
          + valorado(ev.getConocimientoProducto(),  PESO_CONOCIMIENTO_PRODUCTO)
          + valorado(ev.getHigienePresentacion(),   PESO_HIGIENE_PRESENTACION);
        ev.setPuntuacionTotal(suma);
        return suma;
    }

    /** Guarda una evaluación nueva calculando su puntuación primero */
    public ResultadoEvaluacion guardar(Evaluacion ev) {
        calcularPuntuacion(ev);
        boolean ok = ctrl.guardarEvaluacion(ev);
        return ok ? ResultadoEvaluacion.ok("Evaluación guardada correctamente.")
                  : ResultadoEvaluacion.error("No se pudo guardar la evaluación.");
    }

    /** Actualiza una evaluación existente recalculando su puntuación */
    public ResultadoEvaluacion actualizar(Evaluacion ev) {
        calcularPuntuacion(ev);
        boolean ok = ctrl.actualizarEvaluacion(ev);
        return ok ? ResultadoEvaluacion.ok("Evaluación actualizada.")
                  : ResultadoEvaluacion.error("No se pudo actualizar la evaluación.");
    }

    // ══════════════════════════════════════════════════════════════════
    // OBJETIVOS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Guarda un nuevo objetivo para un empleado.
     * La fecha de creación se rellena automáticamente si está vacía.
     */
    public ResultadoEvaluacion guardarObjetivo(Objetivo obj) {
        if (obj.getFechaCreacion() == null) obj.setFechaCreacion(LocalDate.now());
        boolean ok = ctrl.guardarObjetivo(obj);
        return ok ? ResultadoEvaluacion.ok("Objetivo creado.")
                  : ResultadoEvaluacion.error("No se pudo crear el objetivo.");
    }

    /**
     * Actualiza el progreso de un objetivo.
     * Si progreso == 100 cambia el estado a completado automáticamente.
     */
    public ResultadoEvaluacion actualizarProgreso(Objetivo obj, int nuevoProgreso) {
        obj.setProgreso(Math.min(100, Math.max(0, nuevoProgreso)));
        if (obj.getProgreso() == 100 && obj.getEstado() == Objetivo.EstadoObjetivo.en_progreso) {
            obj.setEstado(Objetivo.EstadoObjetivo.completado);
        } else if (obj.getProgreso() > 0 && obj.getEstado() == Objetivo.EstadoObjetivo.pendiente) {
            obj.setEstado(Objetivo.EstadoObjetivo.en_progreso);
        }
        boolean ok = ctrl.actualizarObjetivo(obj);
        return ok ? ResultadoEvaluacion.ok("Progreso actualizado.")
                  : ResultadoEvaluacion.error("No se pudo actualizar el progreso.");
    }

    // INFORMES

    /**
     * Resumen de rendimiento de un empleado concreto en un periodo.
     */
    public ResumenEmpleadoDTO resumenEmpleado(int idEmpleado, LocalDate desde, LocalDate hasta) {
        List<Evaluacion> evals = ctrl.getEvaluacionesPorEmpleado(idEmpleado).stream()
            .filter(e -> e.getEstado() != Evaluacion.EstadoEvaluacion.borrador)
            .filter(e -> e.getFecha() != null
                      && !e.getFecha().isBefore(desde)
                      && !e.getFecha().isAfter(hasta))
            .toList();

        List<Objetivo> objs = ctrl.getObjetivosPorEmpleado(idEmpleado);

        ResumenEmpleadoDTO dto = new ResumenEmpleadoDTO();
        dto.setIdEmpleado(idEmpleado);
        dto.setDesde(desde);
        dto.setHasta(hasta);
        dto.setNumEvaluaciones(evals.size());

        if (!evals.isEmpty()) {
            dto.setMediaGeneral(media(evals, Evaluacion::getPuntuacionTotal));
            dto.setMediaPuntualidad(media(evals, Evaluacion::getPuntualidad));
            dto.setMediaAtencionCliente(media(evals, Evaluacion::getAtencionCliente));
            dto.setMediaTrabajoEquipo(media(evals, Evaluacion::getTrabajoEquipo));
            dto.setMediaConocimientoProducto(media(evals, Evaluacion::getConocimientoProducto));
            dto.setMediaHigienePresentacion(media(evals, Evaluacion::getHigienePresentacion));
        }

        long completados = objs.stream()
            .filter(o -> o.getEstado() == Objetivo.EstadoObjetivo.completado).count();
        dto.setObjetivosTotales(objs.size());
        dto.setObjetivosCompletados((int) completados);
        dto.setPorcentajeObjetivos(objs.isEmpty() ? 0 : (completados * 100.0 / objs.size()));
        dto.setEvaluaciones(evals);
        dto.setObjetivos(objs);
        return dto;
    }

    /**
     * Informe de rendimiento del equipo completo en un periodo.
     * Devuelve un ResumenEmpleadoDTO por cada empleado con al menos una evaluación,
     * ordenados de mayor a menor puntuación media.
     */
    public InformeEquipoDTO informeEquipo(LocalDate desde, LocalDate hasta) {
        List<Evaluacion> todasEvals = ctrl.getEvaluacionesCompletadasPorPeriodo(desde, hasta);

        // Agrupar por empleado
        Map<Integer, List<Evaluacion>> porEmpleado = todasEvals.stream()
            .collect(Collectors.groupingBy(e -> e.getEmpleado().getIdEmpleado()));

        List<ResumenEmpleadoDTO> filas = new ArrayList<>();
        for (Map.Entry<Integer, List<Evaluacion>> entry : porEmpleado.entrySet()) {
            int idEmp = entry.getKey();
            List<Evaluacion> evals = entry.getValue();
            Empleado emp = evals.get(0).getEmpleado();

            List<Objetivo> objs = ctrl.getObjetivosPorEmpleado(idEmp);
            long completados = objs.stream()
                .filter(o -> o.getEstado() == Objetivo.EstadoObjetivo.completado).count();

            ResumenEmpleadoDTO dto = new ResumenEmpleadoDTO();
            dto.setIdEmpleado(idEmp);
            dto.setNombreEmpleado(emp.getNombre() + " " + emp.getApellidos());
            dto.setDesde(desde);
            dto.setHasta(hasta);
            dto.setNumEvaluaciones(evals.size());
            dto.setMediaGeneral(media(evals, Evaluacion::getPuntuacionTotal));
            dto.setMediaPuntualidad(media(evals, Evaluacion::getPuntualidad));
            dto.setMediaAtencionCliente(media(evals, Evaluacion::getAtencionCliente));
            dto.setMediaTrabajoEquipo(media(evals, Evaluacion::getTrabajoEquipo));
            dto.setMediaConocimientoProducto(media(evals, Evaluacion::getConocimientoProducto));
            dto.setMediaHigienePresentacion(media(evals, Evaluacion::getHigienePresentacion));
            dto.setObjetivosTotales(objs.size());
            dto.setObjetivosCompletados((int) completados);
            dto.setPorcentajeObjetivos(objs.isEmpty() ? 0 : (completados * 100.0 / objs.size()));
            dto.setEvaluaciones(evals);
            dto.setObjetivos(objs);
            filas.add(dto);
        }

        filas.sort(Comparator.comparingDouble(ResumenEmpleadoDTO::getMediaGeneral).reversed());

        InformeEquipoDTO informe = new InformeEquipoDTO();
        informe.setDesde(desde);
        informe.setHasta(hasta);
        informe.setFilas(filas);
        if (!filas.isEmpty()) {
            informe.setMediaGlobalEquipo(
                filas.stream().mapToDouble(ResumenEmpleadoDTO::getMediaGeneral).average().orElse(0)
            );
        }
        return informe;
    }

    // ── Helpers internos ──────────────────────────────────────────────

    private int valorado(Integer criterio, int peso) {
        if (criterio == null || criterio <= 0) return 0;
        // criterio 1-10 → escalar a 0-100 proporcional al peso
        return (int) Math.round((criterio / 10.0) * peso);
    }

    @FunctionalInterface
    private interface EvalGetter { Integer get(Evaluacion e); }

    private double media(List<Evaluacion> evals, EvalGetter getter) {
        return evals.stream()
            .map(getter::get)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
    }

    // DTOs

    public static class ResultadoEvaluacion {
        private final boolean exito;
        private final String  mensaje;

        private ResultadoEvaluacion(boolean exito, String mensaje) {
            this.exito   = exito;
            this.mensaje = mensaje;
        }

        public static ResultadoEvaluacion ok(String msg)    { return new ResultadoEvaluacion(true,  msg); }
        public static ResultadoEvaluacion error(String msg) { return new ResultadoEvaluacion(false, msg); }

        public boolean isExito()   { return exito; }
        public String  getMensaje(){ return mensaje; }
    }

    public static class ResumenEmpleadoDTO {
        private int idEmpleado;
        private String nombreEmpleado;
        private LocalDate desde;
        private LocalDate hasta;
        private int numEvaluaciones;
        private double mediaGeneral;
        private double mediaPuntualidad;
        private double mediaAtencionCliente;
        private double mediaTrabajoEquipo;
        private double mediaConocimientoProducto;
        private double mediaHigienePresentacion;
        private int objetivosTotales;
        private int objetivosCompletados;
        private double porcentajeObjetivos;
        private List<Evaluacion> evaluaciones = new ArrayList<>();
        private List<Objetivo>   objetivos    = new ArrayList<>();

        public int     getIdEmpleado()                          { return idEmpleado; }
        public void    setIdEmpleado(int v)                     { this.idEmpleado = v; }
        public String  getNombreEmpleado()                      { return nombreEmpleado; }
        public void    setNombreEmpleado(String v)              { this.nombreEmpleado = v; }
        public LocalDate getDesde()                             { return desde; }
        public void    setDesde(LocalDate v)                    { this.desde = v; }
        public LocalDate getHasta()                             { return hasta; }
        public void    setHasta(LocalDate v)                    { this.hasta = v; }
        public int     getNumEvaluaciones()                     { return numEvaluaciones; }
        public void    setNumEvaluaciones(int v)                { this.numEvaluaciones = v; }
        public double  getMediaGeneral()                        { return mediaGeneral; }
        public void    setMediaGeneral(double v)                { this.mediaGeneral = v; }
        public double  getMediaPuntualidad()                    { return mediaPuntualidad; }
        public void    setMediaPuntualidad(double v)            { this.mediaPuntualidad = v; }
        public double  getMediaAtencionCliente()                { return mediaAtencionCliente; }
        public void    setMediaAtencionCliente(double v)        { this.mediaAtencionCliente = v; }
        public double  getMediaTrabajoEquipo()                  { return mediaTrabajoEquipo; }
        public void    setMediaTrabajoEquipo(double v)          { this.mediaTrabajoEquipo = v; }
        public double  getMediaConocimientoProducto()           { return mediaConocimientoProducto; }
        public void    setMediaConocimientoProducto(double v)   { this.mediaConocimientoProducto = v; }
        public double  getMediaHigienePresentacion()            { return mediaHigienePresentacion; }
        public void    setMediaHigienePresentacion(double v)    { this.mediaHigienePresentacion = v; }
        public int     getObjetivosTotales()                    { return objetivosTotales; }
        public void    setObjetivosTotales(int v)               { this.objetivosTotales = v; }
        public int     getObjetivosCompletados()                { return objetivosCompletados; }
        public void    setObjetivosCompletados(int v)           { this.objetivosCompletados = v; }
        public double  getPorcentajeObjetivos()                 { return porcentajeObjetivos; }
        public void    setPorcentajeObjetivos(double v)         { this.porcentajeObjetivos = v; }
        public List<Evaluacion> getEvaluaciones()               { return evaluaciones; }
        public void    setEvaluaciones(List<Evaluacion> v)      { this.evaluaciones = v; }
        public List<Objetivo>   getObjetivos()                  { return objetivos; }
        public void    setObjetivos(List<Objetivo> v)           { this.objetivos = v; }
    }

    public static class InformeEquipoDTO {
        private LocalDate desde;
        private LocalDate hasta;
        private double mediaGlobalEquipo;
        private List<ResumenEmpleadoDTO> filas = new ArrayList<>();

        public LocalDate getDesde()                             { return desde; }
        public void      setDesde(LocalDate v)                  { this.desde = v; }
        public LocalDate getHasta()                             { return hasta; }
        public void      setHasta(LocalDate v)                  { this.hasta = v; }
        public double    getMediaGlobalEquipo()                 { return mediaGlobalEquipo; }
        public void      setMediaGlobalEquipo(double v)         { this.mediaGlobalEquipo = v; }
        public List<ResumenEmpleadoDTO> getFilas()              { return filas; }
        public void      setFilas(List<ResumenEmpleadoDTO> v)   { this.filas = v; }
    }
}
