package com.hosteleria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "propinas")
public class Propina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_propina")
    private Integer idPropina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "turno", nullable = false)
    private TurnoPropina turno;

    @Column(name = "importe", nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoPropina tipo = TipoPropina.individual;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPago metodoPago = MetodoPago.efectivo;

    public Propina() {}

    public Integer getIdPropina() { return idPropina; }
    public void setIdPropina(Integer idPropina) { this.idPropina = idPropina; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public TurnoPropina getTurno() { return turno; }
    public void setTurno(TurnoPropina turno) { this.turno = turno; }

    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }

    public TipoPropina getTipo() { return tipo; }
    public void setTipo(TipoPropina tipo) { this.tipo = tipo; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public enum TurnoPropina { mañana, tarde, noche }
    public enum TipoPropina { individual, compartida, bote_comun }
    public enum MetodoPago { efectivo, tarjeta, mixto }
}
