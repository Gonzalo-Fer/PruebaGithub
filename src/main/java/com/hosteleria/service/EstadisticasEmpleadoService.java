package com.hosteleria.service;

import com.hosteleria.controller.HosteleriaController;
import com.hosteleria.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de estadísticas de empleados: horas, ausencias, evaluaciones, propinas, etc.
 */
public class EstadisticasEmpleadoService {

    private final HosteleriaController ctrl = new HosteleriaController();

    /**
     * Estadísticas detalladas de un empleado.
     */
    public EstadisticasEmpleadoDTO estadisticasEmpleado(int idEmpleado, LocalDate desde, LocalDate hasta) {
        Optional<Empleado> opt = ctrl.getEmpleadoCompleto(idEmpleado);
        if (opt.isEmpty()) return null;
        Empleado e = opt.get();

        List<Fichaje> fichajes = ctrl.getFichajesPorEmpleadoYRango(idEmpleado, desde, hasta);
        List<Ausencia> ausencias = ctrl.getAusenciasPorEmpleado(idEmpleado).stream()
            .filter(a -> a.getFechaInicio() != null && a.getFechaFin() != null)
            .filter(a -> !a.getFechaFin().isBefore(desde) && !a.getFechaInicio().isAfter(hasta))
            .toList();
        List<Evaluacion> evaluaciones = ctrl.getEvaluacionesPorEmpleado(idEmpleado).stream()
            .filter(ev -> ev.getFecha() != null && !ev.getFecha().isBefore(desde) && !ev.getFecha().isAfter(hasta))
            .toList();
        List<Nomina> nominas = ctrl.getNominasPorEmpleado(idEmpleado).stream()
            .filter(n -> enRangoMesAnio(n.getAnio(), n.getMes(), desde, hasta))
            .toList();
        List<Propina> propinas = new ArrayList<>();
        for (int anio = desde.getYear(); anio <= hasta.getYear(); anio++) {
            for (int mes = 1; mes <= 12; mes++) {
                if (anio == desde.getYear() && mes < desde.getMonthValue()) continue;
                if (anio == hasta.getYear() && mes > hasta.getMonthValue()) break;
                propinas.addAll(ctrl.getPropinasDeEmpleadoEnMes(idEmpleado, mes, anio));
            }
        }

        double totalHoras = fichajes.stream()
            .map(Fichaje::getHorasTrabajadas)
            .filter(Objects::nonNull)
            .mapToDouble(BigDecimal::doubleValue)
            .sum();
        double totalHorasExtra = fichajes.stream()
            .map(Fichaje::getHorasExtra)
            .filter(Objects::nonNull)
            .mapToDouble(BigDecimal::doubleValue)
            .sum();
        int totalRetrasosMin = fichajes.stream()
            .map(Fichaje::getRetrasoMinutos)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();
        int diasAusencia = ausencias.stream()
            .mapToInt(a -> a.getDiasTotales() != null ? a.getDiasTotales() : 0)
            .sum();
        double mediaEvaluacion = evaluaciones.isEmpty() ? 0 :
            evaluaciones.stream()
                .mapToInt(ev -> ev.getPuntuacionTotal() != null ? ev.getPuntuacionTotal() : 0)
                .average().orElse(0);
        BigDecimal totalPropinas = propinas.stream()
            .map(Propina::getImporte)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNominas = nominas.stream()
            .map(Nomina::getTotalNeto)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        EstadisticasEmpleadoDTO dto = new EstadisticasEmpleadoDTO();
        dto.setIdEmpleado(idEmpleado);
        dto.setNombreCompleto(e.getNombre() + " " + e.getApellidos());
        dto.setDesde(desde);
        dto.setHasta(hasta);
        dto.setTotalHorasTrabajadas(totalHoras);
        dto.setTotalHorasExtra(totalHorasExtra);
        dto.setNumeroFichajes(fichajes.size());
        dto.setRetrasoTotalMinutos(totalRetrasosMin);
        dto.setDiasAusencia(diasAusencia);
        dto.setNumeroEvaluaciones(evaluaciones.size());
        dto.setPuntuacionMedia(mediaEvaluacion);
        dto.setTotalPropinas(totalPropinas);
        dto.setTotalNominas(totalNominas);
        dto.setNumeroNominas(nominas.size());
        return dto;
    }

    /**
     * Resumen global: plantilla por área, por estado, totales.
     */
    public EstadisticasGlobalesDTO estadisticasGlobales() {
        List<Empleado> todos = ctrl.getEmpleadosConAreaYPuesto();
        long activos = todos.stream().filter(e -> e.getEstado() == Empleado.EstadoEmpleado.activo).count();
        long bajas = todos.stream().filter(e -> e.getEstado() == Empleado.EstadoEmpleado.baja_definitiva).count();
        long vacaciones = todos.stream().filter(e -> e.getEstado() == Empleado.EstadoEmpleado.vacaciones).count();
        long bajaTemporal = todos.stream().filter(e -> e.getEstado() == Empleado.EstadoEmpleado.baja_temporal).count();

        Map<String, Long> porArea = todos.stream()
            .filter(e -> e.getArea() != null)
            .collect(Collectors.groupingBy(a -> a.getArea().getNombre(), Collectors.counting()));
        Map<String, Long> porPuesto = todos.stream()
            .filter(e -> e.getPuesto() != null)
            .collect(Collectors.groupingBy(p -> p.getPuesto().getTitulo(), Collectors.counting()));
        Map<String, Long> porContrato = todos.stream()
            .filter(e -> e.getTipoContrato() != null)
            .collect(Collectors.groupingBy(e -> e.getTipoContrato().name(), Collectors.counting()));

        EstadisticasGlobalesDTO dto = new EstadisticasGlobalesDTO();
        dto.setTotalEmpleados(todos.size());
        dto.setActivos((int) activos);
        dto.setBajaDefinitiva((int) bajas);
        dto.setVacaciones((int) vacaciones);
        dto.setBajaTemporal((int) bajaTemporal);
        dto.setEmpleadosPorArea(porArea);
        dto.setEmpleadosPorPuesto(porPuesto);
        dto.setEmpleadosPorTipoContrato(porContrato);
        return dto;
    }

    private static boolean enRangoMesAnio(int anio, int mes, LocalDate desde, LocalDate hasta) {
        if (anio < desde.getYear() || anio > hasta.getYear()) return false;
        if (anio == desde.getYear() && mes < desde.getMonthValue()) return false;
        if (anio == hasta.getYear() && mes > hasta.getMonthValue()) return false;
        return true;
    }

    // ── DTOs ──────────────────────────────────────────────────────────

    public static class EstadisticasEmpleadoDTO {
        private int idEmpleado;
        private String nombreCompleto;
        private LocalDate desde;
        private LocalDate hasta;
        private double totalHorasTrabajadas;
        private double totalHorasExtra;
        private int numeroFichajes;
        private int retrasoTotalMinutos;
        private int diasAusencia;
        private int numeroEvaluaciones;
        private double puntuacionMedia;
        private BigDecimal totalPropinas;
        private BigDecimal totalNominas;
        private int numeroNominas;

        public int getIdEmpleado() { return idEmpleado; }
        public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }
        public String getNombreCompleto() { return nombreCompleto; }
        public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
        public LocalDate getDesde() { return desde; }
        public void setDesde(LocalDate desde) { this.desde = desde; }
        public LocalDate getHasta() { return hasta; }
        public void setHasta(LocalDate hasta) { this.hasta = hasta; }
        public double getTotalHorasTrabajadas() { return totalHorasTrabajadas; }
        public void setTotalHorasTrabajadas(double totalHorasTrabajadas) { this.totalHorasTrabajadas = totalHorasTrabajadas; }
        public double getTotalHorasExtra() { return totalHorasExtra; }
        public void setTotalHorasExtra(double totalHorasExtra) { this.totalHorasExtra = totalHorasExtra; }
        public int getNumeroFichajes() { return numeroFichajes; }
        public void setNumeroFichajes(int numeroFichajes) { this.numeroFichajes = numeroFichajes; }
        public int getRetrasoTotalMinutos() { return retrasoTotalMinutos; }
        public void setRetrasoTotalMinutos(int retrasoTotalMinutos) { this.retrasoTotalMinutos = retrasoTotalMinutos; }
        public int getDiasAusencia() { return diasAusencia; }
        public void setDiasAusencia(int diasAusencia) { this.diasAusencia = diasAusencia; }
        public int getNumeroEvaluaciones() { return numeroEvaluaciones; }
        public void setNumeroEvaluaciones(int numeroEvaluaciones) { this.numeroEvaluaciones = numeroEvaluaciones; }
        public double getPuntuacionMedia() { return puntuacionMedia; }
        public void setPuntuacionMedia(double puntuacionMedia) { this.puntuacionMedia = puntuacionMedia; }
        public BigDecimal getTotalPropinas() { return totalPropinas; }
        public void setTotalPropinas(BigDecimal totalPropinas) { this.totalPropinas = totalPropinas; }
        public BigDecimal getTotalNominas() { return totalNominas; }
        public void setTotalNominas(BigDecimal totalNominas) { this.totalNominas = totalNominas; }
        public int getNumeroNominas() { return numeroNominas; }
        public void setNumeroNominas(int numeroNominas) { this.numeroNominas = numeroNominas; }
    }

    public static class EstadisticasGlobalesDTO {
        private int totalEmpleados;
        private int activos;
        private int bajaDefinitiva;
        private int vacaciones;
        private int bajaTemporal;
        private Map<String, Long> empleadosPorArea = new HashMap<>();
        private Map<String, Long> empleadosPorPuesto = new HashMap<>();
        private Map<String, Long> empleadosPorTipoContrato = new HashMap<>();

        public int getTotalEmpleados() { return totalEmpleados; }
        public void setTotalEmpleados(int totalEmpleados) { this.totalEmpleados = totalEmpleados; }
        public int getActivos() { return activos; }
        public void setActivos(int activos) { this.activos = activos; }
        public int getBajaDefinitiva() { return bajaDefinitiva; }
        public void setBajaDefinitiva(int bajaDefinitiva) { this.bajaDefinitiva = bajaDefinitiva; }
        public int getVacaciones() { return vacaciones; }
        public void setVacaciones(int vacaciones) { this.vacaciones = vacaciones; }
        public int getBajaTemporal() { return bajaTemporal; }
        public void setBajaTemporal(int bajaTemporal) { this.bajaTemporal = bajaTemporal; }
        public Map<String, Long> getEmpleadosPorArea() { return empleadosPorArea; }
        public void setEmpleadosPorArea(Map<String, Long> empleadosPorArea) { this.empleadosPorArea = empleadosPorArea; }
        public Map<String, Long> getEmpleadosPorPuesto() { return empleadosPorPuesto; }
        public void setEmpleadosPorPuesto(Map<String, Long> empleadosPorPuesto) { this.empleadosPorPuesto = empleadosPorPuesto; }
        public Map<String, Long> getEmpleadosPorTipoContrato() { return empleadosPorTipoContrato; }
        public void setEmpleadosPorTipoContrato(Map<String, Long> empleadosPorTipoContrato) { this.empleadosPorTipoContrato = empleadosPorTipoContrato; }
    }
}
