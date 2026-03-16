package com.hosteleria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "puestos")
public class Puesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_puesto")
    private Integer idPuesto;

    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private CategoriaPuesto categoria;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "salario_base", precision = 10, scale = 2)
    private BigDecimal salarioBase;

    @Column(name = "requiere_certificacion")
    private Boolean requiereCertificacion = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel")
    private NivelPuesto nivel = NivelPuesto.junior;

    @OneToMany(mappedBy = "puesto", fetch = FetchType.LAZY)
    private List<Empleado> empleados;

    public Puesto() {}

    public Integer getIdPuesto() { return idPuesto; }
    public void setIdPuesto(Integer idPuesto) { this.idPuesto = idPuesto; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public CategoriaPuesto getCategoria() { return categoria; }
    public void setCategoria(CategoriaPuesto categoria) { this.categoria = categoria; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getSalarioBase() { return salarioBase; }
    public void setSalarioBase(BigDecimal salarioBase) { this.salarioBase = salarioBase; }

    public Boolean getRequiereCertificacion() { return requiereCertificacion; }
    public void setRequiereCertificacion(Boolean requiereCertificacion) { this.requiereCertificacion = requiereCertificacion; }

    public NivelPuesto getNivel() { return nivel; }
    public void setNivel(NivelPuesto nivel) { this.nivel = nivel; }

    public List<Empleado> getEmpleados() { return empleados; }
    public void setEmpleados(List<Empleado> empleados) { this.empleados = empleados; }

    @Override
    public String toString() { return titulo != null ? titulo : ""; }

    public enum CategoriaPuesto { cocinero, camarero, recepcionista, limpieza, barman, gerente, ayudante, sommelier, maitre }
    public enum NivelPuesto { aprendiz, junior, senior, jefe, gerente }
}
