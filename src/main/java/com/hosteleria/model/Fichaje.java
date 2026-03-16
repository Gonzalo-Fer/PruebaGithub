package com.hosteleria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "fichajes")
public class Fichaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fichaje")
    private Integer idFichaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_turno")
    private Turno turno;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @Column(name = "horas_trabajadas", precision = 4, scale = 2)
    private BigDecimal horasTrabajadas;

    @Column(name = "horas_extra", precision = 4, scale = 2)
    private BigDecimal horasExtra = BigDecimal.ZERO;

    @Column(name = "retraso_minutos")
    private Integer retrasoMinutos = 0;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    public Fichaje() {}

    public Integer getIdFichaje() { return idFichaje; }
    public void setIdFichaje(Integer idFichaje) { this.idFichaje = idFichaje; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(LocalTime horaEntrada) { this.horaEntrada = horaEntrada; }

    public LocalTime getHoraSalida() { return horaSalida; }
    public void setHoraSalida(LocalTime horaSalida) { this.horaSalida = horaSalida; }

    public BigDecimal getHorasTrabajadas() { return horasTrabajadas; }
    public void setHorasTrabajadas(BigDecimal horasTrabajadas) { this.horasTrabajadas = horasTrabajadas; }

    public BigDecimal getHorasExtra() { return horasExtra; }
    public void setHorasExtra(BigDecimal horasExtra) { this.horasExtra = horasExtra; }

    public Integer getRetrasoMinutos() { return retrasoMinutos; }
    public void setRetrasoMinutos(Integer retrasoMinutos) { this.retrasoMinutos = retrasoMinutos; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
