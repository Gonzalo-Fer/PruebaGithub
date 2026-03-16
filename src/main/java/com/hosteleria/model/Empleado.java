package com.hosteleria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "empleados")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Integer idEmpleado;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "dni", unique = true, nullable = false, length = 20)
    private String dni;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "fecha_contratacion", nullable = false)
    private LocalDate fechaContratacion;

    @Column(name = "fecha_baja")
    private LocalDate fechaBaja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_area")
    private Area area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_puesto")
    private Puesto puesto;

    @Column(name = "salario_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal salarioBase;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contrato", nullable = false)
    private TipoContrato tipoContrato;

    @Column(name = "carnet_manipulador")
    private Boolean carnetManipulador = false;

    @Column(name = "fecha_carnet_manipulador")
    private LocalDate fechaCarnetManipulador;

    @Column(name = "numero_seguridad_social", length = 50)
    private String numeroSeguridadSocial;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoEmpleado estado = EstadoEmpleado.activo;

    @Column(name = "foto_perfil", length = 255)
    private String fotoPerfil;

    @Column(name = "contacto_emergencia_nombre", length = 100)
    private String contactoEmergenciaNombre;

    @Column(name = "contacto_emergencia_telefono", length = 20)
    private String contactoEmergenciaTelefono;

    @Column(name = "contacto_emergencia_relacion", length = 50)
    private String contactoEmergenciaRelacion;

    @OneToMany(mappedBy = "empleado", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Turno> turnos;

    @OneToMany(mappedBy = "empleado", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Fichaje> fichajes;

    @OneToMany(mappedBy = "empleado", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Propina> propinas;

    @OneToMany(mappedBy = "empleado", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Nomina> nominas;

    @OneToMany(mappedBy = "empleado", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Evaluacion> evaluaciones;

    @OneToMany(mappedBy = "evaluador", fetch = FetchType.LAZY)
    private List<Evaluacion> evaluacionesRealizadas;

    @OneToMany(mappedBy = "empleado", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Formacion> formaciones;

    @OneToMany(mappedBy = "empleado", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Ausencia> ausencias;

    public Empleado() {}

    public Integer getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Integer idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(LocalDate fechaContratacion) { this.fechaContratacion = fechaContratacion; }

    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }

    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }

    public Puesto getPuesto() { return puesto; }
    public void setPuesto(Puesto puesto) { this.puesto = puesto; }

    public BigDecimal getSalarioBase() { return salarioBase; }
    public void setSalarioBase(BigDecimal salarioBase) { this.salarioBase = salarioBase; }

    public TipoContrato getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(TipoContrato tipoContrato) { this.tipoContrato = tipoContrato; }

    public Boolean getCarnetManipulador() { return carnetManipulador; }
    public void setCarnetManipulador(Boolean carnetManipulador) { this.carnetManipulador = carnetManipulador; }

    public LocalDate getFechaCarnetManipulador() { return fechaCarnetManipulador; }
    public void setFechaCarnetManipulador(LocalDate fechaCarnetManipulador) { this.fechaCarnetManipulador = fechaCarnetManipulador; }

    public String getNumeroSeguridadSocial() { return numeroSeguridadSocial; }
    public void setNumeroSeguridadSocial(String numeroSeguridadSocial) { this.numeroSeguridadSocial = numeroSeguridadSocial; }

    public EstadoEmpleado getEstado() { return estado; }
    public void setEstado(EstadoEmpleado estado) { this.estado = estado; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public String getContactoEmergenciaNombre() { return contactoEmergenciaNombre; }
    public void setContactoEmergenciaNombre(String contactoEmergenciaNombre) { this.contactoEmergenciaNombre = contactoEmergenciaNombre; }

    public String getContactoEmergenciaTelefono() { return contactoEmergenciaTelefono; }
    public void setContactoEmergenciaTelefono(String contactoEmergenciaTelefono) { this.contactoEmergenciaTelefono = contactoEmergenciaTelefono; }

    public String getContactoEmergenciaRelacion() { return contactoEmergenciaRelacion; }
    public void setContactoEmergenciaRelacion(String contactoEmergenciaRelacion) { this.contactoEmergenciaRelacion = contactoEmergenciaRelacion; }

    public List<Turno> getTurnos() { return turnos; }
    public void setTurnos(List<Turno> turnos) { this.turnos = turnos; }

    public List<Fichaje> getFichajes() { return fichajes; }
    public void setFichajes(List<Fichaje> fichajes) { this.fichajes = fichajes; }

    public List<Propina> getPropinas() { return propinas; }
    public void setPropinas(List<Propina> propinas) { this.propinas = propinas; }

    public List<Nomina> getNominas() { return nominas; }
    public void setNominas(List<Nomina> nominas) { this.nominas = nominas; }

    public List<Evaluacion> getEvaluaciones() { return evaluaciones; }
    public void setEvaluaciones(List<Evaluacion> evaluaciones) { this.evaluaciones = evaluaciones; }

    public List<Evaluacion> getEvaluacionesRealizadas() { return evaluacionesRealizadas; }
    public void setEvaluacionesRealizadas(List<Evaluacion> evaluacionesRealizadas) { this.evaluacionesRealizadas = evaluacionesRealizadas; }

    public List<Formacion> getFormaciones() { return formaciones; }
    public void setFormaciones(List<Formacion> formaciones) { this.formaciones = formaciones; }

    public List<Ausencia> getAusencias() { return ausencias; }
    public void setAusencias(List<Ausencia> ausencias) { this.ausencias = ausencias; }

    @Override
    public String toString() {
        return (nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : "") + " (" + (dni != null ? dni : "") + ")";
    }

    public enum TipoContrato { indefinido, temporal, media_jornada, fines_semana, eventual }
    public enum EstadoEmpleado { activo, baja_temporal, vacaciones, baja_definitiva }
}
