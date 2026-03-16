
package com.hosteleria.ui;

import com.hosteleria.controller.HosteleriaController;
import com.hosteleria.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

    public class EmpleadoFichaDialog extends JDialog {

        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private static final Color BG    = MainUI.BG_DARK;
        private static final Color FG    = MainUI.TEXT_PRIMARY;
        private static final Color MUTED = MainUI.TEXT_MUTED;
        private static final Color BORDER= MainUI.BORDER_COLOR;

        public EmpleadoFichaDialog(Frame parent, HosteleriaController ctrl, Empleado empleado) {
            super(parent, "Ficha — " + empleado.getNombre() + " " + empleado.getApellidos(), true);
            setSize(820, 580);
            setMinimumSize(new Dimension(680, 460));
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout(0, 0));

            add(crearCabecera(empleado), BorderLayout.NORTH);

            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(new Font("SansSerif", Font.PLAIN, 12));
            tabs.addTab("📋 Historial laboral", tablaHistorial(ctrl.getHistorialLaboralPorEmpleado(empleado.getIdEmpleado())));
            tabs.addTab("📄 Contratos",         tablaContratos(ctrl.getContratosPorEmpleado(empleado.getIdEmpleado())));
            tabs.addTab("🎓 Formación",         tablaFormacion(empleado.getFormaciones()));
            tabs.addTab("🗂️ Documentación",     panelDoc(empleado));
            add(tabs, BorderLayout.CENTER);
        }

        private JPanel crearCabecera(Empleado e) {
            JPanel p = new JPanel(new BorderLayout(12, 0));
            p.setBorder(new EmptyBorder(12, 16, 12, 16));
            p.setBackground(MainUI.BG_PANEL);

            JLabel foto = new JLabel();
            foto.setPreferredSize(new Dimension(80, 80));
            if (e.getFotoPerfil() != null && !e.getFotoPerfil().isBlank()) {
                try {
                    ImageIcon ico = new ImageIcon(e.getFotoPerfil());
                    foto.setIcon(new ImageIcon(ico.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)));
                } catch (Exception ignored) {}
            }
            p.add(foto, BorderLayout.WEST);

            JPanel datos = new JPanel(); datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS)); datos.setOpaque(false);
            lbl(datos, e.getNombre() + " " + e.getApellidos(), 15, Font.BOLD,  FG);
            lbl(datos, "DNI: " + s(e.getDni()) + "   ·   NSS: " + s(e.getNumeroSeguridadSocial()), 12, Font.PLAIN, MUTED);
            lbl(datos, "Área: " + (e.getArea()   != null ? e.getArea().getNombre()   : "—")
                    + "   ·   Puesto: " + (e.getPuesto() != null ? e.getPuesto().getTitulo() : "—"), 12, Font.PLAIN, FG);
            lbl(datos, "Contrato: " + e.getTipoContrato() + "   ·   Estado: " + e.getEstado(), 12, Font.PLAIN, FG);
            lbl(datos, "📞 " + s(e.getTelefono()) + "   ✉ " + s(e.getEmail()), 12, Font.PLAIN, MUTED);
            p.add(datos, BorderLayout.CENTER);
            return p;
        }

        private JScrollPane tablaHistorial(List<HistorialLaboral> lista) {
            String[] cols = {"Fecha","Área ant.","Área nueva","Puesto ant.","Puesto nuevo","Sal. ant.","Sal. nuevo","Motivo"};
            Object[][] rows = new Object[lista.size()][cols.length];
            for (int i = 0; i < lista.size(); i++) {
                HistorialLaboral h = lista.get(i);
                rows[i] = new Object[]{
                        h.getFecha() != null ? h.getFecha().format(FMT) : "—",
                        s(h.getAreaAnterior()), s(h.getAreaNueva()),
                        s(h.getPuestoAnterior()), s(h.getPuestoNuevo()),
                        h.getSalarioAnterior() != null ? h.getSalarioAnterior()+" €" : "—",
                        h.getSalarioNuevo()    != null ? h.getSalarioNuevo()   +" €" : "—",
                        s(h.getMotivo())
                };
            }
            return scroll(cols, rows);
        }

        private JScrollPane tablaContratos(List<Contrato> lista) {
            String[] cols = {"Tipo","Inicio","Fin previsto","Fin real","Salario","Renovación","Observaciones"};
            Object[][] rows = new Object[lista.size()][cols.length];
            for (int i = 0; i < lista.size(); i++) {
                Contrato c = lista.get(i);
                rows[i] = new Object[]{
                        c.getTipoContrato(),
                        c.getFechaInicio()      != null ? c.getFechaInicio().format(FMT)      : "—",
                        c.getFechaFinPrevista() != null ? c.getFechaFinPrevista().format(FMT) : "—",
                        c.getFechaFinReal()     != null ? c.getFechaFinReal().format(FMT)     : "—",
                        c.getSalarioBase()      != null ? c.getSalarioBase()+" €"             : "—",
                        s(c.getRenovacion()), s(c.getObservaciones())
                };
            }
            return scroll(cols, rows);
        }

        private JScrollPane tablaFormacion(List<Formacion> lista) {
            if (lista == null) lista = List.of();
            String[] cols = {"Curso","Tipo","Inicio","Fin","Horas","Institución","Certificado","Caducidad"};
            Object[][] rows = new Object[lista.size()][cols.length];
            for (int i = 0; i < lista.size(); i++) {
                Formacion f = lista.get(i);
                rows[i] = new Object[]{
                        f.getCurso(), f.getTipo(),
                        f.getFechaInicio() != null ? f.getFechaInicio().format(FMT) : "—",
                        f.getFechaFin()    != null ? f.getFechaFin().format(FMT)    : "—",
                        f.getDuracionHoras() != null ? f.getDuracionHoras()+"h"    : "—",
                        s(f.getInstitucion()),
                        Boolean.TRUE.equals(f.getCertificado()) ? "Sí" : "No",
                        f.getFechaCaducidad() != null ? f.getFechaCaducidad().format(FMT) : "—"
                };
            }
            return scroll(cols, rows);
        }

        private JPanel panelDoc(Empleado e) {
            JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(BG); p.setBorder(new EmptyBorder(16, 20, 16, 20));
            lbl(p, "DNI: " + s(e.getDni()), 13, Font.PLAIN, FG);
            lbl(p, "Nº Seguridad Social: " + s(e.getNumeroSeguridadSocial()), 13, Font.PLAIN, FG);
            lbl(p, "Carnet manipulador: " + (Boolean.TRUE.equals(e.getCarnetManipulador()) ? "Sí" : "No"), 13, Font.PLAIN, FG);
            if (Boolean.TRUE.equals(e.getCarnetManipulador()) && e.getFechaCarnetManipulador() != null)
                lbl(p, "Fecha carnet: " + e.getFechaCarnetManipulador().format(FMT), 13, Font.PLAIN, FG);
            if (e.getContactoEmergenciaNombre() != null) {
                p.add(Box.createVerticalStrut(10));
                lbl(p, "Contacto emergencia:", 12, Font.BOLD, MUTED);
                lbl(p, "  " + e.getContactoEmergenciaNombre()
                        + " · " + s(e.getContactoEmergenciaRelacion())
                        + " · " + s(e.getContactoEmergenciaTelefono()), 13, Font.PLAIN, FG);
            }
            return p;
        }

        private JScrollPane scroll(String[] cols, Object[][] rows) {
            DefaultTableModel m = new DefaultTableModel(rows, cols) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable t = new JTable(m);
            t.setRowHeight(24); t.setFont(new Font("SansSerif", Font.PLAIN, 12));
            t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
            t.setGridColor(BORDER); t.setFillsViewportHeight(true);
            return new JScrollPane(t);
        }

        private void lbl(JPanel p, String txt, int size, int style, Color color) {
            JLabel l = new JLabel(txt); l.setFont(new Font("SansSerif", style, size));
            l.setForeground(color); l.setAlignmentX(Component.LEFT_ALIGNMENT); p.add(l);
        }

        private String s(String v) { return v != null ? v : "—"; }
    }