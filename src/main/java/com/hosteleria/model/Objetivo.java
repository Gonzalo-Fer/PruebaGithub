package com.hosteleria.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Objetivo de desempeño asignado a un empleado.
 * Puede estar vinculado a una evaluación concreta o ser de seguimiento continuo.
 */
@Entity
@Table(name = "objetivos")
public class Objetivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_objetivo")
    private Integer idObjetivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    /** Responsable que fijó el objetivo (normalmente el evaluador / manager) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable")
    private Empleado responsable;

    /** Evaluación en la que se revisó / cerró este objetivo (opcional) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion")
    private Evaluacion evaluacion;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    /** Progreso de 0 a 100 (%) */
    @Column(name = "progreso")
    private Integer progreso = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoObjetivo estado = EstadoObjetivo.pendiente;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    public Objetivo() {}

    // ── Getters / Setters ─────────────────────────────────────────────

    public Integer getIdObjetivo()                        { return idObjetivo; }
    public void    setIdObjetivo(Integer idObjetivo)      { this.idObjetivo = idObjetivo; }

    public Empleado getEmpleado()                         { return empleado; }
    public void     setEmpleado(Empleado empleado)        { this.empleado = empleado; }

    public Empleado getResponsable()                      { return responsable; }
    public void     setResponsable(Empleado responsable)  { this.responsable = responsable; }

    public Evaluacion getEvaluacion()                     { return evaluacion; }
    public void       setEvaluacion(Evaluacion evaluacion){ this.evaluacion = evaluacion; }

    public String getTitulo()                             { return titulo; }
    public void   setTitulo(String titulo)                { this.titulo = titulo; }

    public String getDescripcion()                        { return descripcion; }
    public void   setDescripcion(String descripcion)      { this.descripcion = descripcion; }

    public LocalDate getFechaCreacion()                           { return fechaCreacion; }
    public void      setFechaCreacion(LocalDate fechaCreacion)    { this.fechaCreacion = fechaCreacion; }

    public LocalDate getFechaLimite()                             { return fechaLimite; }
    public void      setFechaLimite(LocalDate fechaLimite)        { this.fechaLimite = fechaLimite; }

    public Integer getProgreso()                          { return progreso; }
    public void    setProgreso(Integer progreso)          { this.progreso = progreso; }

    public EstadoObjetivo getEstado()                     { return estado; }
    public void           setEstado(EstadoObjetivo estado){ this.estado = estado; }

    public String getObservaciones()                      { return observaciones; }
    public void   setObservaciones(String observaciones)  { this.observaciones = observaciones; }

    @Override
    public String toString() { return titulo != null ? titulo : "Objetivo #" + idObjetivo; }

    // ── Enums ─────────────────────────────────────────────────────────

    public enum EstadoObjetivo { pendiente, en_progreso, completado, cancelado }
}
