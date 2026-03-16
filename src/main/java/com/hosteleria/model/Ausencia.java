package com.hosteleria.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ausencias")
public class Ausencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ausencia")
    private Integer idAusencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    // ── NUEVO: responsable que aprueba/rechaza ─────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aprobado_por")
    private Usuario aprobadoPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoAusencia tipo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "dias_totales")
    private Integer diasTotales;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoAusencia estado = EstadoAusencia.solicitado;

    @Column(name = "justificante", length = 255)
    private String justificante;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // ── NUEVO: motivo de rechazo (relleno por el responsable) ──────
    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    // ── NUEVO: seguimiento de baja médica (IT) ─────────────────────
    @Column(name = "numero_baja_it", length = 50)
    private String numeroBajaIT;

    @Column(name = "fecha_alta_medica")
    private LocalDate fechaAltaMedica;

    // ── NUEVO: retribución del permiso ─────────────────────────────
    @Column(name = "retribuido")
    private Boolean retribuido = true;   // vacaciones y permisos legales → true
    // permisos no retribuidos       → false

    // ══════════════════════════════════════════════════════════════════
    // Constructor
    // ══════════════════════════════════════════════════════════════════

    public Ausencia() {}

    // ══════════════════════════════════════════════════════════════════
    // Getters / Setters existentes
    // ══════════════════════════════════════════════════════════════════

    public Integer getIdAusencia() { return idAusencia; }
    public void setIdAusencia(Integer idAusencia) { this.idAusencia = idAusencia; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public TipoAusencia getTipo() { return tipo; }
    public void setTipo(TipoAusencia tipo) { this.tipo = tipo; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Integer getDiasTotales() { return diasTotales; }
    public void setDiasTotales(Integer diasTotales) { this.diasTotales = diasTotales; }

    public EstadoAusencia getEstado() { return estado; }
    public void setEstado(EstadoAusencia estado) { this.estado = estado; }

    public String getJustificante() { return justificante; }
    public void setJustificante(String justificante) { this.justificante = justificante; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    // ══════════════════════════════════════════════════════════════════
    // Getters / Setters NUEVOS
    // ══════════════════════════════════════════════════════════════════

    public Usuario getAprobadoPor() { return aprobadoPor; }
    public void setAprobadoPor(Usuario aprobadoPor) { this.aprobadoPor = aprobadoPor; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public String getNumeroBajaIT() { return numeroBajaIT; }
    public void setNumeroBajaIT(String numeroBajaIT) { this.numeroBajaIT = numeroBajaIT; }

    public LocalDate getFechaAltaMedica() { return fechaAltaMedica; }
    public void setFechaAltaMedica(LocalDate fechaAltaMedica) { this.fechaAltaMedica = fechaAltaMedica; }

    public Boolean getRetribuido() { return retribuido; }
    public void setRetribuido(Boolean retribuido) { this.retribuido = retribuido; }

    // ══════════════════════════════════════════════════════════════════
    // Enums — se añaden permiso_retribuido y permiso_no_retribuido
    // ══════════════════════════════════════════════════════════════════

    public enum TipoAusencia {
        vacaciones,
        enfermedad,
        permiso_personal,
        baja_medica,
        asuntos_propios,
        maternidad_paternidad,
        permiso_retribuido,       // NUEVO
        permiso_no_retribuido     // NUEVO
    }

    public enum EstadoAusencia {
        solicitado,
        aprobado,
        rechazado,
        cancelado
    }
}