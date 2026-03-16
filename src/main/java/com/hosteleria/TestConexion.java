package com.hosteleria;

import com.hosteleria.controller.HosteleriaController;
import com.hosteleria.model.*;
import com.hosteleria.util.HibernateUtil;

import java.util.List;

/**
 * Clase de prueba rápida de conexión y consultas.
 * Ejecuta un SELECT de cada tabla y muestra los resultados en consola.
 * No requiere WebSocket ni ninguna configuración extra — solo BBDD activa.
 */
public class TestConexion {

    static final String SEP  = "─".repeat(70);
    static final String SEP2 = "═".repeat(70);

    public static void main(String[] args) {

        System.out.println(SEP2);
        System.out.println("  TEST DE CONEXIÓN — Sistema de Gestión de Hostelería");
        System.out.println(SEP2);

        HosteleriaController ctrl = new HosteleriaController();

        probarAreas(ctrl);
        probarPuestos(ctrl);
        probarEmpleados(ctrl);
        probarTurnos(ctrl);
        probarFichajes(ctrl);
        probarPropinas(ctrl);
        probarNominas(ctrl);
        probarEvaluaciones(ctrl);
        probarFormaciones(ctrl);
        probarAusencias(ctrl);

        System.out.println(SEP2);
        System.out.println("  TEST COMPLETADO");
        System.out.println(SEP2);

        HibernateUtil.shutdown();
    }

    // ── ÁREAS ────────────────────────────────────────────────────────

    static void probarAreas(HosteleriaController ctrl) {
        List<Area> lista = ctrl.getAllAreas();
        encabezado("ÁREAS", lista.size());
        System.out.printf("  %-5s %-25s %-15s %-10s%n", "ID", "NOMBRE", "TIPO", "ESTADO");
        System.out.println(SEP);
        for (Area a : lista) {
            System.out.printf("  %-5d %-25s %-15s %-10s%n",
                a.getIdArea(),
                a.getNombre(),
                a.getTipo(),
                a.getEstado());
        }
        System.out.println();
    }

    // ── PUESTOS ──────────────────────────────────────────────────────

    static void probarPuestos(HosteleriaController ctrl) {
        List<Puesto> lista = ctrl.getAllPuestos();
        encabezado("PUESTOS", lista.size());
        System.out.printf("  %-5s %-30s %-15s %-10s %-10s%n", "ID", "TÍTULO", "CATEGORÍA", "NIVEL", "SALARIO BASE");
        System.out.println(SEP);
        for (Puesto p : lista) {
            System.out.printf("  %-5d %-30s %-15s %-10s %-10s€%n",
                p.getIdPuesto(),
                p.getTitulo(),
                p.getCategoria(),
                p.getNivel(),
                p.getSalarioBase());
        }
        System.out.println();
    }

    // ── EMPLEADOS ────────────────────────────────────────────────────

    static void probarEmpleados(HosteleriaController ctrl) {
        List<Empleado> lista = ctrl.getEmpleadosConAreaYPuesto();
        encabezado("EMPLEADOS", lista.size());
        System.out.printf("  %-5s %-25s %-20s %-20s %-12s %-12s%n",
            "ID", "NOMBRE COMPLETO", "ÁREA", "PUESTO", "CONTRATO", "ESTADO");
        System.out.println(SEP);
        for (Empleado e : lista) {
            System.out.printf("  %-5d %-25s %-20s %-20s %-12s %-12s%n",
                e.getIdEmpleado(),
                e.getNombre() + " " + e.getApellidos(),
                e.getArea()   != null ? e.getArea().getNombre()   : "—",
                e.getPuesto() != null ? e.getPuesto().getTitulo() : "—",
                e.getTipoContrato(),
                e.getEstado());
        }
        System.out.println();
    }

    // ── TURNOS ───────────────────────────────────────────────────────

    static void probarTurnos(HosteleriaController ctrl) {
        List<Turno> lista = ctrl.getTurnosConEmpleado();
        encabezado("TURNOS", lista.size());
        System.out.printf("  %-5s %-12s %-25s %-10s %-8s %-8s %-6s %-12s%n",
            "ID", "FECHA", "EMPLEADO", "TIPO", "ENTRADA", "SALIDA", "HORAS", "ESTADO");
        System.out.println(SEP);
        for (Turno t : lista) {
            System.out.printf("  %-5d %-12s %-25s %-10s %-8s %-8s %-6s %-12s%n",
                t.getIdTurno(),
                t.getFecha(),
                t.getEmpleado().getNombre() + " " + t.getEmpleado().getApellidos(),
                t.getTipoTurno(),
                t.getHoraInicio(),
                t.getHoraFin(),
                t.getHorasTrabajadas(),
                t.getEstado());
        }
        System.out.println();
    }

    // ── FICHAJES ─────────────────────────────────────────────────────

    static void probarFichajes(HosteleriaController ctrl) {
        List<Fichaje> lista = ctrl.getFichajesConEmpleadoYTurno();
        encabezado("FICHAJES", lista.size());
        System.out.printf("  %-5s %-12s %-25s %-8s %-8s %-8s %-8s %-7s%n",
            "ID", "FECHA", "EMPLEADO", "ENTRADA", "SALIDA", "H.TRAB.", "H.EXTRA", "RETRASO");
        System.out.println(SEP);
        for (Fichaje f : lista) {
            System.out.printf("  %-5d %-12s %-25s %-8s %-8s %-8s %-8s %-7s%n",
                f.getIdFichaje(),
                f.getFecha(),
                f.getEmpleado().getNombre() + " " + f.getEmpleado().getApellidos(),
                f.getHoraEntrada()    != null ? f.getHoraEntrada().toString()    : "—",
                f.getHoraSalida()     != null ? f.getHoraSalida().toString()     : "—",
                f.getHorasTrabajadas()!= null ? f.getHorasTrabajadas().toString(): "—",
                f.getHorasExtra()     != null ? f.getHorasExtra().toString()     : "0",
                f.getRetrasoMinutos() + "min");
        }
        System.out.println();
    }

    // ── PROPINAS ─────────────────────────────────────────────────────

    static void probarPropinas(HosteleriaController ctrl) {
        List<Propina> lista = ctrl.getPropinasConEmpleado();
        encabezado("PROPINAS", lista.size());
        System.out.printf("  %-5s %-12s %-25s %-8s %-10s %-12s %-10s%n",
            "ID", "FECHA", "EMPLEADO", "TURNO", "IMPORTE", "TIPO", "MÉTODO");
        System.out.println(SEP);
        for (Propina p : lista) {
            System.out.printf("  %-5d %-12s %-25s %-8s %-10s %-12s %-10s%n",
                p.getIdPropina(),
                p.getFecha(),
                p.getEmpleado().getNombre() + " " + p.getEmpleado().getApellidos(),
                p.getTurno(),
                p.getImporte() + "€",
                p.getTipo(),
                p.getMetodoPago());
        }
        System.out.println();
    }

    // ── NÓMINAS ──────────────────────────────────────────────────────

    static void probarNominas(HosteleriaController ctrl) {
        List<Nomina> lista = ctrl.getNominasConEmpleado();
        encabezado("NÓMINAS", lista.size());
        System.out.printf("  %-5s %-25s %-8s %-12s %-12s %-12s %-12s %-8s%n",
            "ID", "EMPLEADO", "MES/AÑO", "S.BASE", "H.EXTRA", "PROPINAS", "TOTAL NETO", "ESTADO");
        System.out.println(SEP);
        for (Nomina n : lista) {
            System.out.printf("  %-5d %-25s %02d/%-6d %-12s %-12s %-12s %-12s %-8s%n",
                n.getIdNomina(),
                n.getEmpleado().getNombre() + " " + n.getEmpleado().getApellidos(),
                n.getMes(),
                n.getAnio(),
                n.getSalarioBase() + "€",
                n.getHorasExtra() + "€",
                n.getPropinas() + "€",
                n.getTotalNeto() + "€",
                n.getEstado());
        }
        System.out.println();
    }

    // ── EVALUACIONES ─────────────────────────────────────────────────

    static void probarEvaluaciones(HosteleriaController ctrl) {
        List<Evaluacion> lista = ctrl.getEvaluacionesConEmpleadoYEvaluador();
        encabezado("EVALUACIONES", lista.size());
        System.out.printf("  %-5s %-12s %-25s %-25s %-7s %-7s %-7s %-7s %-7s %-7s%n",
            "ID", "FECHA", "EMPLEADO", "EVALUADOR",
            "PUNTUAL", "CLIENTE", "EQUIPO", "PRODUC.", "HIGIENE", "TOTAL");
        System.out.println(SEP);
        for (Evaluacion e : lista) {
            System.out.printf("  %-5d %-12s %-25s %-25s %-7d %-7d %-7d %-7d %-7d %-7d%n",
                e.getIdEvaluacion(),
                e.getFecha(),
                e.getEmpleado().getNombre()  + " " + e.getEmpleado().getApellidos(),
                e.getEvaluador().getNombre() + " " + e.getEvaluador().getApellidos(),
                e.getPuntualidad(),
                e.getAtencionCliente(),
                e.getTrabajoEquipo(),
                e.getConocimientoProducto(),
                e.getHigienePresentacion(),
                e.getPuntuacionTotal());
        }
        System.out.println();
    }

    // ── FORMACIONES ──────────────────────────────────────────────────

    static void probarFormaciones(HosteleriaController ctrl) {
        List<Formacion> lista = ctrl.getFormacionesConEmpleado();
        encabezado("FORMACIONES", lista.size());
        System.out.printf("  %-5s %-25s %-35s %-22s %-12s %-12s%n",
            "ID", "EMPLEADO", "CURSO", "TIPO", "FECHA FIN", "CADUCIDAD");
        System.out.println(SEP);
        for (Formacion f : lista) {
            System.out.printf("  %-5d %-25s %-35s %-22s %-12s %-12s%n",
                f.getIdFormacion(),
                f.getEmpleado().getNombre() + " " + f.getEmpleado().getApellidos(),
                truncar(f.getCurso(), 33),
                f.getTipo(),
                f.getFechaFin()       != null ? f.getFechaFin().toString()       : "—",
                f.getFechaCaducidad() != null ? f.getFechaCaducidad().toString() : "—");
        }
        System.out.println();
    }

    // ── AUSENCIAS ────────────────────────────────────────────────────

    static void probarAusencias(HosteleriaController ctrl) {
        List<Ausencia> lista = ctrl.getAusenciasConEmpleado();
        encabezado("AUSENCIAS", lista.size());
        System.out.printf("  %-5s %-25s %-22s %-12s %-12s %-5s %-10s%n",
            "ID", "EMPLEADO", "TIPO", "INICIO", "FIN", "DÍAS", "ESTADO");
        System.out.println(SEP);
        for (Ausencia a : lista) {
            System.out.printf("  %-5d %-25s %-22s %-12s %-12s %-5s %-10s%n",
                a.getIdAusencia(),
                a.getEmpleado().getNombre() + " " + a.getEmpleado().getApellidos(),
                a.getTipo(),
                a.getFechaInicio(),
                a.getFechaFin(),
                a.getDiasTotales() != null ? a.getDiasTotales().toString() : "—",
                a.getEstado());
        }
        System.out.println();
    }

    // ── Utilidades ────────────────────────────────────────────────────

    static void encabezado(String tabla, int total) {
        System.out.println(SEP2);
        System.out.printf("  %s  (%d registros)%n", tabla, total);
        System.out.println(SEP2);
    }

    static String truncar(String texto, int max) {
        if (texto == null) return "—";
        return texto.length() <= max ? texto : texto.substring(0, max - 1) + "…";
    }
}
