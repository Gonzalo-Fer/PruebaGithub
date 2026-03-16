package com.hosteleria.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "evaluaciones")
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion")
    private Integer idEvaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluador", nullable = false)
    private Empleado evaluador;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "periodo", length = 50)
    private String periodo;

    @Column(name = "puntualidad")
    private Integer puntualidad;

    @Column(name = "atencion_cliente")
    private Integer atencionCliente;

    @Column(name = "trabajo_equipo")
    private Integer trabajoEquipo;

    @Column(name = "conocimiento_producto")
    private Integer conocimientoProducto;

    @Column(name = "higiene_presentacion")
    private Integer higienePresentacion;

    @Column(name = "puntuacion_total")
    private Integer puntuacionTotal;

    @Column(name = "comentarios", columnDefinition = "TEXT")
    private String comentarios;

    public Evaluacion() {}

    public Integer getIdEvaluacion() { return idEvaluacion; }
    public void setIdEvaluacion(Integer idEvaluacion) { this.idEvaluacion = idEvaluacion; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public Empleado getEvaluador() { return evaluador; }
    public void setEvaluador(Empleado evaluador) { this.evaluador = evaluador; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public Integer getPuntualidad() { return puntualidad; }
    public void setPuntualidad(Integer puntualidad) { this.puntualidad = puntualidad; }

    public Integer getAtencionCliente() { return atencionCliente; }
    public void setAtencionCliente(Integer atencionCliente) { this.atencionCliente = atencionCliente; }

    public Integer getTrabajoEquipo() { return trabajoEquipo; }
    public void setTrabajoEquipo(Integer trabajoEquipo) { this.trabajoEquipo = trabajoEquipo; }

    public Integer getConocimientoProducto() { return conocimientoProducto; }
    public void setConocimientoProducto(Integer conocimientoProducto) { this.conocimientoProducto = conocimientoProducto; }

    public Integer getHigienePresentacion() { return higienePresentacion; }
    public void setHigienePresentacion(Integer higienePresentacion) { this.higienePresentacion = higienePresentacion; }

    public Integer getPuntuacionTotal() { return puntuacionTotal; }
    public void setPuntuacionTotal(Integer puntuacionTotal) { this.puntuacionTotal = puntuacionTotal; }

    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }
}
