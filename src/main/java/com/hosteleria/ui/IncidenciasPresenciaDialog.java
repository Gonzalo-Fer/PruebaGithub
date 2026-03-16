package com.hosteleria.ui;

import com.hosteleria.controller.HosteleriaController;
import com.hosteleria.model.Fichaje;
import com.hosteleria.model.Turno;
import com.hosteleria.service.PresenciaService;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Diálogo de incidencias del control de presencia.
 *
 * Tres pestañas:
 *   ⚠️ Retrasos     → fichajes con retraso ≥ umbral en un rango de fechas
 *   🚫 Ausencias    → turnos de hoy sin fichaje de entrada
 *   🔓 Sin salida   → fichajes de hoy con entrada pero sin hora de salida
 *
 * Acciones disponibles:
 *   · Filtrar por rango de fechas
 *   · Cerrar todos los fichajes abiertos de hoy automáticamente
 *   · Doble clic en fichaje → corregir entrada / salida manualmente
 */
public class IncidenciasPresenciaDialog extends JDialog {

    // ── Colores ───────────────────────────────────────────────────────
    private static final Color ACCENT     = new Color(237, 137, 54);
    private static final Color BG         = new Color(247, 250, 252);
    private static final Color ROJO_C     = new Color(254, 178, 178);
    private static final Color AMBAR_C    = new Color(254, 215, 170);
    private static final Color BORDER_C   = new Color(226, 232, 240);
    private static final Color TEXT_D     = new Color(26,  32,  44);
    private static final Color TEXT_M     = new Color(113, 128, 150);

    private final PresenciaService     presencia;
    private final HosteleriaController ctrl;

    private JTextField  tfDesde, tfHasta;
    private JTabbedPane tabs;

    public IncidenciasPresenciaDialog(Frame parent,
                                      PresenciaService presencia,
                                      HosteleriaController ctrl) {
        super(parent, "🚨 Incidencias de presencia", true);
        this.presencia = presencia;
        this.ctrl      = ctrl;
        construirUI();
        setSize(880, 580);
        setLocationRelativeTo(parent);
    }

    private void construirUI() {
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(crearBarraFiltro(), BorderLayout.NORTH);
        tabs = crearTabs();
        add(tabs, BorderLayout.CENTER);
        add(crearPiePagina(), BorderLayout.SOUTH);
    }

    // ── Barra de filtro ───────────────────────────────────────────────

    private JPanel crearBarraFiltro() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_C));

        tfDesde = campoBarra(LocalDate.now().minusMonths(1).toString());
        tfHasta = campoBarra(LocalDate.now().toString());

        JButton btnFiltrar  = boton("🔍 Filtrar",                     ACCENT);
        JButton btnCerrarAb = boton("⚡ Cerrar fichajes abiertos hoy", new Color(246, 100, 100));

        barra.add(etiqueta("Desde:"));  barra.add(tfDesde);
        barra.add(etiqueta("Hasta:"));  barra.add(tfHasta);
        barra.add(btnFiltrar);
        barra.add(Box.createHorizontalStrut(20));
        barra.add(btnCerrarAb);

        btnFiltrar.addActionListener(e -> {
            try {
                LocalDate.parse(tfDesde.getText().trim());
                LocalDate.parse(tfHasta.getText().trim());
                recargarTabs();
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                    "Formato de fecha incorrecto.\nUsa AAAA-MM-DD.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnCerrarAb.addActionListener(e -> {
            int conf = JOptionPane.showConfirmDialog(this,
                "¿Cerrar automáticamente todos los fichajes abiertos de hoy?",
                "Confirmar cierre", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                int n = presencia.cerrarFichajesAbiertos(LocalDate.now());
                JOptionPane.showMessageDialog(this,
                    n + " fichaje(s) cerrado(s) automáticamente.");
                recargarTabs();
            }
        });

        return barra;
    }

    private void recargarTabs() {
        int sel = tabs.getSelectedIndex();
        remove(tabs);
        tabs = crearTabs();
        add(tabs, BorderLayout.CENTER);
        tabs.setSelectedIndex(sel);
        revalidate(); repaint();
    }

    // ── Pestañas ─────────────────────────────────────────────────────

    private JTabbedPane crearTabs() {
        JTabbedPane t = new JTabbedPane();
        t.setBackground(BG);
        t.addTab("⚠️ Retrasos",    tabRetrasos());
        t.addTab("🚫 Ausencias",   tabAusencias());
        t.addTab("🔓 Sin salida",  tabAbiertos());
        return t;
    }

    // Pestaña 1: Retrasos

    private JPanel tabRetrasos() {
        LocalDate desde = parseFecha(tfDesde.getText(), LocalDate.now().minusMonths(1));
        LocalDate hasta = parseFecha(tfHasta.getText(), LocalDate.now());
        List<Fichaje> datos = presencia.getIncidenciasRetraso(desde, hasta);

        String[] cols = {"ID", "Empleado", "Fecha", "Entrada", "Inicio turno",
                         "Retraso (min)", "Horas trabajadas", "Observaciones"};
        Object[][] rows = new Object[datos.size()][cols.length];
        for (int i = 0; i < datos.size(); i++) {
            Fichaje f = datos.get(i);
            String iniTurno = (f.getTurno() != null && f.getTurno().getHoraInicio() != null)
                ? f.getTurno().getHoraInicio().toString() : "—";
            rows[i] = new Object[]{
                f.getIdFichaje(),
                f.getEmpleado().getNombre() + " " + f.getEmpleado().getApellidos(),
                f.getFecha(), f.getHoraEntrada(), iniTurno,
                f.getRetrasoMinutos(),
                f.getHorasTrabajadas() != null ? f.getHorasTrabajadas() + "h" : "—",
                f.getObservaciones() != null ? f.getObservaciones() : "—"
            };
        }

        JTable tabla = crearTabla(cols, rows);
        // Colorear filas: naranja ≥15 min, rojo ≥30 min
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                Object rm = t.getModel().getValueAt(row, 5);
                int ret = rm instanceof Integer ? (Integer) rm : 0;
                setBackground(sel ? ROW_HOVER
                    : ret >= 30 ? ROJO_C : (ret >= 15 ? AMBAR_C : Color.WHITE));
                setForeground(sel ? Color.WHITE : TEXT_D);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        registrarCorreccionDobleClick(tabla, datos);
        return envolver(tabla,
            datos.size() + " fichaje(s) con retraso ≥ "
            + PresenciaService.UMBRAL_INCIDENCIA_MIN + " min   "
            + "(doble clic para corregir)");
    }

    // Pestaña 2: Ausencias no justificadas de hoy

    private JPanel tabAusencias() {
        List<Turno> datos = presencia.detectarAusenciasNoJustificadas(LocalDate.now());
        String[] cols = {"ID Turno", "Empleado", "Fecha", "Inicio", "Fin", "Tipo", "Área"};
        Object[][] rows = new Object[datos.size()][cols.length];
        for (int i = 0; i < datos.size(); i++) {
            Turno t = datos.get(i);
            rows[i] = new Object[]{
                t.getIdTurno(),
                t.getEmpleado().getNombre() + " " + t.getEmpleado().getApellidos(),
                t.getFecha(), t.getHoraInicio(), t.getHoraFin(),
                t.getTipoTurno(),
                t.getAreaAsignada() != null ? t.getAreaAsignada() : "—"
            };
        }
        JTable tabla = crearTabla(cols, rows);
        tabla.setDefaultRenderer(Object.class, rojoRenderer());
        return envolver(tabla,
            datos.size() + " turno(s) de hoy sin fichaje de entrada (ausencias sin justificar)");
    }

    // Pestaña 3: Fichajes sin hora de salida (hoy)

    private JPanel tabAbiertos() {
        List<Fichaje> datos = ctrl.getFichajesAbiertos(LocalDate.now());
        String[] cols = {"ID", "Empleado", "Fecha", "Hora entrada", "Fin turno previsto"};
        Object[][] rows = new Object[datos.size()][cols.length];
        for (int i = 0; i < datos.size(); i++) {
            Fichaje f = datos.get(i);
            rows[i] = new Object[]{
                f.getIdFichaje(),
                f.getEmpleado().getNombre() + " " + f.getEmpleado().getApellidos(),
                f.getFecha(), f.getHoraEntrada(),
                (f.getTurno() != null && f.getTurno().getHoraFin() != null)
                    ? f.getTurno().getHoraFin() : "—"
            };
        }
        JTable tabla = crearTabla(cols, rows);
        tabla.setDefaultRenderer(Object.class, ambarRenderer());
        registrarCorreccionDobleClick(tabla, datos);
        return envolver(tabla,
            datos.size() + " fichaje(s) sin hora de salida   (doble clic para registrar salida)");
    }

    // ── Corrección de fichaje ─────────────────────────────────────────

    private void registrarCorreccionDobleClick(JTable tabla, List<Fichaje> datos) {
        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tabla.getSelectedRow();
                    if (row >= 0 && row < datos.size()) abrirCorreccion(datos.get(row));
                }
            }
        });
    }

    private void abrirCorreccion(Fichaje f) {
        JTextField tfE = new JTextField(f.getHoraEntrada() != null ? f.getHoraEntrada().toString() : "", 8);
        JTextField tfS = new JTextField(f.getHoraSalida()  != null ? f.getHoraSalida().toString()  : "", 8);
        JTextField tfO = new JTextField(f.getObservaciones() != null ? f.getObservaciones() : "", 30);

        JPanel form = new JPanel(new GridLayout(3, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        form.add(new JLabel("Entrada (HH:mm):")); form.add(tfE);
        form.add(new JLabel("Salida  (HH:mm):")); form.add(tfS);
        form.add(new JLabel("Observaciones:"));    form.add(tfO);

        int opt = JOptionPane.showConfirmDialog(this, form,
            "Corregir fichaje #" + f.getIdFichaje(), JOptionPane.OK_CANCEL_OPTION);
        if (opt != JOptionPane.OK_OPTION) return;
        try {
            LocalTime entrada = tfE.getText().isBlank() ? null : LocalTime.parse(tfE.getText().trim());
            LocalTime salida  = tfS.getText().isBlank() ? null : LocalTime.parse(tfS.getText().trim());
            String    obs     = tfO.getText().isBlank() ? null : tfO.getText().trim();
            PresenciaService.Resultado r = presencia.corregirFichaje(f.getIdFichaje(), entrada, salida, obs);
            JOptionPane.showMessageDialog(this, r.mensaje,
                r.ok ? "Éxito" : "Error",
                r.ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            if (r.ok) recargarTabs();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                "Formato de hora incorrecto. Usa HH:mm", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private static final Color ROW_HOVER = new Color(254, 235, 200);

    private JTable crearTabla(String[] cols, Object[][] rows) {
        DefaultTableModel model = new DefaultTableModel(rows, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(model);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setRowHeight(27);
        t.setShowVerticalLines(false);
        t.setGridColor(BORDER_C);
        t.setSelectionBackground(ROW_HOVER);
        t.setFillsViewportHeight(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(255, 247, 237));
        h.setForeground(ACCENT);
        h.setFont(new Font("SansSerif", Font.BOLD, 12));
        h.setReorderingAllowed(false);
        return t;
    }

    private JPanel envolver(JTable tabla, String info) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel lbl = new JLabel(info);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(TEXT_M);
        p.add(lbl, BorderLayout.NORTH);
        JScrollPane sc = new JScrollPane(tabla);
        sc.setBorder(BorderFactory.createLineBorder(BORDER_C));
        p.add(sc, BorderLayout.CENTER);
        return p;
    }

    private DefaultTableCellRenderer rojoRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(sel ? ROW_HOVER : ROJO_C);
                setForeground(TEXT_D);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
    }

    private DefaultTableCellRenderer ambarRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(sel ? ROW_HOVER : AMBAR_C);
                setForeground(TEXT_D);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
    }

    private JPanel crearPiePagina() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));
        JButton cerrar = new JButton("Cerrar");
        cerrar.addActionListener(e -> dispose());
        p.add(cerrar);
        return p;
    }

    private JTextField campoBarra(String valor) {
        JTextField tf = new JTextField(valor, 11);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        return tf;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return l;
    }

    private JButton boton(String texto, Color bg) {
        JButton b = new JButton(texto);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        b.setFocusPainted(false);
        return b;
    }

    private LocalDate parseFecha(String s, LocalDate fallback) {
        try { return LocalDate.parse(s.trim()); } catch (Exception e) { return fallback; }
    }
}
