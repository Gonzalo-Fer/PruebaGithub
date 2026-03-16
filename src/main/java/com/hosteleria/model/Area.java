package com.hosteleria.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "areas")
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_area")
    private Integer idArea;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoArea tipo;

    @Column(name = "responsable_id")
    private Integer responsableId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoArea estado = EstadoArea.activa;

    @OneToMany(mappedBy = "area", fetch = FetchType.LAZY)
    private List<Empleado> empleados;

    public Area() {}

    public Integer getIdArea() { return idArea; }
    public void setIdArea(Integer idArea) { this.idArea = idArea; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public TipoArea getTipo() { return tipo; }
    public void setTipo(TipoArea tipo) { this.tipo = tipo; }

    public Integer getResponsableId() { return responsableId; }
    public void setResponsableId(Integer responsableId) { this.responsableId = responsableId; }

    public EstadoArea getEstado() { return estado; }
    public void setEstado(EstadoArea estado) { this.estado = estado; }

    public List<Empleado> getEmpleados() { return empleados; }
    public void setEmpleados(List<Empleado> empleados) { this.empleados = empleados; }

    @Override
    public String toString() { return nombre != null ? nombre : ""; }

    public enum TipoArea { cocina, sala, recepcion, limpieza, administracion, bar, eventos }
    public enum EstadoArea { activa, inactiva }
}
