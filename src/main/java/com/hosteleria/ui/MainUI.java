package com.hosteleria.ui;

import com.hosteleria.controller.HosteleriaController;
import com.hosteleria.model.*;
import com.hosteleria.service.*;
import com.hosteleria.util.HibernateUtil;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class MainUI extends JFrame {

    // ── Paleta ────────────────────────────────────────────────────────
    static final Color BG_DARK      = new Color(247, 250, 252);
    static final Color BG_PANEL     = new Color(255, 255, 255);
    static final Color BG_CARD      = new Color(255, 247, 237);
    static final Color ACCENT       = new Color(237, 137, 54);
    static final Color ACCENT2      = new Color(254, 215, 170);
    static final Color ACCENT_RED   = new Color(246, 100, 100);
    static final Color TEXT_PRIMARY = new Color(26,  32,  44);
    static final Color TEXT_MUTED   = new Color(113, 128, 150);
    static final Color BORDER_COLOR = new Color(226, 232, 240);
    static final Color ROW_EVEN     = new Color(255, 250, 245);
    static final Color ROW_ODD      = new Color(255, 255, 255);
    static final Color ROW_HOVER    = new Color(254, 235, 200);

    // ── Servicios ─────────────────────────────────────────────────────
    private final HosteleriaController       ctrl          = new HosteleriaController();
    private final GeneradorDocumentosService generadorDocs = new GeneradorDocumentosService();
    private final EmpleadoGestionService     gestionEmp    = new EmpleadoGestionService();
    private final EstadisticasEmpleadoService estadisticas = new EstadisticasEmpleadoService();
    private final PresenciaService           presencia     = new PresenciaService();

    private JLabel statusLabel;


    public MainUI() {
        setTitle("Hostelería — Panel de Gestión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1300, 820);
        setMinimumSize(new Dimension(950, 620));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));
        add(crearHeader(),          BorderLayout.NORTH);
        add(crearCuerpoPrincipal(), BorderLayout.CENTER);
        add(crearStatusBar(),       BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { HibernateUtil.shutdown(); }
        });
    }

    
    // ACCIONES
    

    private void cerrarSesion() {
        if (JOptionPane.showConfirmDialog(this, "¿Cerrar sesión?", "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().cerrarSesion();
            dispose();
            LoginDialog dlg = new LoginDialog(null);
            dlg.setVisible(true);
            if (dlg.isLoginOk()) new MainUI().setVisible(true);
            else System.exit(0);
        }
    }

    private void abrirRegistroUsuario()  { new RegistroDialog(this).setVisible(true); }

    private void abrirAltaEmpleado() {
        new AltaEmpleadoDialog(this, gestionEmp).setVisible(true); recargarTodo();
    }
    private void abrirBajaEmpleado() {
        new BajaEmpleadoDialog(this, gestionEmp, ctrl).setVisible(true); recargarTodo();
    }
    private void abrirEditarEmpleadoPorId() {
        String s = JOptionPane.showInputDialog(this, "ID del empleado a modificar:");
        if (s == null || s.isBlank()) return;
        try {
            int id = Integer.parseInt(s.trim());
            ctrl.getEmpleadoCompleto(id).ifPresentOrElse(
                    emp -> { new EditarEmpleadoDialog(this, ctrl, gestionEmp, emp).setVisible(true); recargarTodo(); },
                    ()  -> JOptionPane.showMessageDialog(this, "Empleado no encontrado."));
        } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "ID no válido."); }
    }
    private void abrirFichaEmpleadoPorId() {
        String s = JOptionPane.showInputDialog(this, "ID del empleado:");
        if (s == null || s.isBlank()) return;
        try {
            int id = Integer.parseInt(s.trim());
            ctrl.getEmpleadoCompleto(id).ifPresentOrElse(
                    emp -> new EmpleadoFichaDialog(this, ctrl, emp).setVisible(true),
                    ()  -> JOptionPane.showMessageDialog(this, "Empleado no encontrado."));
        } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "ID no válido."); }
    }

    // ── módulo de ausencias ──────────────────────────────
    private void abrirAusencias() {
        new AusenciasDialog(this).setVisible(true);
    }

    private void generarInformeEmpleado() {
        String s = JOptionPane.showInputDialog(this, "ID del empleado:");
        if (s == null || s.isBlank()) return;
        try {
            int id = Integer.parseInt(s.trim()); LocalDate h = LocalDate.now();
            File dir = new File(System.getProperty("user.home"), "Documents");
            if (!dir.exists()) dir = new File(System.getProperty("user.home"));
            JOptionPane.showMessageDialog(this, "Informe generado:\n" +
                    generadorDocs.generarInformeEmpleado(id, h.minusMonths(6), h, dir.toPath()));
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: "+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }
    private void generarCertificado() {
        String s = JOptionPane.showInputDialog(this, "ID del empleado:");
        if (s == null || s.isBlank()) return;
        try {
            int id = Integer.parseInt(s.trim());
            File dir = new File(System.getProperty("user.home"), "Documents");
            if (!dir.exists()) dir = new File(System.getProperty("user.home"));
            JOptionPane.showMessageDialog(this, "Certificado generado:\n" +
                    generadorDocs.generarCertificadoTrabajo(id, dir.toPath()));
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: "+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }
    private void generarResumenPlantilla() {
        try {
            File dir = new File(System.getProperty("user.home"), "Documents");
            if (!dir.exists()) dir = new File(System.getProperty("user.home"));
            JOptionPane.showMessageDialog(this, "Resumen generado:\n" +
                    generadorDocs.generarResumenPlantilla(dir.toPath()));
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: "+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void abrirEstadisticasGlobales()  { new EstadisticasGlobalesDialog(this, estadisticas).setVisible(true); }
    private void abrirEstadisticasEmpleado() {
        String s = JOptionPane.showInputDialog(this, "ID del empleado:");
        if (s == null || s.isBlank()) return;
        try {
            int id = Integer.parseInt(s.trim()); LocalDate h = LocalDate.now();
            new EstadisticasEmpleadoDialog(this, estadisticas, id, h.minusMonths(6), h).setVisible(true);
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "ID no válido."); }
    }

    // Presencia
    private void abrirNuevoTurno() {
        List<Empleado> emps = ctrl.getEmpleadosActivosConAreaYPuesto();
        if (emps.isEmpty()) { JOptionPane.showMessageDialog(this, "No hay empleados activos."); return; }
        TurnoDialog dlg = new TurnoDialog(this, presencia, emps, null);
        dlg.setVisible(true);
        if (dlg.isGuardado()) recargarTodo();
    }
    private void abrirIncidencias() {
        new IncidenciasPresenciaDialog(this, presencia, ctrl).setVisible(true);
    }

    
    // HEADER
   

    private JPanel crearHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(BG_PANEL);
        h.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));
        h.setPreferredSize(new Dimension(0, 56));
        JLabel titulo = new JLabel("  ◈  HOSTELERÍA MANAGER");
        titulo.setFont(new Font("Monospaced", Font.BOLD, 15));
        titulo.setForeground(ACCENT);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        h.add(titulo, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        right.setBackground(BG_PANEL);
        JButton btnU = new JButton("👤");
        btnU.setFont(new Font("SansSerif", Font.PLAIN, 20));
        btnU.setBackground(BG_PANEL); btnU.setForeground(ACCENT);
        btnU.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btnU.setFocusPainted(false); btnU.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPopupMenu menu = new JPopupMenu();
        Usuario u = SessionManager.getInstance().haySesionActiva()
                ? SessionManager.getInstance().getUsuarioActual() : null;
        if (u != null) {
            JMenuItem mp = new JMenuItem("Mi perfil — "+u.getNombre());
            mp.setEnabled(false); menu.add(mp); menu.addSeparator();
        }
        JMenuItem rec = new JMenuItem("↻ Recargar datos"); rec.addActionListener(e -> recargarTodo());
        menu.add(rec);
        if (SessionManager.getInstance().esAdminOGerente()) {
            JMenuItem reg = new JMenuItem("Registrar usuario");
            reg.addActionListener(e -> abrirRegistroUsuario()); menu.add(reg);
        }
        menu.addSeparator();
        JMenuItem cs = new JMenuItem("Cerrar sesión"); cs.addActionListener(e -> cerrarSesion());
        menu.add(cs);
        btnU.addActionListener(e -> menu.show(btnU, 0, btnU.getHeight()));
        right.add(btnU);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    
    // CARDS
    

    private static final String CARD_EMPLEADOS     = "empleados";
    private static final String CARD_CONTRATACION  = "contratacion";
    private static final String CARD_HORARIOS      = "horarios";
    private static final String CARD_AUSENCIAS     = "ausencias";       
    private static final String CARD_GRUPOS        = "grupos";
    private static final String CARD_COMUNICACION  = "comunicacion";
    private static final String CARD_DOCUMENTACION = "documentacion";
    private static final String CARD_ESTADISTICAS  = "estadisticas";
    private static final String CARD_DATOS         = "datos";

    private CardLayout  cardLayout;
    private JPanel      panelContenido;
    private String      cardVisible   = CARD_EMPLEADOS;
    private JTabbedPane tabsEmpleados;
    private JTabbedPane tabsHorarios;

    private JPanel crearCuerpoPrincipal() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 0));
        cuerpo.setBackground(BG_DARK);
        panelContenido = new JPanel(cardLayout = new CardLayout());
        panelContenido.setBackground(BG_DARK);
        panelContenido.add(crearPanelEmpleadosModulo(),  CARD_EMPLEADOS);
        panelContenido.add(crearPanelContratacion(),     CARD_CONTRATACION);
        panelContenido.add(crearPanelHorarios(),         CARD_HORARIOS);
        panelContenido.add(crearPanelAusenciasModulo(),  CARD_AUSENCIAS);   // NUEVO
        panelContenido.add(crearPanelGrupos(),           CARD_GRUPOS);
        panelContenido.add(crearPanelComunicacion(),     CARD_COMUNICACION);
        panelContenido.add(crearPanelDocumentacion(),    CARD_DOCUMENTACION);
        panelContenido.add(crearPanelEstadisticas(),     CARD_ESTADISTICAS);
        panelContenido.add(crearPanelDatosGenerales(),   CARD_DATOS);
        cuerpo.add(crearMenuLateral(), BorderLayout.WEST);
        cuerpo.add(panelContenido,     BorderLayout.CENTER);
        cardLayout.show(panelContenido, CARD_EMPLEADOS);
        return cuerpo;
    }

    // ══════════════════════════════════════════════════════════════════
    // SIDEBAR
    // ══════════════════════════════════════════════════════════════════

    private JPanel crearMenuLateral() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(BG_PANEL);
        sb.setPreferredSize(new Dimension(228, 0));
        sb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 12, 16, 12)));

        sb.add(sec("Gestión de empleados", new Object[][]{
                {"📋 Contratación (alta/baja)", CARD_CONTRATACION, -1},
                {"✏️ Modificar empleado",        CARD_CONTRATACION, -2},
                {"📂 Ficha de empleado",          CARD_EMPLEADOS,    -3}
        }));
        sb.add(Box.createVerticalStrut(14));
        sb.add(sec("Control de presencia", new Object[][]{
                {"🗓️ Control de presencia",          CARD_HORARIOS,  0},
                
        }));
        sb.add(Box.createVerticalStrut(14));
        // ── NUEVO: sección Ausencias y Permisos ───────────────────────
        sb.add(sec("Ausencias y permisos", new Object[][]{
                {"🏖️ Ausencias y permisos",        CARD_AUSENCIAS, -1}
        }));
        sb.add(Box.createVerticalStrut(14));
        // ─────────────────────────────────────────────────────────────
        sb.add(sec("Grupos de trabajo", new Object[][]{
                {"🏢 Áreas", CARD_GRUPOS, -1}
        }));
        sb.add(Box.createVerticalStrut(14));
        sb.add(sec("Comunicación", new Object[][]{
                {"💬 Chat", CARD_COMUNICACION, -1}
        }));
        sb.add(Box.createVerticalStrut(14));
        sb.add(sec("Documentación", new Object[][]{
                {"📄 Informes y certificados", CARD_DOCUMENTACION, -1}
        }));
        sb.add(Box.createVerticalStrut(14));
        sb.add(sec("Estadísticas", new Object[][]{
                {"📊 Estadísticas", CARD_ESTADISTICAS, -1}
        }));
        sb.add(Box.createVerticalStrut(14));
        sb.add(sec("Datos generales", new Object[][]{
                {"🗄️ Todas las tablas - Desarrollo -", CARD_DATOS, -1}
        }));
        sb.add(Box.createVerticalGlue());
        return sb;
    }

    private JPanel sec(String titulo, Object[][] items) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_PANEL);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(5));
        for (Object[] item : items) {
            p.add(btnLateral((String)item[0], (String)item[1], (Integer)item[2]));
            p.add(Box.createVerticalStrut(1));
        }
        return p;
    }

    private JButton btnLateral(String texto, String card, int idx) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(TEXT_PRIMARY); btn.setBackground(BG_PANEL);
        btn.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btn.addActionListener(e -> {
            if (CARD_CONTRATACION.equals(card) && idx == -2)  { abrirEditarEmpleadoPorId(); return; }
            if (CARD_EMPLEADOS.equals(card)    && idx == -3)  { abrirFichaEmpleadoPorId();  return; }
            if (CARD_HORARIOS.equals(card)     && idx == -10) { abrirNuevoTurno();          return; }
            if (CARD_HORARIOS.equals(card)     && idx == -11) { abrirIncidencias();         return; }
            if (CARD_AUSENCIAS.equals(card)    && idx == -1)  { abrirAusencias();           return; } // NUEVO
            cardVisible = card;
            cardLayout.show(panelContenido, card);
            if (CARD_EMPLEADOS.equals(card) && tabsEmpleados != null && idx >= 0)
                tabsEmpleados.setSelectedIndex(idx);
            if (CARD_HORARIOS.equals(card)  && tabsHorarios  != null && idx >= 0)
                tabsHorarios.setSelectedIndex(idx);
        });
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(BG_CARD); }
            @Override public void mouseExited (MouseEvent e) { btn.setBackground(BG_PANEL); }
        });
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════
    // PANEL HORARIOS — CONTROL DE PRESENCIA
    // ══════════════════════════════════════════════════════════════════

    private JPanel crearPanelHorarios() {
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        acciones.setBackground(BG_PANEL);
        acciones.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JButton btnNuevo      = boton("➕ Nuevo turno",               ACCENT);
        JButton btnIncidencias= boton("🚨 Incidencias",               ACCENT_RED);
        JButton btnCerrarAb   = boton("⚡ Cerrar fichajes abiertos",  new Color(113, 128, 150));

        btnNuevo.addActionListener(e       -> abrirNuevoTurno());
        btnIncidencias.addActionListener(e -> abrirIncidencias());
        btnCerrarAb.addActionListener(e    -> {
            int n = presencia.cerrarFichajesAbiertos(LocalDate.now());
            JOptionPane.showMessageDialog(this, n + " fichaje(s) cerrado(s).");
            recargarTodo();
        });

        JLabel wifiLabel = new JLabel("  📡 Fichaje WiFi → ws://servidor:8080/fichaje");
        wifiLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        wifiLabel.setForeground(TEXT_MUTED);

        acciones.add(btnNuevo); acciones.add(btnIncidencias); acciones.add(btnCerrarAb);
        acciones.add(Box.createHorizontalStrut(16)); acciones.add(wifiLabel);

        tabsHorarios = new JTabbedPane(JTabbedPane.TOP);
        tabsHorarios.setBackground(BG_DARK);

        List<Empleado> emps = ctrl.getEmpleadosActivosConAreaYPuesto();
        CuadrantePanel cpSem = new CuadrantePanel(presencia, emps);
        cpSem.setOnCambio(this::recargarTodo);
        JPanel wSem = new JPanel(new BorderLayout());
        wSem.setBackground(BG_DARK);
        wSem.add(cpSem, BorderLayout.CENTER);
        tabsHorarios.addTab("🗓️ Cuadrante semanal", wSem);

        CuadrantePanel cpMes = new CuadrantePanel(presencia, emps);
        cpMes.setOnCambio(this::recargarTodo);
        JPanel wMes = new JPanel(new BorderLayout());
        wMes.setBackground(BG_DARK);
        wMes.add(cpMes, BorderLayout.CENTER);
        tabsHorarios.addTab("📅 Cuadrante mensual", wMes);

        tabsHorarios.addTab("📋 Fichajes", crearPanelFichajes());

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG_DARK);
        wrap.add(acciones,     BorderLayout.NORTH);
        wrap.add(tabsHorarios, BorderLayout.CENTER);
        return wrap;
    }

    // ══════════════════════════════════════════════════════════════════
    // PANEL AUSENCIAS Y PERMISOS 
    // ══════════════════════════════════════════════════════════════════

    private JPanel crearPanelAusenciasModulo() {
        JPanel w = new JPanel(new BorderLayout());
        w.setBackground(BG_DARK);
        w.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        // Cabecera
        JLabel titulo = new JLabel("🏖️  Ausencias y Permisos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(TEXT_PRIMARY);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel subtitulo = new JLabel("Gestiona vacaciones, permisos retribuidos/no retribuidos y bajas médicas (IT).");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitulo.setForeground(TEXT_MUTED);

        JPanel hdr = new JPanel();
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.setOpaque(false);
        hdr.add(titulo);
        hdr.add(subtitulo);
        hdr.add(Box.createVerticalStrut(28));

        // Tarjetas de acceso rápido
        JPanel tarjetas = new JPanel(new GridLayout(1, 3, 20, 0));
        tarjetas.setOpaque(false);
        tarjetas.setMaximumSize(new Dimension(900, 140));

        tarjetas.add(tarjeta("📝 Solicitar",
                "Vacaciones, permisos\nretribuidos y no retribuidos",
                new Color(237, 137, 54)));
        tarjetas.add(tarjeta("✅ Aprobar / Rechazar",
                "Gestión de solicitudes\npendientes por responsable",
                new Color(56, 161, 105)));
        tarjetas.add(tarjeta("🏥 Bajas IT",
                "Registro y seguimiento\nde bajas médicas",
                new Color(66, 153, 225)));

        // Botón principal
        JButton btnAbrir = new JButton("Abrir módulo completo →");
        btnAbrir.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAbrir.setBackground(ACCENT);
        btnAbrir.setForeground(Color.WHITE);
        btnAbrir.setFocusPainted(false);
        btnAbrir.setBorder(BorderFactory.createEmptyBorder(12, 28, 12, 28));
        btnAbrir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAbrir.addActionListener(e -> abrirAusencias());
        btnAbrir.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnAbrir.setBackground(ACCENT.brighter()); }
            @Override public void mouseExited (MouseEvent e) { btnAbrir.setBackground(ACCENT); }
        });

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setOpaque(false);
        centro.add(hdr);
        centro.add(tarjetas);
        centro.add(Box.createVerticalStrut(28));
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(btnAbrir);
        centro.add(btnWrap);

        w.add(centro, BorderLayout.NORTH);
        return w;
    }

    private JPanel tarjeta(String titulo, String desc, Color color) {
        JPanel t = new JPanel(new BorderLayout(0, 8));
        t.setBackground(BG_PANEL);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JPanel barra = new JPanel();
        barra.setBackground(color);
        barra.setPreferredSize(new Dimension(0, 4));

        JLabel lTitulo = new JLabel(titulo);
        lTitulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lTitulo.setForeground(TEXT_PRIMARY);

        JLabel lDesc = new JLabel("<html>" + desc.replace("\n", "<br>") + "</html>");
        lDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lDesc.setForeground(TEXT_MUTED);

        t.add(barra,   BorderLayout.NORTH);
        t.add(lTitulo, BorderLayout.CENTER);
        t.add(lDesc,   BorderLayout.SOUTH);
        return t;
    }

    
    // RESTO DE PANELES
    

    private JPanel crearPanelContratacion() {
        JPanel acc = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        acc.setBackground(BG_DARK);
        JButton ba = boton("➕ Alta empleado",  ACCENT2);   ba.addActionListener(e -> abrirAltaEmpleado());
        JButton bb = boton("➖ Baja empleado",  ACCENT_RED); bb.addActionListener(e -> abrirBajaEmpleado());
        acc.add(ba); acc.add(bb);
        JPanel w = new JPanel(new BorderLayout()); w.setBackground(BG_DARK);
        w.add(acc, BorderLayout.NORTH); w.add(crearPanelEmpleados(), BorderLayout.CENTER);
        return w;
    }

    private JPanel crearPanelGrupos() {
        JPanel w = new JPanel(new BorderLayout()); w.setBackground(BG_DARK);
        w.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        w.add(crearPanelAreas(), BorderLayout.CENTER); return w;
    }

    private JPanel crearPanelComunicacion() {
        JPanel c = new JPanel(new GridBagLayout()); c.setBackground(BG_DARK);
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(12,12,12,12);
        JLabel t = new JLabel("💬 Comunicación"); t.setFont(new Font("SansSerif",Font.BOLD,22)); t.setForeground(ACCENT);
        JLabel d = new JLabel("<html><center>El chat está disponible en la app móvil (ws://servidor:8080/chat).<br>" +
                "El fichaje WiFi llega por ws://servidor:8080/fichaje.</center></html>");
        d.setFont(new Font("SansSerif",Font.PLAIN,14)); d.setForeground(TEXT_MUTED);
        JButton b = boton("Info del servidor WebSocket", ACCENT);
        b.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Endpoints WebSocket activos:\n  /chat    → Chat entre empleados\n  /fichaje → Fichaje desde app móvil vía WiFi"));
        g.gridy=0; c.add(t,g); g.gridy=1; c.add(d,g); g.gridy=2; c.add(b,g);
        JPanel w = new JPanel(new BorderLayout()); w.setBackground(BG_DARK); w.add(c,BorderLayout.CENTER); return w;
    }

    private JPanel crearPanelEmpleadosModulo() {
        tabsEmpleados = new JTabbedPane(JTabbedPane.TOP);
        tabsEmpleados.setBackground(BG_DARK);
        tabsEmpleados.addTab("Empleados",    crearPanelEmpleados());
        tabsEmpleados.addTab("Áreas",        crearPanelAreas());
        tabsEmpleados.addTab("Puestos",      crearPanelPuestos());
        tabsEmpleados.addTab("Fichajes",     crearPanelFichajes());
        JPanel w = new JPanel(new BorderLayout()); w.setBackground(BG_DARK);
        w.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        w.add(tabsEmpleados, BorderLayout.CENTER); return w;
    }

    private JPanel crearPanelDatosGenerales() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BG_DARK);
        tabs.addTab("👥 Empleados",   crearPanelEmpleados());
        tabs.addTab("🏢 Áreas",       crearPanelAreas());
        tabs.addTab("💼 Puestos",      crearPanelPuestos());
        tabs.addTab("🕐 Turnos",       crearPanelTurnos());
        tabs.addTab("📋 Fichajes",     crearPanelFichajes());
        tabs.addTab("💰 Propinas",     crearPanelPropinas());
        tabs.addTab("📄 Nóminas",      crearPanelNominas());
        tabs.addTab("⭐ Evaluaciones", crearPanelEvaluaciones());
        tabs.addTab("🎓 Formación",    crearPanelFormaciones());
        tabs.addTab("🏖️ Ausencias",    crearPanelAusencias());
        tabs.addTab("👤 Usuarios",     crearPanelUsuarios());
        JPanel hdr = new JPanel(new BorderLayout()); hdr.setBackground(BG_DARK);
        hdr.setBorder(BorderFactory.createEmptyBorder(16,24,8,24));
        JLabel lbl = new JLabel("🗄️  Datos generales — todas las tablas");
        lbl.setFont(new Font("SansSerif",Font.BOLD,17)); lbl.setForeground(TEXT_PRIMARY);
        hdr.add(lbl,BorderLayout.WEST);
        JPanel w = new JPanel(new BorderLayout()); w.setBackground(BG_DARK);
        w.add(hdr,BorderLayout.NORTH); w.add(tabs,BorderLayout.CENTER); return w;
    }

    private JPanel crearPanelDocumentacion() {
        JPanel c = new JPanel(new GridBagLayout()); c.setBackground(BG_DARK);
        c.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        GridBagConstraints g = new GridBagConstraints(); g.insets=new Insets(10,10,10,10);
        g.anchor=GridBagConstraints.NORTHWEST; g.fill=GridBagConstraints.HORIZONTAL;
        JButton b1=boton("📄 Informe de empleado (PDF)",    ACCENT); b1.addActionListener(e->generarInformeEmpleado());
        JButton b2=boton("📋 Certificado de trabajo (PDF)", ACCENT); b2.addActionListener(e->generarCertificado());
        JButton b3=boton("📊 Resumen plantilla (PDF)",      ACCENT); b3.addActionListener(e->generarResumenPlantilla());
        g.gridy=0; c.add(b1,g); g.gridy=1; c.add(b2,g); g.gridy=2; c.add(b3,g);
        JPanel w = new JPanel(new BorderLayout()); w.setBackground(BG_DARK);
        w.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        w.add(c,BorderLayout.NORTH); return w;
    }

    private JPanel crearPanelEstadisticas() {
        EstadisticasEmpleadoService.EstadisticasGlobalesDTO g = estadisticas.estadisticasGlobales();
        JPanel c = new JPanel(new BorderLayout(24,24)); c.setBackground(BG_DARK);
        c.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        JPanel pv = new PanelGraficosEstadisticas(g); pv.setPreferredSize(new Dimension(0,280));
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.LEFT,12,8)); bot.setBackground(BG_DARK);
        JButton bg = boton("Ver estadísticas globales",  ACCENT); bg.addActionListener(e->abrirEstadisticasGlobales());
        JButton be = boton("Estadísticas por empleado",  ACCENT); be.addActionListener(e->abrirEstadisticasEmpleado());
        bot.add(bg); bot.add(be);
        JPanel sur = new JPanel(new BorderLayout()); sur.setBackground(BG_DARK); sur.add(bot,BorderLayout.NORTH);
        c.add(pv,BorderLayout.CENTER); c.add(sur,BorderLayout.SOUTH); return c;
    }

   
    // PANELES DE TABLA
    

    private JPanel crearPanelEmpleados() {
        List<Empleado> d = ctrl.getEmpleadosConAreaYPuesto();
        String[] cols = {"ID","Nombre","Apellidos","DNI","Email","Teléfono","Área","Puesto","Salario Base","Contrato","Estado"};
        Object[][] rows = new Object[d.size()][cols.length];
        for (int i=0;i<d.size();i++) { Empleado e=d.get(i);
            rows[i]=new Object[]{e.getIdEmpleado(),e.getNombre(),e.getApellidos(),e.getDni(),e.getEmail(),e.getTelefono(),
                    e.getArea()!=null?e.getArea().getNombre():"—",e.getPuesto()!=null?e.getPuesto().getTitulo():"—",
                    e.getSalarioBase()+" €",e.getTipoContrato(),e.getEstado()}; }
        return tabla("Empleados registrados",cols,rows,d.size(),new int[]{40,90,110,80,160,95,120,140,90,100,90});
    }
    private JPanel crearPanelAreas() {
        List<Area> d=ctrl.getAllAreas(); String[] cols={"ID","Nombre","Tipo","Estado","Descripción"};
        Object[][] rows=new Object[d.size()][cols.length];
        for (int i=0;i<d.size();i++) { Area a=d.get(i);
            rows[i]=new Object[]{a.getIdArea(),a.getNombre(),a.getTipo(),a.getEstado(),
                    a.getDescripcion()!=null?a.getDescripcion():"—"}; }
        return tabla("Áreas / Departamentos",cols,rows,d.size(),new int[]{40,160,110,80,350});
    }
    private JPanel crearPanelPuestos() {
        List<Puesto> d=ctrl.getAllPuestos(); String[] cols={"ID","Título","Categoría","Nivel","Salario Base","Certificación"};
        Object[][] rows=new Object[d.size()][cols.length];
        for (int i=0;i<d.size();i++) { Puesto p=d.get(i);
            rows[i]=new Object[]{p.getIdPuesto(),p.getTitulo(),p.getCategoria(),p.getNivel(),
                    p.getSalarioBase()+" €",Boolean.TRUE.equals(p.getRequiereCertificacion())?"Sí":"No"}; }
        return tabla("Puestos de trabajo",cols,rows,d.size(),new int[]{40,180,110,80,100,90});
    }
    private JPanel crearPanelTurnos() {
        List<Turno> d=ctrl.getTurnosConEmpleado();
        String[] cols={"ID","Empleado","Fecha","Inicio","Fin","Tipo","Área","Horas","Estado"};
        Object[][] rows=new Object[d.size()][cols.length];
        for (int i=0;i<d.size();i++) { Turno t=d.get(i);
            rows[i]=new Object[]{t.getIdTurno(),
                    t.getEmpleado().getNombre()+" "+t.getEmpleado().getApellidos(),
                    t.getFecha(),t.getHoraInicio(),t.getHoraFin(),t.getTipoTurno(),
                    t.getAreaAsignada()!=null?t.getAreaAsignada():"—",t.getHorasTrabajadas(),t.getEstado()}; }
        return tabla("Turnos programados",cols,rows,d.size(),new int[]{40,170,90,70,70,80,140,60,90});
    }
    private JPanel crearPanelFichajes() {
        List<Fichaje> d=ctrl.getFichajesConEmpleadoConTurno();
        String[] cols={"ID","Empleado","Fecha","Entrada","Salida","H.Trabajadas","H.Extra","Retraso(min)","Observaciones"};
        Object[][] rows=new Object[d.size()][cols.length];
        for (int i=0;i<d.size();i++) { Fichaje f=d.get(i);
            rows[i]=new Object[]{f.getIdFichaje(),
                    f.getEmpleado().getNombre()+" "+f.getEmpleado().getApellidos(),
                    f.getFecha(),f.getHoraEntrada(),f.getHoraSalida(),
                    f.getHorasTrabajadas(),f.getHorasExtra(),f.getRetrasoMinutos(),
                    f.getObservaciones()!=null?f.getObservaciones():"—"}; }
        JPanel panel = new JPanel(new BorderLayout(0,0)); panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        JPanel hdr=new JPanel(new BorderLayout()); hdr.setBackground(BG_DARK);
        hdr.setBorder(BorderFactory.createEmptyBorder(0,0,14,0));
        JLabel lT=new JLabel("Registro de fichajes"); lT.setFont(new Font("SansSerif",Font.BOLD,17)); lT.setForeground(TEXT_PRIMARY);
        JLabel lN=new JLabel(d.size()+" registros"); lN.setFont(new Font("Monospaced",Font.PLAIN,12)); lN.setForeground(ACCENT2);
        hdr.add(lT,BorderLayout.WEST); hdr.add(lN,BorderLayout.EAST);
        panel.add(hdr,BorderLayout.NORTH);
        DefaultTableModel model=new DefaultTableModel(rows,cols){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable t=new JTable(model); estilizarTabla(t,new int[]{40,170,90,70,70,90,70,90,200});
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable tbl,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(tbl,v,sel,foc,row,col);
                Object rm=tbl.getModel().getValueAt(row,7);
                int ret=rm instanceof Integer?(Integer)rm:0;
                setBackground(sel?ROW_HOVER:ret>=30?new Color(254,178,178):ret>=15?new Color(254,215,170):(row%2==0?ROW_EVEN:ROW_ODD));
                setForeground(sel?Color.WHITE:TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(0,8,0,8)); return this;
            }
        });
        JScrollPane sc=new JScrollPane(t); sc.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(sc,BorderLayout.CENTER); return panel;
    }
    private JPanel crearPanelPropinas() {
        List<Propina> d=ctrl.getPropinasConEmpleado(); String[] cols={"ID","Empleado","Fecha","Turno","Importe","Tipo","Método"};
        Object[][] rows=new Object[d.size()][cols.length];
        for (int i=0;i<d.size();i++) { Propina p=d.get(i);
            rows[i]=new Object[]{p.getIdPropina(),
                    p.getEmpleado().getNombre()+" "+p.getEmpleado().getApellidos(),
                    p.getFecha(),p.getTurno(),p.getImporte()+" €",p.getTipo(),p.getMetodoPago()}; }
        return tabla("Registro de propinas",cols,rows,d.size(),new int[]{40,170,90,80,90,110,110});
    }
    private JPanel crearPanelNominas() {
        List<Nomina> d=ctrl.getNominasConEmpleado();
        String[] cols={"ID","Empleado","Mes","Año","S.Base","H.Extra","Propinas","S.Social","IRPF","Neto","Estado"};
        Object[][] rows=new Object[d.size()][cols.length];
        for (int i=0;i<d.size();i++) { Nomina n=d.get(i);
            rows[i]=new Object[]{n.getIdNomina(),
                    n.getEmpleado().getNombre()+" "+n.getEmpleado().getApellidos(),
                    n.getMes(),n.getAnio(),n.getSalarioBase()+" €",n.getHorasExtra()+" €",n.getPropinas()+" €",
                    n.getSeguridadSocial()+" €",n.getIrpf()+" €",n.getTotalNeto()+" €",n.getEstado()}; }
        return tabla("Nóminas",cols,rows,d.size(),new int[]{40,160,45,50,90,80,80,80,70,90,80});
    }
    private JPanel crearPanelEvaluaciones() {
        List<Evaluacion> d=ctrl.getEvaluacionesConEmpleadoYEvaluador();
        String[] cols={"ID","Empleado","Evaluador","Fecha","Periodo","Puntual.","Cliente","Equipo","Conocim.","Higiene","Total"};
        Object[][] rows=new Object[d.size()][cols.length];
        for (int i=0;i<d.size();i++) { Evaluacion e=d.get(i);
            rows[i]=new Object[]{e.getIdEvaluacion(),
                    e.getEmpleado().getNombre()+" "+e.getEmpleado().getApellidos(),
                    e.getEvaluador().getNombre()+" "+e.getEvaluador().getApellidos(),
                    e.getFecha(),e.getPeriodo(),e.getPuntualidad(),e.getAtencionCliente(),
                    e.getTrabajoEquipo(),e.getConocimientoProducto(),e.getHigienePresentacion(),e.getPuntuacionTotal()}; }
        return tabla("Evaluaciones",cols,rows,d.size(),new int[]{40,160,160,90,80,65,65,65,65,65,55});
    }
    private JPanel crearPanelFormaciones() {
        List<Formacion> d=ctrl.getFormacionesConEmpleado();
        String[] cols={"ID","Empleado","Curso","Tipo","F.Inicio","F.Fin","Horas","Certificado","Caduca"};
        Object[][] rows=new Object[d.size()][cols.length];
        for (int i=0;i<d.size();i++) { Formacion f=d.get(i);
            rows[i]=new Object[]{f.getIdFormacion(),
                    f.getEmpleado().getNombre()+" "+f.getEmpleado().getApellidos(),
                    f.getCurso(),f.getTipo(),f.getFechaInicio()!=null?f.getFechaInicio():"—",
                    f.getFechaFin()!=null?f.getFechaFin():"—",
                    f.getDuracionHoras()!=null?f.getDuracionHoras()+"h":"—",
                    Boolean.TRUE.equals(f.getCertificado())?"Sí":"No",
                    f.getFechaCaducidad()!=null?f.getFechaCaducidad():"—"}; }
        return tabla("Formación",cols,rows,d.size(),new int[]{40,160,220,140,85,85,55,80,85});
    }
    private JPanel crearPanelAusencias() {
        List<Ausencia> d=ctrl.getAusenciasConEmpleado();
        String[] cols={"ID","Empleado","Tipo","F.Inicio","F.Fin","Días","Estado","Retribuido","Observaciones"};
        Object[][] rows=new Object[d.size()][cols.length];
        for (int i=0;i<d.size();i++) { Ausencia a=d.get(i);
            rows[i]=new Object[]{a.getIdAusencia(),
                    a.getEmpleado().getNombre()+" "+a.getEmpleado().getApellidos(),
                    a.getTipo(),a.getFechaInicio(),a.getFechaFin(),
                    a.getDiasTotales()!=null?a.getDiasTotales():"—",a.getEstado(),
                    Boolean.TRUE.equals(a.getRetribuido())?"Sí":"No",
                    a.getObservaciones()!=null?a.getObservaciones():"—"}; }
        return tabla("Ausencias",cols,rows,d.size(),new int[]{40,170,140,85,85,45,100,70,230});
    }
    private JPanel crearPanelUsuarios() {
        List<Usuario> d=ctrl.getUsuariosConEmpleado();
        String[] cols={"ID","Nombre","Username","Email","Rol","PC","Activo","Empleado vinculado","Último acceso"};
        Object[][] rows=new Object[d.size()][cols.length];
        for (int i=0;i<d.size();i++) { Usuario u=d.get(i);
            rows[i]=new Object[]{u.getIdUsuario(),u.getNombre(),u.getUsername(),u.getEmail(),u.getRol(),
                    u.isAccesoPC()?"✓":"✗",u.isActivo()?"✓":"✗",
                    u.getEmpleado()!=null?u.getEmpleado().getNombre()+" "+u.getEmpleado().getApellidos():"—",
                    u.getUltimoAcceso()!=null?u.getUltimoAcceso():"Nunca"}; }
        return tabla("Usuarios",cols,rows,d.size(),new int[]{40,140,100,200,90,50,50,180,140});
    }

    // ── Fábrica de tablas ──────────────────────────────────────────────

    private JPanel tabla(String titulo, String[] cols, Object[][] rows, int total, int[] anchos) {
        JPanel panel=new JPanel(new BorderLayout(0,0)); panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        JPanel hdr=new JPanel(new BorderLayout()); hdr.setBackground(BG_DARK);
        hdr.setBorder(BorderFactory.createEmptyBorder(0,0,14,0));
        JLabel lT=new JLabel(titulo); lT.setFont(new Font("SansSerif",Font.BOLD,17)); lT.setForeground(TEXT_PRIMARY);
        JLabel lN=new JLabel(total+" registros"); lN.setFont(new Font("Monospaced",Font.PLAIN,12)); lN.setForeground(ACCENT2);
        hdr.add(lT,BorderLayout.WEST); hdr.add(lN,BorderLayout.EAST);
        panel.add(hdr,BorderLayout.NORTH);
        DefaultTableModel model=new DefaultTableModel(rows,cols){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable t=new JTable(model); estilizarTabla(t,anchos);
        JScrollPane sc=new JScrollPane(t); sc.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        sc.getViewport().setBackground(BG_DARK); panel.add(sc,BorderLayout.CENTER); return panel;
    }

    private void estilizarTabla(JTable t, int[] anchos) {
        t.setBackground(BG_DARK); t.setForeground(TEXT_PRIMARY);
        t.setFont(new Font("SansSerif",Font.PLAIN,12)); t.setRowHeight(28);
        t.setShowVerticalLines(false); t.setShowHorizontalLines(true);
        t.setGridColor(BORDER_COLOR); t.setSelectionBackground(ROW_HOVER);
        t.setSelectionForeground(Color.WHITE);
        t.setIntercellSpacing(new Dimension(12,0)); t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        t.setFillsViewportHeight(true);
        JTableHeader h=t.getTableHeader(); h.setBackground(BG_CARD); h.setForeground(ACCENT);
        h.setFont(new Font("SansSerif",Font.BOLD,12)); h.setPreferredSize(new Dimension(0,34));
        h.setBorder(BorderFactory.createMatteBorder(0,0,1,0,ACCENT)); h.setReorderingAllowed(false);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable tbl,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(tbl,v,sel,foc,row,col);
                setBackground(sel?ROW_HOVER:(row%2==0?ROW_EVEN:ROW_ODD));
                setForeground(sel?Color.WHITE:TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(0,8,0,8));
                setFont(new Font("SansSerif",Font.PLAIN,12)); return this;
            }
        });
        for (int i=0;i<anchos.length&&i<t.getColumnCount();i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
    }


    private JPanel crearStatusBar() {
        JPanel bar=new JPanel(new BorderLayout()); bar.setBackground(BG_PANEL);
        bar.setBorder(new MatteBorder(1,0,0,0,BORDER_COLOR)); bar.setPreferredSize(new Dimension(0,28));
        statusLabel=new JLabel("  ● Conectado a la base de datos");
        statusLabel.setFont(new Font("Monospaced",Font.PLAIN,11)); statusLabel.setForeground(ACCENT2);
        bar.add(statusLabel,BorderLayout.WEST);
        JLabel v=new JLabel("Hostelería Manager v1.1  ");
        v.setFont(new Font("Monospaced",Font.PLAIN,11)); v.setForeground(TEXT_MUTED);
        bar.add(v,BorderLayout.EAST); return bar;
    }

    // ── Botón estilizado ──────────────────────────────────────────────

    private JButton boton(String texto, Color color) {
        JButton b=new JButton(texto);
        b.setFont(new Font("SansSerif",Font.BOLD,12));
        b.setForeground(BG_DARK); b.setBackground(color);
        b.setBorder(BorderFactory.createEmptyBorder(6,16,6,16));
        b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter(){
            @Override public void mouseEntered(MouseEvent e){b.setBackground(color.brighter());}
            @Override public void mouseExited (MouseEvent e){b.setBackground(color);}
        }); return b;
    }

    
    // METODO DE RECARGA
    

    private void recargarTodo() {
        statusLabel.setForeground(new Color(246,173,85));
        statusLabel.setText("  ↻ Recargando datos...");
        SwingUtilities.invokeLater(() -> {
            panelContenido.removeAll();
            panelContenido.add(crearPanelEmpleadosModulo(),  CARD_EMPLEADOS);
            panelContenido.add(crearPanelContratacion(),     CARD_CONTRATACION);
            panelContenido.add(crearPanelHorarios(),         CARD_HORARIOS);
            panelContenido.add(crearPanelAusenciasModulo(),  CARD_AUSENCIAS);   // NUEVO
            panelContenido.add(crearPanelGrupos(),           CARD_GRUPOS);
            panelContenido.add(crearPanelComunicacion(),     CARD_COMUNICACION);
            panelContenido.add(crearPanelDocumentacion(),    CARD_DOCUMENTACION);
            panelContenido.add(crearPanelEstadisticas(),     CARD_ESTADISTICAS);
            panelContenido.add(crearPanelDatosGenerales(),   CARD_DATOS);
            cardLayout.show(panelContenido, cardVisible);
            panelContenido.revalidate(); panelContenido.repaint();
            statusLabel.setForeground(ACCENT2);
            statusLabel.setText("  ● Datos actualizados correctamente");
        });
    }


    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);
            if (login.isLoginOk()) new MainUI().setVisible(true);
            else System.exit(0);
        });
    }
}