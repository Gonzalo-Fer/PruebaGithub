package com.hosteleria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "nominas")
public class Nomina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nomina")
    private Integer idNomina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @Column(name = "mes", nullable = false)
    private Integer mes;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "salario_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal salarioBase;

    @Column(name = "horas_extra", precision = 10, scale = 2)
    private BigDecimal horasExtra = BigDecimal.ZERO;

    @Column(name = "propinas", precision = 10, scale = 2)
    private BigDecimal propinas = BigDecimal.ZERO;

    @Column(name = "complementos", precision = 10, scale = 2)
    private BigDecimal complementos = BigDecimal.ZERO;

    @Column(name = "deducciones", precision = 10, scale = 2)
    private BigDecimal deducciones = BigDecimal.ZERO;

    @Column(name = "seguridad_social", precision = 10, scale = 2)
    private BigDecimal seguridadSocial = BigDecimal.ZERO;

    @Column(name = "irpf", precision = 10, scale = 2)
    private BigDecimal irpf = BigDecimal.ZERO;

    @Column(name = "total_neto", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalNeto;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoNomina estado = EstadoNomina.pendiente;

    public Nomina() {}

    public Integer getIdNomina() { return idNomina; }
    public void setIdNomina(Integer idNomina) { this.idNomina = idNomina; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public BigDecimal getSalarioBase() { return salarioBase; }
    public void setSalarioBase(BigDecimal salarioBase) { this.salarioBase = salarioBase; }

    public BigDecimal getHorasExtra() { return horasExtra; }
    public void setHorasExtra(BigDecimal horasExtra) { this.horasExtra = horasExtra; }

    public BigDecimal getPropinas() { return propinas; }
    public void setPropinas(BigDecimal propinas) { this.propinas = propinas; }

    public BigDecimal getComplementos() { return complementos; }
    public void setComplementos(BigDecimal complementos) { this.complementos = complementos; }

    public BigDecimal getDeducciones() { return deducciones; }
    public void setDeducciones(BigDecimal deducciones) { this.deducciones = deducciones; }

    public BigDecimal getSeguridadSocial() { return seguridadSocial; }
    public void setSeguridadSocial(BigDecimal seguridadSocial) { this.seguridadSocial = seguridadSocial; }

    public BigDecimal getIrpf() { return irpf; }
    public void setIrpf(BigDecimal irpf) { this.irpf = irpf; }

    public BigDecimal getTotalNeto() { return totalNeto; }
    public void setTotalNeto(BigDecimal totalNeto) { this.totalNeto = totalNeto; }

    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    public EstadoNomina getEstado() { return estado; }
    public void setEstado(EstadoNomina estado) { this.estado = estado; }

    public enum EstadoNomina { pendiente, pagada, revision }
}
