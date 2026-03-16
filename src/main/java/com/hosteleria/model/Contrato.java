package com.hosteleria.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contratos")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato")
    private Long idContrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contrato", nullable = false)
    private Empleado.TipoContrato tipoContrato;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin_prevista")
    private LocalDate fechaFinPrevista;

    @Column(name = "fecha_fin_real")
    private LocalDate fechaFinReal;

    @Column(name = "salario_base", precision = 10, scale = 2)
    private BigDecimal salarioBase;

    @Column(name = "renovacion", length = 100)
    private String renovacion;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    public Contrato() {}

    public Long getIdContrato() { return idContrato; }
    public void setIdContrato(Long idContrato) { this.idContrato = idContrato; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public Empleado.TipoContrato getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(Empleado.TipoContrato tipoContrato) { this.tipoContrato = tipoContrato; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFinPrevista() { return fechaFinPrevista; }
    public void setFechaFinPrevista(LocalDate fechaFinPrevista) { this.fechaFinPrevista = fechaFinPrevista; }

    public LocalDate getFechaFinReal() { return fechaFinReal; }
    public void setFechaFinReal(LocalDate fechaFinReal) { this.fechaFinReal = fechaFinReal; }

    public BigDecimal getSalarioBase() { return salarioBase; }
    public void setSalarioBase(BigDecimal salarioBase) { this.salarioBase = salarioBase; }

    public String getRenovacion() { return renovacion; }
    public void setRenovacion(String renovacion) { this.renovacion = renovacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}

