package com.hosteleria.service;

import com.hosteleria.controller.HosteleriaController;
import com.hosteleria.model.Area;
import com.hosteleria.model.Empleado;
import com.hosteleria.model.HistorialLaboral;
import com.hosteleria.model.Puesto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de empleados: altas, bajas y datos de contratación.
 */
public class EmpleadoGestionService {

    private final HosteleriaController ctrl = new HosteleriaController();

    /**
     * Da de alta un nuevo empleado (contratación).
     */
    public ResultadoAlta altaEmpleado(String nombre, String apellidos, String dni, String email, String telefono,
                                     LocalDate fechaNacimiento, Integer idArea, Integer idPuesto,
                                     BigDecimal salarioBase, Empleado.TipoContrato tipoContrato,
                                     String numeroSeguridadSocial, Boolean carnetManipulador) {
        if (nombre == null || nombre.isBlank() || apellidos == null || apellidos.isBlank()) {
            return new ResultadoAlta(false, "Nombre y apellidos son obligatorios.", null);
        }
        if (dni == null || dni.isBlank()) {
            return new ResultadoAlta(false, "El DNI es obligatorio.", null);
        }
        if (telefono == null || telefono.isBlank()) {
            return new ResultadoAlta(false, "El teléfono es obligatorio.", null);
        }
        if (fechaNacimiento == null) {
            return new ResultadoAlta(false, "La fecha de nacimiento es obligatoria.", null);
        }
        if (idPuesto == null || salarioBase == null || tipoContrato == null) {
            return new ResultadoAlta(false, "Puesto, salario base y tipo de contrato son obligatorios.", null);
        }

        Optional<Empleado> existente = ctrl.getAllEmpleados().stream()
            .filter(e -> dni.equalsIgnoreCase(e.getDni()))
            .findFirst();
        if (existente.isPresent()) {
            return new ResultadoAlta(false, "Ya existe un empleado con ese DNI.", null);
        }

        Empleado e = new Empleado();
        e.setNombre(nombre.trim());
        e.setApellidos(apellidos.trim());
        e.setDni(dni.trim().toUpperCase());
        e.setEmail(email != null ? email.trim() : null);
        e.setTelefono(telefono.trim());
        e.setFechaNacimiento(fechaNacimiento);
        e.setFechaContratacion(LocalDate.now());
        e.setSalarioBase(salarioBase);
        e.setTipoContrato(tipoContrato);
        e.setNumeroSeguridadSocial(numeroSeguridadSocial != null ? numeroSeguridadSocial.trim() : null);
        e.setCarnetManipulador(Boolean.TRUE.equals(carnetManipulador));
        if (Boolean.TRUE.equals(carnetManipulador)) {
            e.setFechaCarnetManipulador(LocalDate.now());
        }
        e.setEstado(Empleado.EstadoEmpleado.activo);

        if (idArea != null && idArea > 0) {
            ctrl.getAreaCompleta(idArea).ifPresent(e::setArea);
        }
        ctrl.getAllPuestos().stream()
            .filter(p -> idPuesto.equals(p.getIdPuesto()))
            .findFirst()
            .ifPresent(e::setPuesto);

        boolean ok = ctrl.guardarEmpleado(e);
        return ok ? new ResultadoAlta(true, null, e) : new ResultadoAlta(false, "Error al guardar el empleado.", null);
    }

    /**
     * Da de baja a un empleado (baja definitiva o temporal).
     */
    public ResultadoBaja bajaEmpleado(int idEmpleado, Empleado.EstadoEmpleado motivoBaja, String observaciones) {
        Optional<Empleado> opt = ctrl.getEmpleadoCompleto(idEmpleado);
        if (opt.isEmpty()) {
            return new ResultadoBaja(false, "Empleado no encontrado.");
        }
        Empleado e = opt.get();
        if (e.getEstado() == Empleado.EstadoEmpleado.baja_definitiva) {
            return new ResultadoBaja(false, "El empleado ya está dado de baja definitiva.");
        }
        e.setEstado(motivoBaja != null ? motivoBaja : Empleado.EstadoEmpleado.baja_definitiva);
        e.setFechaBaja(LocalDate.now());
        boolean ok = ctrl.actualizarEmpleado(e);
        return ok ? new ResultadoBaja(true, null) : new ResultadoBaja(false, "Error al actualizar.");
    }

    /**
     * Reactiva un empleado que estaba de baja.
     */
    public boolean reactivarEmpleado(int idEmpleado) {
        Optional<Empleado> opt = ctrl.getEmpleadoCompleto(idEmpleado);
        if (opt.isEmpty()) return false;
        Empleado e = opt.get();
        e.setEstado(Empleado.EstadoEmpleado.activo);
        e.setFechaBaja(null);
        return ctrl.actualizarEmpleado(e);
    }

    /**
     * Registra un movimiento en el historial laboral si han cambiado
     * área, puesto o salario base.
     */
    public void registrarHistorialSiCambio(Empleado anterior, Empleado modificado, String motivo) {
        boolean cambioArea = (anterior.getArea() == null && modificado.getArea() != null) ||
                (anterior.getArea() != null && modificado.getArea() == null) ||
                (anterior.getArea() != null && modificado.getArea() != null &&
                 !anterior.getArea().getNombre().equals(modificado.getArea().getNombre()));
        boolean cambioPuesto = (anterior.getPuesto() == null && modificado.getPuesto() != null) ||
                (anterior.getPuesto() != null && modificado.getPuesto() == null) ||
                (anterior.getPuesto() != null && modificado.getPuesto() != null &&
                 !anterior.getPuesto().getTitulo().equals(modificado.getPuesto().getTitulo()));
        boolean cambioSalario = anterior.getSalarioBase() != null && modificado.getSalarioBase() != null &&
                anterior.getSalarioBase().compareTo(modificado.getSalarioBase()) != 0;

        if (!cambioArea && !cambioPuesto && !cambioSalario) return;

        HistorialLaboral h = new HistorialLaboral();
        h.setEmpleado(modificado);
        h.setFecha(LocalDate.now());
        h.setAreaAnterior(anterior.getArea() != null ? anterior.getArea().getNombre() : null);
        h.setAreaNueva(modificado.getArea() != null ? modificado.getArea().getNombre() : null);
        h.setPuestoAnterior(anterior.getPuesto() != null ? anterior.getPuesto().getTitulo() : null);
        h.setPuestoNuevo(modificado.getPuesto() != null ? modificado.getPuesto().getTitulo() : null);
        h.setSalarioAnterior(anterior.getSalarioBase());
        h.setSalarioNuevo(modificado.getSalarioBase());
        h.setMotivo(motivo != null ? motivo : "Actualización de ficha");
        ctrl.guardarHistorial(h);
    }

    public List<Area> getAreas() {
        return ctrl.getAllAreas();
    }

    public List<Puesto> getPuestos() {
        return ctrl.getAllPuestos();
    }

    public Optional<Empleado> getEmpleado(int id) {
        return ctrl.getEmpleadoCompleto(id);
    }

    public static final class ResultadoAlta {
        private final boolean ok;
        private final String mensajeError;
        private final Empleado empleado;

        public ResultadoAlta(boolean ok, String mensajeError, Empleado empleado) {
            this.ok = ok;
            this.mensajeError = mensajeError;
            this.empleado = empleado;
        }
        public boolean isOk() { return ok; }
        public String getMensajeError() { return mensajeError; }
        public Empleado getEmpleado() { return empleado; }
    }

    public static final class ResultadoBaja {
        private final boolean ok;
        private final String mensajeError;

        public ResultadoBaja(boolean ok, String mensajeError) {
            this.ok = ok;
            this.mensajeError = mensajeError;
        }
        public boolean isOk() { return ok; }
        public String getMensajeError() { return mensajeError; }
    }
}
