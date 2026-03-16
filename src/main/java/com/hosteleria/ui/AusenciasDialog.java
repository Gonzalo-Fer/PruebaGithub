package com.hosteleria.ui;

import com.hosteleria.controller.HosteleriaController;
import com.hosteleria.model.Ausencia;
import com.hosteleria.model.Ausencia.EstadoAusencia;
import com.hosteleria.model.Ausencia.TipoAusencia;
import com.hosteleria.model.Empleado;
import com.hosteleria.model.Usuario;
import com.hosteleria.service.AusenciasService;
import com.hosteleria.service.AusenciasService.ResumenDiasAusencia;
import com.hosteleria.service.SessionManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AusenciasDialog extends JDialog {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final HosteleriaController ctrl          = new HosteleriaController();
    private final AusenciasService     service       = new AusenciasService();
    private final Usuario              usuarioActual;

    // ── Colores ───────────────────────────────────────────────────────
    private static final Color ACCENT       = MainUI.ACCENT;
    private static final Color BG_DARK      = MainUI.BG_DARK;
    private static final Color BG_PANEL     = MainUI.BG_PANEL;
    private static final Color TEXT_PRIMARY = MainUI.TEXT_PRIMARY;
    private static final Color TEXT_MUTED   = MainUI.TEXT_MUTED;
    private static final Color BORDER_COLOR = MainUI.BORDER_COLOR;

    // ── Lista maestra de empleados (cargada una vez) ──────────────────
    private final List<Empleado> todosEmpleados;

    public AusenciasDialog(Frame parent) {
        super(parent, "🏖️ Ausencias y Permisos", true);
        this.usuarioActual  = SessionManager.getInstance().getUsuarioActual();
        this.todosEmpleados = ctrl.getAllEmpleados();

        setSize(960, 700);
        setMinimumSize(new Dimension(800, 540));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabs.addTab("📝 Solicitar",          crearPanelSolicitar());
        tabs.addTab("✅ Aprobar / Rechazar",  crearPanelAprobar());
        tabs.addTab("🏥 Bajas IT",            crearPanelBajasIT());
        tabs.addTab("📅 Calendario",          crearPanelCalendario());
        tabs.addTab("📊 Días disponibles",    crearPanelDiasDisponibles());

        if (usuarioActual != null && usuarioActual.getRol() == Usuario.Rol.EMPLEADO)
            tabs.setEnabledAt(1, false);

        add(tabs, BorderLayout.CENTER);
        add(crearBotonCerrar(), BorderLayout.SOUTH);
    }

    // ══════════════════════════════════════════════════════════════════
    // HELPERS: selector de empleado reutilizable
    // ══════════════════════════════════════════════════════════════════

    /**
     * Crea un JComboBox con el formato "ID — Nombre Apellidos"
     * igual al de la imagen, filtrando por los estados indicados.
     */
    private JComboBox<Empleado> cmbEmpleados(Empleado.EstadoEmpleado... excluir) {
        JComboBox<Empleado> cmb = new JComboBox<>();
        java.util.Set<Empleado.EstadoEmpleado> excluidos =
                excluir.length > 0 ? java.util.EnumSet.copyOf(java.util.Arrays.asList(excluir))
                        : java.util.EnumSet.noneOf(Empleado.EstadoEmpleado.class);

        todosEmpleados.stream()
                .filter(e -> !excluidos.contains(e.getEstado()))
                .forEach(cmb::addItem);

        // Renderer: "ID — Nombre Apellidos"
        cmb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Empleado e) {
                    setText(e.getIdEmpleado() + " — " + e.getNombre() + " " + e.getApellidos());
                }
                setFont(new Font("SansSerif", Font.PLAIN, 12));
                return this;
            }
        });
        cmb.setPreferredSize(new Dimension(340, 28));
        return cmb;
    }

    /**
     * Crea un JComboBox con las bajas médicas activas (aprobadas) de un empleado
     * para seleccionar a qué baja se registra el alta médica.
     */
    private JComboBox<Ausencia> cmbBajasActivas() {
        JComboBox<Ausencia> cmb = new JComboBox<>();
        poblarCmbBajas(cmb);
        cmb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Ausencia a) {
                    setText(a.getIdAusencia()
                            + " — " + a.getEmpleado().getNombre() + " " + a.getEmpleado().getApellidos()
                            + "  (baja desde " + a.getFechaInicio().format(FMT) + ")");
                } else if (value == null) {
                    setText("— Sin bajas activas —");
                }
                setFont(new Font("SansSerif", Font.PLAIN, 12));
                return this;
            }
        });
        cmb.setPreferredSize(new Dimension(400, 28));
        return cmb;
    }

    /** Carga (o recarga) las bajas médicas activas en el combo. */
    private void poblarCmbBajas(JComboBox<Ausencia> cmb) {
        cmb.removeAllItems();
        ctrl.getAusenciasConEmpleado().stream()
                .filter(a -> a.getTipo()  == TipoAusencia.baja_medica
                        && a.getEstado() == EstadoAusencia.aprobado
                        && a.getFechaAltaMedica() == null)
                .forEach(cmb::addItem);
    }

    /** Spinner de fecha que devuelve LocalDate. Parte de la fecha indicada. */
    private JSpinner spinnerFecha(LocalDate inicial) {
        SpinnerDateModel model = new SpinnerDateModel(
                java.sql.Date.valueOf(inicial),
                null, null,
                java.util.Calendar.DAY_OF_MONTH
        );
        JSpinner sp = new JSpinner(model);
        sp.setEditor(new JSpinner.DateEditor(sp, "dd/MM/yyyy"));
        sp.setPreferredSize(new Dimension(140, 28));
        return sp;
    }

    /** Extrae LocalDate de un JSpinner de fecha. */
    private LocalDate fechaDe(JSpinner sp) {
        java.util.Date d = (java.util.Date) sp.getValue();
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    // ══════════════════════════════════════════════════════════════════
    // PESTAÑA 1 — SOLICITAR
    // ══════════════════════════════════════════════════════════════════

    private JPanel crearPanelSolicitar() {
        JPanel p = panelBase();
        p.setBorder(new EmptyBorder(20, 24, 20, 24));
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = gbc();

        JComboBox<Empleado>   cmbEmp  = cmbEmpleados(Empleado.EstadoEmpleado.baja_definitiva);
        JComboBox<TipoAusencia> cmbTipo = new JComboBox<>(new TipoAusencia[]{
                TipoAusencia.vacaciones,
                TipoAusencia.permiso_retribuido,
                TipoAusencia.permiso_no_retribuido,
                TipoAusencia.permiso_personal,
                TipoAusencia.asuntos_propios,
                TipoAusencia.maternidad_paternidad
        });
        JSpinner spInicio = spinnerFecha(LocalDate.now());
        JSpinner spFin    = spinnerFecha(LocalDate.now().plusDays(1));
        JTextArea txtObs  = new JTextArea(3, 30);
        txtObs.setLineWrap(true); txtObs.setWrapStyleWord(true);
        JLabel lblRes = new JLabel(" ");
        lblRes.setFont(new Font("SansSerif", Font.BOLD, 12));

        int row = 0;
        fila(p, g, row++, "Empleado:",         cmbEmp);
        fila(p, g, row++, "Tipo de ausencia:", cmbTipo);
        fila(p, g, row++, "Fecha inicio:",     spInicio);
        fila(p, g, row++, "Fecha fin:",        spFin);
        g.gridx = 0; g.gridy = row;   p.add(etiqueta("Observaciones:"), g);
        g.gridx = 1; g.gridy = row++; p.add(new JScrollPane(txtObs), g);

        g.gridx = 0; g.gridy = row; g.gridwidth = 2;
        JButton btnEnviar = boton("Enviar solicitud", ACCENT, Color.WHITE);
        p.add(btnEnviar, g);
        g.gridy = ++row; p.add(lblRes, g);

        btnEnviar.addActionListener(e -> {
            Empleado emp     = (Empleado) cmbEmp.getSelectedItem();
            TipoAusencia t   = (TipoAusencia) cmbTipo.getSelectedItem();
            if (emp == null) { lblRes.setForeground(Color.RED); lblRes.setText("Selecciona un empleado."); return; }
            var res = service.solicitarAusencia(emp.getIdEmpleado(), t,
                    fechaDe(spInicio), fechaDe(spFin), txtObs.getText().trim());
            lblRes.setForeground(res.ok ? new Color(34, 139, 34) : Color.RED);
            lblRes.setText(res.mensaje);
        });

        return p;
    }

    // ══════════════════════════════════════════════════════════════════
    // PESTAÑA 2 — APROBAR / RECHAZAR
    // ══════════════════════════════════════════════════════════════════

    private JPanel crearPanelAprobar() {
        JPanel p = panelBase();
        p.setLayout(new BorderLayout(0, 8));
        p.setBorder(new EmptyBorder(12, 16, 12, 16));

        String[] cols = {"ID", "Empleado", "Tipo", "Desde", "Hasta", "Días", "Estado"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = crearTabla(model);
        JLabel lblMsg = new JLabel(" ");

        JButton btnCargar   = boton("↻ Cargar pendientes",  ACCENT,                      Color.WHITE);
        JButton btnAprobar  = boton("✅ Aprobar",            new Color(34, 139, 34),      Color.WHITE);
        JButton btnRechazar = boton("❌ Rechazar",           new Color(200, 50, 50),      Color.WHITE);

        btnCargar.addActionListener(e -> {
            model.setRowCount(0);
            ctrl.getAusenciasPendientes().forEach(a -> model.addRow(new Object[]{
                    a.getIdAusencia(),
                    a.getEmpleado().getIdEmpleado() + " — "
                            + a.getEmpleado().getNombre() + " " + a.getEmpleado().getApellidos(),
                    a.getTipo().name().replace("_", " "),
                    a.getFechaInicio().format(FMT),
                    a.getFechaFin().format(FMT),
                    a.getDiasTotales(),
                    a.getEstado().name()
            }));
        });

        btnAprobar.addActionListener(e -> {
            int row = tabla.getSelectedRow();
            if (row < 0) { lblMsg.setForeground(Color.RED); lblMsg.setText("Selecciona una fila."); return; }
            var res = service.aprobarAusencia((int) model.getValueAt(row, 0), usuarioActual);
            lblMsg.setForeground(res.ok ? new Color(34, 139, 34) : Color.RED);
            lblMsg.setText(res.mensaje);
            if (res.ok) model.removeRow(row);
        });

        btnRechazar.addActionListener(e -> {
            int row = tabla.getSelectedRow();
            if (row < 0) { lblMsg.setForeground(Color.RED); lblMsg.setText("Selecciona una fila."); return; }
            String motivo = JOptionPane.showInputDialog(this, "Motivo del rechazo:");
            if (motivo == null) return;
            var res = service.rechazarAusencia((int) model.getValueAt(row, 0), usuarioActual, motivo);
            lblMsg.setForeground(res.ok ? new Color(34, 139, 34) : Color.RED);
            lblMsg.setText(res.mensaje);
            if (res.ok) model.removeRow(row);
        });

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnCargar); botones.add(btnAprobar); botones.add(btnRechazar); botones.add(lblMsg);

        p.add(botones,                BorderLayout.NORTH);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════
    // PESTAÑA 3 — BAJAS IT
    // ══════════════════════════════════════════════════════════════════

    private JPanel crearPanelBajasIT() {
        JPanel p = panelBase();
        p.setBorder(new EmptyBorder(20, 24, 20, 24));
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = gbc();

        // ── Sección: Registrar baja ───────────────────────────────────
        JComboBox<Empleado> cmbEmp      = cmbEmpleados(Empleado.EstadoEmpleado.baja_definitiva);
        JSpinner            spInicio    = spinnerFecha(LocalDate.now());
        JTextField          txtNumBaja  = new JTextField();
        JTextArea           txtObs      = new JTextArea(2, 30);
        txtObs.setLineWrap(true); txtObs.setWrapStyleWord(true);
        JLabel lblMsg = new JLabel(" ");
        lblMsg.setFont(new Font("SansSerif", Font.BOLD, 12));

        int row = 0;
        g.gridx = 0; g.gridy = row; g.gridwidth = 2;
        JLabel secBaja = new JLabel("REGISTRAR BAJA MÉDICA");
        secBaja.setFont(new Font("SansSerif", Font.BOLD, 11));
        secBaja.setForeground(TEXT_MUTED);
        p.add(secBaja, g);

        g.gridwidth = 1;
        fila(p, g, ++row, "Empleado:",          cmbEmp);
        fila(p, g, ++row, "Fecha inicio baja:", spInicio);
        fila(p, g, ++row, "Número IT/baja SS:", txtNumBaja);
        g.gridx = 0; g.gridy = ++row; p.add(etiqueta("Observaciones:"), g);
        g.gridx = 1;                  p.add(new JScrollPane(txtObs), g);

        g.gridx = 0; g.gridy = ++row; g.gridwidth = 2;
        JButton btnBaja = boton("🏥 Registrar baja IT", new Color(200, 80, 50), Color.WHITE);
        p.add(btnBaja, g);

        // ── Separador ────────────────────────────────────────────────
        g.gridy = ++row; p.add(new JSeparator(), g);

        // ── Sección: Registrar alta ───────────────────────────────────
        g.gridy = ++row;
        JLabel secAlta = new JLabel("REGISTRAR ALTA MÉDICA");
        secAlta.setFont(new Font("SansSerif", Font.BOLD, 11));
        secAlta.setForeground(TEXT_MUTED);
        p.add(secAlta, g);

        // Selector de baja activa en lugar de campo de texto con ID
        JComboBox<Ausencia> cmbBaja  = cmbBajasActivas();
        JSpinner            spAlta   = spinnerFecha(LocalDate.now());

        g.gridwidth = 1;
        fila(p, g, ++row, "Baja a cerrar:",  cmbBaja);
        fila(p, g, ++row, "Fecha alta:",     spAlta);

        g.gridx = 0; g.gridy = ++row; g.gridwidth = 2;
        JButton btnAlta = boton("✅ Registrar alta médica", new Color(34, 139, 34), Color.WHITE);
        p.add(btnAlta, g);

        g.gridy = ++row; p.add(lblMsg, g);

        // ── Listeners ────────────────────────────────────────────────
        btnBaja.addActionListener(e -> {
            Empleado emp = (Empleado) cmbEmp.getSelectedItem();
            if (emp == null) { lblMsg.setForeground(Color.RED); lblMsg.setText("Selecciona un empleado."); return; }
            var res = service.registrarBajaMedica(emp.getIdEmpleado(), fechaDe(spInicio),
                    txtNumBaja.getText().trim(), txtObs.getText().trim());
            lblMsg.setForeground(res.ok ? new Color(34, 139, 34) : Color.RED);
            lblMsg.setText(res.ok
                    ? res.mensaje + " | ID baja: " + res.ausencia.getIdAusencia()
                    : res.mensaje);
            if (res.ok) {
                // Recargar el combo de bajas activas
                refrescarCmbBajas(cmbBaja);
            }
        });

        btnAlta.addActionListener(e -> {
            Ausencia baja = (Ausencia) cmbBaja.getSelectedItem();
            if (baja == null) { lblMsg.setForeground(Color.RED); lblMsg.setText("No hay bajas activas."); return; }
            var res = service.registrarAltaMedica(baja.getIdAusencia(), fechaDe(spAlta));
            lblMsg.setForeground(res.ok ? new Color(34, 139, 34) : Color.RED);
            lblMsg.setText(res.mensaje);
            if (res.ok) refrescarCmbBajas(cmbBaja);
        });

        return p;
    }

    /** Recarga el contenido del combo de bajas activas sin recrearlo. */
    private void refrescarCmbBajas(JComboBox<Ausencia> cmb) {
        poblarCmbBajas(cmb);
    }

    // ══════════════════════════════════════════════════════════════════
    // PESTAÑA 4 — CALENDARIO
    // ══════════════════════════════════════════════════════════════════

    private JPanel crearPanelCalendario() {
        JPanel p = panelBase();
        p.setLayout(new BorderLayout(0, 8));
        p.setBorder(new EmptyBorder(12, 16, 12, 16));

        LocalDate hoy     = LocalDate.now();
        JSpinner  spinMes = new JSpinner(new SpinnerNumberModel(hoy.getMonthValue(), 1, 12, 1));
        JSpinner  spinAno = new JSpinner(new SpinnerNumberModel(hoy.getYear(), 2000, 2100, 1));
        JButton   btnVer  = boton("Ver mes", ACCENT, Color.WHITE);

        String[] cols = {"Empleado", "Tipo", "Desde", "Hasta", "Días", "Estado"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = crearTabla(model);

        btnVer.addActionListener(e -> {
            LocalDate desde = LocalDate.of((int) spinAno.getValue(), Month.of((int) spinMes.getValue()), 1);
            LocalDate hasta = desde.withDayOfMonth(desde.lengthOfMonth());
            model.setRowCount(0);
            service.getCalendarioAusencias(desde, hasta).forEach(a -> model.addRow(new Object[]{
                    a.getEmpleado().getIdEmpleado() + " — "
                            + a.getEmpleado().getNombre() + " " + a.getEmpleado().getApellidos(),
                    a.getTipo().name().replace("_", " "),
                    a.getFechaInicio().format(FMT),
                    a.getFechaFin().format(FMT),
                    a.getDiasTotales(),
                    a.getEstado().name()
            }));
        });

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);
        filtros.add(new JLabel("Mes:")); filtros.add(spinMes);
        filtros.add(new JLabel("Año:")); filtros.add(spinAno);
        filtros.add(btnVer);

        p.add(filtros,                BorderLayout.NORTH);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════
    // PESTAÑA 5 — DÍAS DISPONIBLES
    // ══════════════════════════════════════════════════════════════════

    private JPanel crearPanelDiasDisponibles() {
        JPanel p = panelBase();
        p.setLayout(new BorderLayout(0, 8));
        p.setBorder(new EmptyBorder(12, 16, 12, 16));

        JSpinner spinAno = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1));
        JButton  btnVer  = boton("Ver resumen", ACCENT, Color.WHITE);

        String[] cols = {"Empleado", "Vac. totales", "Vac. usadas", "Vac. disponibles",
                "Perm. retribuidos", "Perm. no retribuidos", "Días baja IT"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = crearTabla(model);

        btnVer.addActionListener(e -> {
            int anio = (int) spinAno.getValue();
            model.setRowCount(0);
            todosEmpleados.stream()
                    .filter(emp -> emp.getEstado() != Empleado.EstadoEmpleado.baja_definitiva)
                    .forEach(emp -> {
                        ResumenDiasAusencia r = service.getResumenDias(emp.getIdEmpleado(), anio);
                        model.addRow(new Object[]{
                                emp.getIdEmpleado() + " — " + emp.getNombre() + " " + emp.getApellidos(),
                                r.diasVacacionesTotales,
                                r.diasVacacionesUsados,
                                r.diasVacacionesDisponibles,
                                r.diasPermisosRetribuidos,
                                r.diasPermisosNoRetribuidos,
                                r.diasBajaMedica
                        });
                    });
        });

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);
        filtros.add(new JLabel("Año:")); filtros.add(spinAno); filtros.add(btnVer);

        p.add(filtros,                BorderLayout.NORTH);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════
    // HELPERS UI
    // ══════════════════════════════════════════════════════════════════

    private JPanel panelBase() {
        JPanel p = new JPanel();
        p.setBackground(BG_DARK);
        return p;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(TEXT_PRIMARY);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        return l;
    }

    private JButton boton(String texto, Color bg, Color fg) {
        JButton b = new JButton(texto);
        b.setBackground(bg); b.setForeground(fg);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JTable crearTabla(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(26);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setGridColor(BORDER_COLOR);
        return t;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(6, 6, 6, 6);
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.anchor  = GridBagConstraints.WEST;
        return g;
    }

    private void fila(JPanel p, GridBagConstraints g, int row, String label, JComponent campo) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1;
        p.add(etiqueta(label), g);
        g.gridx = 1;
        p.add(campo, g);
    }

    private JPanel crearBotonCerrar() {
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        sur.setBackground(BG_PANEL);
        JButton b = boton("Cerrar", new Color(113, 128, 150), Color.WHITE);
        b.addActionListener(e -> dispose());
        sur.add(b);
        return sur;
    }
}