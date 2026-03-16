package com.hosteleria.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "historial_laboral")
public class HistorialLaboral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "area_anterior", length = 100)
    private String areaAnterior;

    @Column(name = "area_nueva", length = 100)
    private String areaNueva;

    @Column(name = "puesto_anterior", length = 100)
    private String puestoAnterior;

    @Column(name = "puesto_nuevo", length = 100)
    private String puestoNuevo;

    @Column(name = "salario_anterior", precision = 10, scale = 2)
    private BigDecimal salarioAnterior;

    @Column(name = "salario_nuevo", precision = 10, scale = 2)
    private BigDecimal salarioNuevo;

    @Column(name = "motivo", length = 255)
    private String motivo;

    public HistorialLaboral() {}

    public Long getIdHistorial() { return idHistorial; }
    public void setIdHistorial(Long idHistorial) { this.idHistorial = idHistorial; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getAreaAnterior() { return areaAnterior; }
    public void setAreaAnterior(String areaAnterior) { this.areaAnterior = areaAnterior; }

    public String getAreaNueva() { return areaNueva; }
    public void setAreaNueva(String areaNueva) { this.areaNueva = areaNueva; }

    public String getPuestoAnterior() { return puestoAnterior; }
    public void setPuestoAnterior(String puestoAnterior) { this.puestoAnterior = puestoAnterior; }

    public String getPuestoNuevo() { return puestoNuevo; }
    public void setPuestoNuevo(String puestoNuevo) { this.puestoNuevo = puestoNuevo; }

    public BigDecimal getSalarioAnterior() { return salarioAnterior; }
    public void setSalarioAnterior(BigDecimal salarioAnterior) { this.salarioAnterior = salarioAnterior; }

    public BigDecimal getSalarioNuevo() { return salarioNuevo; }
    public void setSalarioNuevo(BigDecimal salarioNuevo) { this.salarioNuevo = salarioNuevo; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}

