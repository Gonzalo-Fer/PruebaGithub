package com.hosteleria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "turnos")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turno")
    private Integer idTurno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_turno", nullable = false)
    private TipoTurno tipoTurno;

    @Column(name = "area_asignada", length = 100)
    private String areaAsignada;

    @Column(name = "horas_trabajadas", precision = 4, scale = 2)
    private BigDecimal horasTrabajadas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoTurno estado = EstadoTurno.programado;

    @OneToMany(mappedBy = "turno", fetch = FetchType.LAZY)
    private List<Fichaje> fichajes;

    public Turno() {}

    public Integer getIdTurno() { return idTurno; }
    public void setIdTurno(Integer idTurno) { this.idTurno = idTurno; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public TipoTurno getTipoTurno() { return tipoTurno; }
    public void setTipoTurno(TipoTurno tipoTurno) { this.tipoTurno = tipoTurno; }

    public String getAreaAsignada() { return areaAsignada; }
    public void setAreaAsignada(String areaAsignada) { this.areaAsignada = areaAsignada; }

    public BigDecimal getHorasTrabajadas() { return horasTrabajadas; }
    public void setHorasTrabajadas(BigDecimal horasTrabajadas) { this.horasTrabajadas = horasTrabajadas; }

    public EstadoTurno getEstado() { return estado; }
    public void setEstado(EstadoTurno estado) { this.estado = estado; }

    public List<Fichaje> getFichajes() { return fichajes; }
    public void setFichajes(List<Fichaje> fichajes) { this.fichajes = fichajes; }

    public enum TipoTurno { mañana, tarde, noche, partido, completo }
    public enum EstadoTurno { programado, completado, ausente, cancelado }
}
