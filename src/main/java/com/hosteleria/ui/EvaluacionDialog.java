package com.hosteleria.ui;

import com.hosteleria.controller.EvaluacionController;
import com.hosteleria.controller.HosteleriaController;
import com.hosteleria.model.Empleado;
import com.hosteleria.model.Evaluacion;
import com.hosteleria.model.Evaluacion.EstadoEvaluacion;
import com.hosteleria.model.Objetivo;
import com.hosteleria.model.Objetivo.EstadoObjetivo;
import com.hosteleria.service.EvaluacionService;
import com.hosteleria.service.SessionManager;
import com.hosteleria.model.Usuario;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Diálogo principal del módulo de Evaluación del Desempeño.
 *
 * Pestañas:
 *   1. Nueva evaluación — formulario con sliders de criterios
 *   2. Historial         — evaluaciones pasadas de un empleado
 *   3. Objetivos         — alta, seguimiento y cierre de objetivos
 */
public class EvaluacionDialog extends JDialog {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Colores del sistema
    private static final Color ACCENT       = MainUI.ACCENT;
    private static final Color BG_DARK      = MainUI.BG_DARK;
    private static final Color BG_PANEL     = MainUI.BG_PANEL;
    private static final Color TEXT_PRIMARY = MainUI.TEXT_PRIMARY;
    private static final Color TEXT_MUTED   = MainUI.TEXT_MUTED;
    private static final Color BORDER_COLOR = MainUI.BORDER_COLOR;

    private final HosteleriaController ctrl    = new HosteleriaController();
    private final EvaluacionController evCtrl  = new EvaluacionController();
    private final EvaluacionService    service = new EvaluacionService();
    private final List<Empleado>       empleados;
    private final Usuario              usuarioActual;

    public EvaluacionDialog(Frame parent) {
        super(parent, "⭐ Evaluación del Desempeño", true);
        this.empleados    = ctrl.getEmpleadosActivosConAreaYPuesto();
        this.usuarioActual = SessionManager.getInstance().getUsuarioActual();

        setSize(980, 720);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabs.addTab("📝 Nueva evaluación", crearPanelNuevaEvaluacion());
        tabs.addTab("📋 Historial",         crearPanelHistorial());
        tabs.addTab("🎯 Objetivos",          crearPanelObjetivos());

        // Empleados solo pueden ver su propio historial
        if (usuarioActual != null && usuarioActual.getRol() == Usuario.Rol.EMPLEADO) {
            tabs.setEnabledAt(0, false);
        }

        add(tabs, BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);
    }

    // ══════════════════════════════════════════════════════════════════
    // PESTAÑA 1 — NUEVA EVALUACIÓN
    // ══════════════════════════════════════════════════════════════════

    private JPanel crearPanelNuevaEvaluacion() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        // ── Cabecera: empleado, evaluador, periodo, fecha ─────────────
        JPanel cabecera = new JPanel(new GridBagLayout());
        cabecera.setBackground(BG_PANEL);
        cabecera.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(12, 16, 12, 16)));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JComboBox<Empleado> cmbEmpleado  = cmbEmpleados();
        JComboBox<Empleado> cmbEvaluador = cmbEmpleados();
        JTextField txtPeriodo  = new JTextField(12);
        JTextField txtFecha    = new JTextField(LocalDate.now().format(FMT), 10);

        addFila(cabecera, gc, 0, "Empleado evaluado:", cmbEmpleado);
        addFila(cabecera, gc, 1, "Evaluador:",         cmbEvaluador);
        addFila(cabecera, gc, 2, "Periodo (ej. Q1-2025):", txtPeriodo);
        addFila(cabecera, gc, 3, "Fecha:",             txtFecha);

        // ── Criterios con sliders ─────────────────────────────────────
        JPanel panelCriterios = new JPanel(new GridBagLayout());
        panelCriterios.setBackground(BG_PANEL);
        panelCriterios.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(12, 16, 12, 16)));
        gc = new GridBagConstraints();
        gc.insets = new Insets(5, 6, 5, 6);
        gc.anchor = GridBagConstraints.WEST;

        String[][] criterios = {
            {"Puntualidad",            "Cumplimiento de horarios y disciplina horaria"},
            {"Atención al cliente",    "Trato, amabilidad y resolución de incidencias"},
            {"Trabajo en equipo",      "Colaboración, comunicación y apoyo al equipo"},
            {"Conocimiento producto",  "Dominio de la carta, procedimientos y producto"},
            {"Higiene y presentación", "Uniformidad, higiene personal y del puesto"}
        };
        JSlider[] sliders    = new JSlider[5];
        JLabel[]  lblValores = new JLabel[5];

        for (int i = 0; i < criterios.length; i++) {
            sliders[i]    = crearSlider();
            lblValores[i] = new JLabel("5");
            lblValores[i].setFont(new Font("SansSerif", Font.BOLD, 14));
            lblValores[i].setForeground(ACCENT);
            lblValores[i].setPreferredSize(new Dimension(28, 20));

            final JLabel lv = lblValores[i];
            sliders[i].addChangeListener(e -> lv.setText(String.valueOf(((JSlider) e.getSource()).getValue())));

            gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
            gc.fill = GridBagConstraints.NONE;
            JLabel lbl = new JLabel(criterios[i][0] + ":");
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lbl.setToolTipText(criterios[i][1]);
            panelCriterios.add(lbl, gc);

            gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
            panelCriterios.add(sliders[i], gc);

            gc.gridx = 2; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
            panelCriterios.add(lblValores[i], gc);
        }

        // ── Comentarios ───────────────────────────────────────────────
        JPanel panelComentarios = new JPanel(new BorderLayout(4, 4));
        panelComentarios.setBackground(BG_PANEL);
        panelComentarios.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(10, 14, 10, 14)));
        JLabel lblComent = new JLabel("Comentarios y observaciones:");
        lblComent.setFont(new Font("SansSerif", Font.BOLD, 12));
        JTextArea txtComentarios = new JTextArea(4, 40);
        txtComentarios.setFont(new Font("SansSerif", Font.PLAIN, 12));
        txtComentarios.setLineWrap(true);
        txtComentarios.setWrapStyleWord(true);
        panelComentarios.add(lblComent, BorderLayout.NORTH);
        panelComentarios.add(new JScrollPane(txtComentarios), BorderLayout.CENTER);

        // ── Estado ────────────────────────────────────────────────────
        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panelEstado.setBackground(BG_PANEL);
        panelEstado.add(new JLabel("Estado:"));
        JComboBox<EstadoEvaluacion> cmbEstado = new JComboBox<>(EstadoEvaluacion.values());
        cmbEstado.setSelectedItem(EstadoEvaluacion.completada);
        panelEstado.add(cmbEstado);

        // ── Botón guardar ─────────────────────────────────────────────
        JButton btnGuardar = crearBoton("💾 Guardar evaluación", ACCENT);
        btnGuardar.addActionListener(e -> {
            Empleado emp  = (Empleado) cmbEmpleado.getSelectedItem();
            Empleado eval = (Empleado) cmbEvaluador.getSelectedItem();
            if (emp == null || eval == null) {
                JOptionPane.showMessageDialog(this, "Selecciona empleado y evaluador.");
                return;
            }
            if (emp.getIdEmpleado().equals(eval.getIdEmpleado())) {
                JOptionPane.showMessageDialog(this, "Un empleado no puede evaluarse a sí mismo.");
                return;
            }
            LocalDate fecha;
            try {
                fecha = LocalDate.parse(txtFecha.getText().trim(), FMT);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Formato de fecha incorrecto (dd/MM/yyyy).");
                return;
            }

            Evaluacion ev = new Evaluacion();
            ev.setEmpleado(emp);
            ev.setEvaluador(eval);
            ev.setFecha(fecha);
            ev.setPeriodo(txtPeriodo.getText().trim());
            ev.setPuntualidad(sliders[0].getValue());
            ev.setAtencionCliente(sliders[1].getValue());
            ev.setTrabajoEquipo(sliders[2].getValue());
            ev.setConocimientoProducto(sliders[3].getValue());
            ev.setHigienePresentacion(sliders[4].getValue());
            ev.setComentarios(txtComentarios.getText().trim());
            ev.setEstado((EstadoEvaluacion) cmbEstado.getSelectedItem());

            EvaluacionService.ResultadoEvaluacion res = service.guardar(ev);
            if (res.isExito()) {
                JOptionPane.showMessageDialog(this,
                    res.getMensaje() + "\nPuntuación: " + ev.getPuntuacionTotal() + " / 100",
                    "Evaluación guardada", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormEvaluacion(sliders, txtPeriodo, txtComentarios);
            } else {
                JOptionPane.showMessageDialog(this, res.getMensaje(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(BG_DARK);
        south.add(panelEstado);
        south.add(btnGuardar);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(BG_DARK);
        center.add(separador("Datos generales"));
        center.add(cabecera);
        center.add(Box.createVerticalStrut(10));
        center.add(separador("Criterios de evaluación  (1 = Muy deficiente   10 = Excelente)"));
        center.add(panelCriterios);
        center.add(Box.createVerticalStrut(10));
        center.add(panelComentarios);

        root.add(new JScrollPane(center), BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        return root;
    }

    // ══════════════════════════════════════════════════════════════════
    // PESTAÑA 2 — HISTORIAL DE EVALUACIONES
    // ══════════════════════════════════════════════════════════════════

    private JPanel crearPanelHistorial() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Selector de empleado
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtro.setBackground(BG_PANEL);
        filtro.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR), new EmptyBorder(8, 12, 8, 12)));
        JComboBox<Empleado> cmbEmp = cmbEmpleados();
        filtro.add(new JLabel("Empleado:"));
        filtro.add(cmbEmp);

        // Tabla
        String[] cols = {"ID", "Fecha", "Periodo", "Evaluador",
                         "Puntualidad", "Atención", "Equipo", "Producto", "Higiene",
                         "Total", "Estado"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(model);
        estilizarTabla(tabla);

        // Panel de resumen
        JLabel lblResumen = new JLabel(" ");
        lblResumen.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblResumen.setForeground(TEXT_MUTED);

        JButton btnCargar = crearBoton("🔍 Cargar historial", ACCENT);
        filtro.add(btnCargar);
        filtro.add(lblResumen);

        btnCargar.addActionListener(e -> {
            Empleado emp = (Empleado) cmbEmp.getSelectedItem();
            if (emp == null) return;
            model.setRowCount(0);
            List<Evaluacion> evals = evCtrl.getEvaluacionesPorEmpleado(emp.getIdEmpleado());
            for (Evaluacion ev : evals) {
                model.addRow(new Object[]{
                    ev.getIdEvaluacion(),
                    ev.getFecha() != null ? ev.getFecha().format(FMT) : "—",
                    ev.getPeriodo() != null ? ev.getPeriodo() : "—",
                    ev.getEvaluador() != null ? ev.getEvaluador().getNombre() + " " + ev.getEvaluador().getApellidos() : "—",
                    ev.getPuntualidad(),
                    ev.getAtencionCliente(),
                    ev.getTrabajoEquipo(),
                    ev.getConocimientoProducto(),
                    ev.getHigienePresentacion(),
                    ev.getPuntuacionTotal(),
                    ev.getEstado() != null ? ev.getEstado().name() : "—"
                });
            }
            if (evals.isEmpty()) {
                lblResumen.setText("Sin evaluaciones registradas.");
            } else {
                double media = evals.stream()
                    .filter(ev -> ev.getPuntuacionTotal() != null)
                    .mapToInt(Evaluacion::getPuntuacionTotal)
                    .average().orElse(0);
                lblResumen.setText(String.format(
                    "  %d evaluaciones · Media: %.1f / 100", evals.size(), media));
            }
        });

        root.add(filtro, BorderLayout.NORTH);
        root.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return root;
    }

    // ══════════════════════════════════════════════════════════════════
    // PESTAÑA 3 — OBJETIVOS
    // ══════════════════════════════════════════════════════════════════

    private JPanel crearPanelObjetivos() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        // ── Formulario nuevo objetivo ─────────────────────────────────
        JPanel formObj = new JPanel(new GridBagLayout());
        formObj.setBackground(BG_PANEL);
        formObj.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(12, 14, 12, 14)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(4, 6, 4, 6);
        gc.anchor  = GridBagConstraints.WEST;
        gc.fill    = GridBagConstraints.HORIZONTAL;

        JComboBox<Empleado> cmbEmpObj        = cmbEmpleados();
        JComboBox<Empleado> cmbResponsable   = cmbEmpleados();
        JTextField txtTitulo    = new JTextField(28);
        JTextField txtFechaLim  = new JTextField(10);
        JTextArea  txtDescObj   = new JTextArea(2, 28);
        txtDescObj.setLineWrap(true);
        txtDescObj.setWrapStyleWord(true);

        addFila(formObj, gc, 0, "Empleado:",       cmbEmpObj);
        addFila(formObj, gc, 1, "Responsable:",    cmbResponsable);
        addFila(formObj, gc, 2, "Título:",          txtTitulo);
        addFila(formObj, gc, 3, "Fecha límite:",    txtFechaLim);
        gc.gridx = 0; gc.gridy = 4; gc.weightx = 0;
        formObj.add(new JLabel("Descripción:"), gc);
        gc.gridx = 1; gc.gridy = 4; gc.weightx = 1; gc.gridwidth = 2;
        formObj.add(new JScrollPane(txtDescObj), gc);
        gc.gridwidth = 1;

        JButton btnCrearObj = crearBoton("➕ Crear objetivo", ACCENT);
        gc.gridx = 1; gc.gridy = 5;
        formObj.add(btnCrearObj, gc);

        // ── Tabla de objetivos ────────────────────────────────────────
        JPanel panelTabla = new JPanel(new BorderLayout(4, 4));
        panelTabla.setBackground(BG_DARK);

        JPanel filtroObj = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        filtroObj.setBackground(BG_PANEL);
        filtroObj.setBorder(new EmptyBorder(4, 8, 4, 8));
        JComboBox<Empleado> cmbFiltroObj = cmbEmpleados();
        JButton btnCargarObj = crearBoton("🔍 Cargar", ACCENT);
        filtroObj.add(new JLabel("Ver objetivos de:"));
        filtroObj.add(cmbFiltroObj);
        filtroObj.add(btnCargarObj);

        String[] colsObj = {"ID", "Título", "Fecha límite", "Progreso (%)", "Estado", "Observaciones"};
        DefaultTableModel modelObj = new DefaultTableModel(colsObj, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
        };
        JTable tablaObj = new JTable(modelObj);
        estilizarTabla(tablaObj);

        // Columna progreso editable → actualizar al confirmar
        tablaObj.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (col == 3 && row >= 0) {
                try {
                    int idObj = (int) modelObj.getValueAt(row, 0);
                    int prog  = Integer.parseInt(modelObj.getValueAt(row, 3).toString());
                    evCtrl.getObjetivosPorEmpleado(
                        ((Empleado) cmbFiltroObj.getSelectedItem()).getIdEmpleado()
                    ).stream()
                        .filter(o -> o.getIdObjetivo().equals(idObj))
                        .findFirst()
                        .ifPresent(o -> {
                            service.actualizarProgreso(o, prog);
                            modelObj.setValueAt(o.getEstado().name(), row, 4);
                        });
                } catch (NumberFormatException ignored) {}
            }
        });

        btnCargarObj.addActionListener(e -> {
            Empleado emp = (Empleado) cmbFiltroObj.getSelectedItem();
            if (emp == null) return;
            modelObj.setRowCount(0);
            evCtrl.getObjetivosPorEmpleado(emp.getIdEmpleado()).forEach(o ->
                modelObj.addRow(new Object[]{
                    o.getIdObjetivo(),
                    o.getTitulo(),
                    o.getFechaLimite() != null ? o.getFechaLimite().format(FMT) : "—",
                    o.getProgreso() != null ? o.getProgreso() : 0,
                    o.getEstado() != null ? o.getEstado().name() : "—",
                    o.getObservaciones() != null ? o.getObservaciones() : ""
                })
            );
        });

        btnCrearObj.addActionListener(e -> {
            Empleado emp  = (Empleado) cmbEmpObj.getSelectedItem();
            Empleado resp = (Empleado) cmbResponsable.getSelectedItem();
            String titulo = txtTitulo.getText().trim();
            if (emp == null || titulo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Empleado y título son obligatorios.");
                return;
            }
            LocalDate fechaLim = null;
            if (!txtFechaLim.getText().isBlank()) {
                try { fechaLim = LocalDate.parse(txtFechaLim.getText().trim(), FMT); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Fecha límite incorrecta."); return; }
            }
            Objetivo obj = new Objetivo();
            obj.setEmpleado(emp);
            obj.setResponsable(resp);
            obj.setTitulo(titulo);
            obj.setDescripcion(txtDescObj.getText().trim());
            obj.setFechaLimite(fechaLim);
            obj.setFechaCreacion(LocalDate.now());
            obj.setEstado(EstadoObjetivo.pendiente);

            EvaluacionService.ResultadoEvaluacion res = service.guardarObjetivo(obj);
            JOptionPane.showMessageDialog(this, res.getMensaje(),
                res.isExito() ? "OK" : "Error",
                res.isExito() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            if (res.isExito()) {
                txtTitulo.setText("");
                txtFechaLim.setText("");
                txtDescObj.setText("");
            }
        });

        panelTabla.add(filtroObj, BorderLayout.NORTH);
        panelTabla.add(new JScrollPane(tablaObj), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            formObj, panelTabla);
        split.setDividerLocation(220);
        split.setResizeWeight(0.35);

        root.add(split, BorderLayout.CENTER);
        return root;
    }

    // ══════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════

    private JComboBox<Empleado> cmbEmpleados() {
        JComboBox<Empleado> cmb = new JComboBox<>();
        empleados.forEach(cmb::addItem);
        cmb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> l, Object v, int i, boolean sel, boolean focus) {
                super.getListCellRendererComponent(l, v, i, sel, focus);
                if (v instanceof Empleado e)
                    setText(e.getIdEmpleado() + " — " + e.getNombre() + " " + e.getApellidos());
                setFont(new Font("SansSerif", Font.PLAIN, 12));
                return this;
            }
        });
        cmb.setPreferredSize(new Dimension(320, 28));
        return cmb;
    }

    private JSlider crearSlider() {
        JSlider s = new JSlider(1, 10, 5);
        s.setMajorTickSpacing(1);
        s.setPaintTicks(true);
        s.setPaintLabels(true);
        s.setSnapToTicks(true);
        s.setBackground(BG_PANEL);
        return s;
    }

    private JButton crearBoton(String texto, Color bg) {
        JButton b = new JButton(texto);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        return b;
    }

    private JPanel crearBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(BG_DARK);
        JButton cerrar = crearBoton("Cerrar", new Color(113, 128, 150));
        cerrar.addActionListener(e -> dispose());
        p.add(cerrar);
        return p;
    }

    private void addFila(JPanel p, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        p.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        p.add(field, gc);
    }

    private JPanel separador(String titulo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(4, 0, 4, 0));
        JLabel lbl = new JLabel("  " + titulo);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(TEXT_MUTED);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(237, 242, 247));
        lbl.setBorder(new EmptyBorder(3, 6, 3, 6));
        p.add(lbl);
        return p;
    }

    private void estilizarTabla(JTable t) {
        t.setRowHeight(24);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        t.setSelectionBackground(MainUI.ROW_HOVER);
        t.setGridColor(BORDER_COLOR);
        t.setIntercellSpacing(new Dimension(6, 2));
        t.setFillsViewportHeight(true);
    }

    private void limpiarFormEvaluacion(JSlider[] sliders, JTextField periodo, JTextArea comentarios) {
        for (JSlider s : sliders) s.setValue(5);
        periodo.setText("");
        comentarios.setText("");
    }
}
