package com.hosteleria.controller;

import com.hosteleria.model.*;
import com.hosteleria.util.HibernateUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Fachada principal del sistema.
 *
 * Delega en los controladores especializados:
 *   - EmpleadoController  → empleados, áreas, puestos, contratos, historial
 *   - TurnoController     → turnos, fichajes
 *   - AusenciaController  → ausencias
 *   - NominaController    → nóminas, propinas
 *   - FormacionController → formación, evaluaciones
 *   - UsuarioController   → usuarios
 *
 * Mantiene la interfaz pública original para que las clases de UI
 * no necesiten ningún cambio.
 */
public class HosteleriaController {

    private final EmpleadoController  empleados  = new EmpleadoController();
    private final TurnoController     turnos     = new TurnoController();
    private final AusenciaController  ausencias  = new AusenciaController();
    private final NominaController    nominas    = new NominaController();
    private final FormacionController formacion  = new FormacionController();
    private final UsuarioController   usuarios    = new UsuarioController();
    private final EvaluacionController evaluacion  = new EvaluacionController();

    // ── Acceso directo a controladores especializados ─────────────────
    public EmpleadoController  empleados()  { return empleados; }
    public TurnoController     turnos()     { return turnos; }
    public AusenciaController  ausencias()  { return ausencias; }
    public NominaController    nominas()    { return nominas; }
    public FormacionController formacion()  { return formacion; }
    public UsuarioController    usuarios()    { return usuarios; }
    public EvaluacionController evaluacion()  { return evaluacion; }

    // ÁREAS
    public List<Area>            getAllAreas()                          { return empleados.getAllAreas(); }
    public List<Area>            getAreasConEmpleados()                 { return empleados.getAreasConEmpleados(); }
    public Optional<Area>        getAreaCompleta(int id)                { return empleados.getAreaCompleta(id); }

    // PUESTOS
    public List<Puesto>          getAllPuestos()                        { return empleados.getAllPuestos(); }
    public List<Puesto>          getPuestosConEmpleados()               { return empleados.getPuestosConEmpleados(); }

    // EMPLEADOS
    public List<Empleado>        getAllEmpleados()                      { return empleados.getAllEmpleados(); }
    public List<Empleado>        getEmpleadosConAreaYPuesto()           { return empleados.getEmpleadosConAreaYPuesto(); }
    public List<Empleado>        getEmpleadosActivosConAreaYPuesto()    { return empleados.getEmpleadosActivosConAreaYPuesto(); }
    public List<Empleado>        getEmpleadosConTurnos()                { return empleados.getEmpleadosConTurnos(); }
    public List<Empleado>        getEmpleadosConNominas()               { return empleados.getEmpleadosConNominas(); }
    public List<Empleado>        getEmpleadosConAusencias()             { return empleados.getEmpleadosConAusencias(); }
    public List<Empleado>        getEmpleadosConFormacion()             { return empleados.getEmpleadosConFormacion(); }
    public List<Empleado>        getEmpleadosConEvaluaciones()          { return empleados.getEmpleadosConEvaluaciones(); }
    public Optional<Empleado>    getEmpleadoCompleto(int id)            { return empleados.getEmpleadoCompleto(id); }
    public boolean               guardarEmpleado(Empleado e)            { return empleados.guardarEmpleado(e); }
    public boolean               actualizarEmpleado(Empleado e)         { return empleados.actualizarEmpleado(e); }

    // HISTORIAL LABORAL Y CONTRATOS
    public List<HistorialLaboral> getHistorialLaboralPorEmpleado(int id) { return empleados.getHistorialLaboralPorEmpleado(id); }
    public boolean                guardarHistorial(HistorialLaboral h)   { return empleados.guardarHistorial(h); }
    public List<Contrato>         getContratosPorEmpleado(int id)        { return empleados.getContratosPorEmpleado(id); }
    public boolean                guardarContrato(Contrato c)            { return empleados.guardarContrato(c); }

    // TURNOS
    public List<Turno>           getAllTurnos()                                              { return turnos.getAllTurnos(); }
    public List<Turno>           getTurnosConEmpleado()                                     { return turnos.getTurnosConEmpleado(); }
    public List<Turno>           getTurnosPorFechaConEmpleado(LocalDate f)                  { return turnos.getTurnosPorFechaConEmpleado(f); }
    public List<Turno>           getTurnosConFichajesPorEmpleado(int id)                    { return turnos.getTurnosConFichajesPorEmpleado(id); }
    public List<Turno>           getTurnosPorRangoConEmpleado(LocalDate d, LocalDate h)     { return turnos.getTurnosPorRangoConEmpleado(d, h); }
    public List<Turno>           getTurnosSinFichaje(LocalDate f)                           { return turnos.getTurnosSinFichaje(f); }
    public Optional<Turno>       getTurnoPorId(int id)             { return turnos.getTurnoPorId(id); }
    public Optional<Turno>       getTurnoDeEmpleadoEnFecha(int id, LocalDate f)           { return turnos.getTurnoDeEmpleadoEnFecha(id, f); }
    public boolean               guardarTurno(Turno t)                           { return turnos.guardarTurno(t); }
    public boolean               actualizarTurno(Turno t)                                { return turnos.actualizarTurno(t); }
    public boolean               eliminarTurno(int id)        { return turnos.eliminarTurno(id); }
    public List<Fichaje> getFichajesConEmpleadoConTurno() { return turnos.getFichajesConEmpleadoConTurno(); }

    // FICHAJES
    public List<Fichaje>         getAllFichajes()                                                   { return turnos.getAllFichajes(); }
    public List<Fichaje>         getFichajesPorEmpleadoYRango(int id, LocalDate d, LocalDate h)    { return turnos.getFichajesPorEmpleadoYRango(id, d, h); }
    public Optional<Fichaje>     getFichajeAbiertoHoy(int id, LocalDate f)                         { return turnos.getFichajeAbiertoHoy(id, f); }
    public List<Fichaje>         getFichajesAbiertos(LocalDate f)                                  { return turnos.getFichajesAbiertos(f); }
    public List<Fichaje>         getFichajesConRetrasoEnRango(LocalDate d, LocalDate h, int u)     { return turnos.getFichajesConRetrasoEnRango(d, h, u); }
    public Optional<Fichaje>     getFichajePorId(int id)                                           { return turnos.getFichajePorId(id); }
    public boolean               guardarFichaje(Fichaje f)                                         { return turnos.guardarFichaje(f); }
    public boolean               actualizarFichaje(Fichaje f)                                      { return turnos.actualizarFichaje(f); }

    // AUSENCIAS
    public List<Ausencia>        getAllAusencias()                       { return ausencias.getAllAusencias(); }
    public List<Ausencia>        getAusenciasConEmpleado()               { return ausencias.getAusenciasConEmpleado(); }
    public List<Ausencia>        getAusenciasPendientes()                { return ausencias.getAusenciasPendientes(); }
    public List<Ausencia>        getAusenciasActivasEnFecha(LocalDate f) { return ausencias.getAusenciasActivasEnFecha(f); }
    public List<Ausencia>        getAusenciasPorEmpleado(int id)         { return ausencias.getAusenciasPorEmpleado(id); }
    public boolean               guardarAusencia(Ausencia a)             { return ausencias.guardarAusencia(a); }
    public boolean               actualizarAusencia(Ausencia a)          { return ausencias.actualizarAusencia(a); }

    // NÓMINAS
    public List<Nomina>          getAllNominas()                        { return nominas.getAllNominas(); }
    public List<Nomina>          getNominasConEmpleado()                { return nominas.getNominasConEmpleado(); }
    public List<Nomina>          getNominasPorEmpleado(int id)          { return nominas.getNominasPorEmpleado(id); }
    public List<Nomina>          getNominasPendientes()                 { return nominas.getNominasPendientes(); }
    public boolean               guardarNomina(Nomina n)                { return nominas.guardarNomina(n); }
    public boolean               actualizarNomina(Nomina n)             { return nominas.actualizarNomina(n); }

    // PROPINAS
    public List<Propina>         getAllPropinas()                                  { return nominas.getAllPropinas(); }
    public List<Propina>         getPropinasConEmpleado()                          { return nominas.getPropinasConEmpleado(); }
    public List<Propina>         getPropinasDeEmpleadoEnMes(int id, int m, int a) { return nominas.getPropinasDeEmpleadoEnMes(id, m, a); }
    public boolean               guardarPropina(Propina p)                         { return nominas.guardarPropina(p); }

    // FORMACIÓN
    public List<Formacion>       getAllFormaciones()                                { return formacion.getAllFormaciones(); }
    public List<Formacion>       getFormacionesConEmpleado()                        { return formacion.getFormacionesConEmpleado(); }
    public List<Formacion>       getFormacionesProximasACaducar(LocalDate h)        { return formacion.getFormacionesProximasACaducar(h); }
    public List<Formacion>       getFormacionesPorTipo(Formacion.TipoFormacion t)   { return formacion.getFormacionesPorTipo(t); }
    public boolean               guardarFormacion(Formacion f)                      { return formacion.guardarFormacion(f); }
    public boolean               actualizarFormacion(Formacion f)                   { return formacion.actualizarFormacion(f); }

    // EVALUACIONES
    public List<Evaluacion>      getAllEvaluaciones()                               { return formacion.getAllEvaluaciones(); }
    public List<Evaluacion>      getEvaluacionesConEmpleadoYEvaluador()             { return formacion.getEvaluacionesConEmpleadoYEvaluador(); }
    public List<Evaluacion>      getEvaluacionesPorEmpleado(int id)                 { return formacion.getEvaluacionesPorEmpleado(id); }
    public boolean               guardarEvaluacion(Evaluacion e)                    { return formacion.guardarEvaluacion(e); }
    public boolean               actualizarEvaluacion(Evaluacion e)                 { return formacion.actualizarEvaluacion(e); }

    // USUARIOS
    public List<Usuario>         getAllUsuarios()                        { return usuarios.getAllUsuarios(); }
    public List<Usuario>         getUsuariosConEmpleado()                { return usuarios.getUsuariosConEmpleado(); }
    public List<Usuario>         getUsuariosConAccesoPC()                { return usuarios.getUsuariosConAccesoPC(); }
    public Optional<Usuario>     getUsuarioPorUsername(String u)         { return usuarios.getUsuarioPorUsername(u); }
    public Optional<Usuario>     getUsuarioPorEmail(String e)            { return usuarios.getUsuarioPorEmail(e); }
    public List<Usuario>         getUsuariosPorRol(Usuario.Rol r)        { return usuarios.getUsuariosPorRol(r); }
    public boolean               guardarUsuario(Usuario u)               { return usuarios.guardarUsuario(u); }
    public void                  registrarUltimoAcceso(int id)           { usuarios.registrarUltimoAcceso(id); }

    // ── main — prueba de conectividad ─────────────────────────────────
    public static void main(String[] args) {
        HosteleriaController ctrl = new HosteleriaController();
        LocalDate hoy = LocalDate.now();

        System.out.println("=== EMPLEADOS ACTIVOS CON ÁREA Y PUESTO ===");
        ctrl.getEmpleadosActivosConAreaYPuesto().forEach(e ->
            System.out.printf("  [%d] %s %s | Área: %s | Puesto: %s%n",
                e.getIdEmpleado(), e.getNombre(), e.getApellidos(),
                e.getArea()   != null ? e.getArea().getNombre()   : "—",
                e.getPuesto() != null ? e.getPuesto().getTitulo() : "—")
        );
        System.out.println("\n=== TURNOS DE HOY ===");
        ctrl.getTurnosPorFechaConEmpleado(hoy).forEach(t ->
            System.out.printf("  [%d] %s %s | %s → %s | %s%n",
                t.getIdTurno(), t.getEmpleado().getNombre(), t.getEmpleado().getApellidos(),
                t.getHoraInicio(), t.getHoraFin(), t.getEstado())
        );
        System.out.println("\n=== NÓMINAS PENDIENTES ===");
        ctrl.getNominasPendientes().forEach(n ->
            System.out.printf("  [%d] %s %s | %02d/%d | Neto: %.2f€%n",
                n.getIdNomina(), n.getEmpleado().getNombre(), n.getEmpleado().getApellidos(),
                n.getMes(), n.getAnio(), n.getTotalNeto())
        );
        System.out.println("\n=== AUSENCIAS PENDIENTES ===");
        ctrl.getAusenciasPendientes().forEach(a ->
            System.out.printf("  [%d] %s %s | %s | %s → %s%n",
                a.getIdAusencia(), a.getEmpleado().getNombre(), a.getEmpleado().getApellidos(),
                a.getTipo(), a.getFechaInicio(), a.getFechaFin())
        );
        System.out.println("\n=== FICHA COMPLETA EMPLEADO #1 ===");
        ctrl.getEmpleadoCompleto(1).ifPresentOrElse(
            e -> {
                System.out.printf("  Nombre      : %s %s%n",  e.getNombre(), e.getApellidos());
                System.out.printf("  Turnos      : %d%n",      e.getTurnos().size());
                System.out.printf("  Nóminas     : %d%n",      e.getNominas().size());
                System.out.printf("  Ausencias   : %d%n",      e.getAusencias().size());
                System.out.printf("  Evaluaciones: %d%n",      e.getEvaluaciones().size());
            },
            () -> System.out.println("  Empleado no encontrado.")
        );

        HibernateUtil.shutdown();
    }
}