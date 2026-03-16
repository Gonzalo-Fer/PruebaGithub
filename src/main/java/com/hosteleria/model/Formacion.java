package com.hosteleria.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "formacion")
public class Formacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_formacion")
    private Integer idFormacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @Column(name = "curso", nullable = false, length = 200)
    private String curso;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoFormacion tipo;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "duracion_horas")
    private Integer duracionHoras;

    @Column(name = "certificado")
    private Boolean certificado = false;

    @Column(name = "fecha_caducidad")
    private LocalDate fechaCaducidad;

    @Column(name = "institucion", length = 200)
    private String institucion;

    public Formacion() {}

    public Integer getIdFormacion() { return idFormacion; }
    public void setIdFormacion(Integer idFormacion) { this.idFormacion = idFormacion; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }

    public TipoFormacion getTipo() { return tipo; }
    public void setTipo(TipoFormacion tipo) { this.tipo = tipo; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Integer getDuracionHoras() { return duracionHoras; }
    public void setDuracionHoras(Integer duracionHoras) { this.duracionHoras = duracionHoras; }

    public Boolean getCertificado() { return certificado; }
    public void setCertificado(Boolean certificado) { this.certificado = certificado; }

    public LocalDate getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(LocalDate fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }

    public String getInstitucion() { return institucion; }
    public void setInstitucion(String institucion) { this.institucion = institucion; }

    public enum TipoFormacion { manipulador_alimentos, alergia_intolerancia, vinos, idiomas, primeros_auxilios, prevencion_riesgos, otros }
}
