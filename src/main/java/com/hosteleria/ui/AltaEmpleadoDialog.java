package com.hosteleria.ui;

import com.hosteleria.controller.HosteleriaController;
import com.hosteleria.model.*;
import com.hosteleria.service.EmpleadoGestionService;
import com.hosteleria.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Alta de empleado con pestañas opcionales para historial, contratos y formación.
 * Las pestañas 2-4 se habilitan automáticamente tras completar el alta.
 */
public class AltaEmpleadoDialog extends JDialog {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color BG     = MainUI.BG_DARK;
    private static final Color ACCENT = MainUI.ACCENT;
    private static final Color FG     = MainUI.TEXT_PRIMARY;
    private static final Color BORDER = MainUI.BORDER_COLOR;

    private final EmpleadoGestionService servicio;
    private final HosteleriaController   ctrl;

    private JTabbedPane tabs;
    private Empleado    empleadoCreado; // se rellena tras el alta exitosa

    public AltaEmpleadoDialog(Frame parent, EmpleadoGestionService servicio) {
        super(parent, "Alta de empleado", true);
        this.servicio = servicio;
        this.ctrl     = new HosteleriaController();

        setSize(680, 640);
        setMinimumSize(new Dimension(580, 520));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabs.addTab("👤 Datos personales",  tabAlta());
        tabs.addTab("📋 Historial laboral", tabHistorial());
        tabs.addTab("📄 Contratos",         tabContratos());
        tabs.addTab("🎓 Formación",         tabFormacion());

        // Pestañas 1-3 deshabilitadas hasta completar el alta
        for (int i = 1; i <= 3; i++) tabs.setEnabledAt(i, false);

        add(tabs, BorderLayout.CENTER);
    }


    // TAB 1 — ALTA


    private JPanel tabAlta() {
        JPanel form = panelForm();
        GridBagConstraints g = gbc();

        JTextField txtNombre    = tf(25), txtApellidos = tf(25);
        JTextField txtDni       = tf(25), txtEmail     = tf(25);
        JTextField txtTelefono  = tf(25), txtNSS       = tf(25);
        JTextField txtSalario   = tf(12);

        JSpinner spFechaNac = spinner(LocalDate.of(1990, 1, 1));

        List<Area> areas = servicio.getAreas();
        JComboBox<Area> cmbArea = new JComboBox<>(areas.toArray(new Area[0]));
        cmbArea.insertItemAt(null, 0); cmbArea.setSelectedIndex(0);
        cmbArea.setRenderer((l,v,i,s,f) -> jlabel(v instanceof Area a ? a.getNombre() : "— Sin área —", l, s));

        List<Puesto> puestos = servicio.getPuestos();
        JComboBox<Puesto> cmbPuesto = new JComboBox<>(puestos.toArray(new Puesto[0]));
        cmbPuesto.setRenderer((l,v,i,s,f) -> jlabel(v instanceof Puesto p ? p.getTitulo() : "", l, s));

        JComboBox<Empleado.TipoContrato> cmbTipo = new JComboBox<>(Empleado.TipoContrato.values());
        JCheckBox chkCarnet = new JCheckBox("Carnet de manipulador");
        chkCarnet.setOpaque(false); chkCarnet.setForeground(FG);

        JLabel lblMsg = msgLabel();

        int r = 0;
        fila(form, g, r++, "Nombre *",          txtNombre);
        fila(form, g, r++, "Apellidos *",        txtApellidos);
        fila(form, g, r++, "DNI *",              txtDni);
        fila(form, g, r++, "Email",              txtEmail);
        fila(form, g, r++, "Teléfono *",         txtTelefono);
        fila(form, g, r++, "Fecha nacimiento *", spFechaNac);
        fila(form, g, r++, "Área",               cmbArea);
        fila(form, g, r++, "Puesto *",           cmbPuesto);
        fila(form, g, r++, "Salario base *",     txtSalario);
        fila(form, g, r++, "Tipo contrato *",    cmbTipo);
        fila(form, g, r++, "Nº Seg. Social",     txtNSS);
        g.gridx=0; g.gridy=r; g.gridwidth=2; form.add(chkCarnet, g); r++;
        g.gridwidth=1;
        g.gridx=0; g.gridy=r; g.gridwidth=2; form.add(lblMsg, g); r++;

        JButton btnAlta = boton("✅ Dar de alta");
        g.gridy=r; form.add(btnAlta, g);

        btnAlta.addActionListener(e -> {
            try {
                Puesto puesto = (Puesto) cmbPuesto.getSelectedItem();
                if (puesto == null) { msg(lblMsg, false, "Selecciona un puesto."); return; }
                Area area = (Area) cmbArea.getSelectedItem();
                BigDecimal salario = new BigDecimal(txtSalario.getText().trim().replace(",","."));
                Empleado.TipoContrato tipo = (Empleado.TipoContrato) cmbTipo.getSelectedItem();

                EmpleadoGestionService.ResultadoAlta res = servicio.altaEmpleado(
                        txtNombre.getText(), txtApellidos.getText(), txtDni.getText(),
                        txtEmail.getText(), txtTelefono.getText(), ld(spFechaNac),
                        area != null ? area.getIdArea() : null,
                        puesto.getIdPuesto(), salario, tipo,
                        txtNSS.getText(), chkCarnet.isSelected());

                if (res.isOk()) {
                    empleadoCreado = res.getEmpleado();
                    msg(lblMsg, true, "✅ Empleado dado de alta (ID: " + empleadoCreado.getIdEmpleado()
                            + "). Puedes añadir historial, contratos y formación.");
                    btnAlta.setEnabled(false);
                    // Habilitar las demás pestañas
                    for (int i = 1; i <= 3; i++) tabs.setEnabledAt(i, true);
                } else {
                    msg(lblMsg, false, res.getMensajeError());
                }
            } catch (Exception ex) {
                msg(lblMsg, false, "Datos incorrectos: " + ex.getMessage());
            }
        });

        return scrollWrap(form);
    }


    // TAB 2 — HISTORIAL LABORAL


    private JPanel tabHistorial() {
        String[] cols = {"Fecha","Área ant.","Área nueva","Puesto ant.","Puesto nuevo","Sal. ant.","Sal. nuevo","Motivo"};
        DefaultTableModel model = tableModel(cols);

        JPanel form = panelForm();
        form.setBorder(titledBorder("➕ Añadir entrada de historial"));
        GridBagConstraints g = gbc();

        JSpinner   spFecha     = spinner(LocalDate.now());
        JTextField txtAAnt     = tf(16), txtANueva   = tf(16);
        JTextField txtPAnt     = tf(16), txtPNuevo   = tf(16);
        JTextField txtSalAnt   = tf(10), txtSalNuevo = tf(10);
        JTextField txtMotivo   = tf(28);
        JLabel     lblMsg      = msgLabel();

        int r = 0;
        fila(form, g, r++, "Fecha",           spFecha);
        fila(form, g, r++, "Área anterior",   txtAAnt);
        fila(form, g, r++, "Área nueva",      txtANueva);
        fila(form, g, r++, "Puesto anterior", txtPAnt);
        fila(form, g, r++, "Puesto nuevo",    txtPNuevo);
        fila(form, g, r++, "Salario ant. €",  txtSalAnt);
        fila(form, g, r++, "Salario nuevo €", txtSalNuevo);
        fila(form, g, r++, "Motivo",          txtMotivo);
        g.gridx=0; g.gridy=r; g.gridwidth=2; form.add(lblMsg, g);
        JButton btn = boton("➕ Guardar entrada"); g.gridy=++r; form.add(btn, g);

        btn.addActionListener(e -> {
            if (empleadoCreado == null) return;
            try {
                HistorialLaboral h = new HistorialLaboral();
                h.setEmpleado(empleadoCreado); h.setFecha(ld(spFecha));
                h.setAreaAnterior(bn(txtAAnt.getText()));    h.setAreaNueva(bn(txtANueva.getText()));
                h.setPuestoAnterior(bn(txtPAnt.getText()));  h.setPuestoNuevo(bn(txtPNuevo.getText()));
                if (!txtSalAnt.getText().isBlank())   h.setSalarioAnterior(new BigDecimal(txtSalAnt.getText().replace(",",".")));
                if (!txtSalNuevo.getText().isBlank()) h.setSalarioNuevo(new BigDecimal(txtSalNuevo.getText().replace(",",".")));
                h.setMotivo(bn(txtMotivo.getText()));
                boolean ok = ctrl.guardarHistorial(h);
                msg(lblMsg, ok, ok ? "✅ Guardado." : "❌ Error al guardar.");
                if (ok) recargar(model, () -> ctrl.getHistorialLaboralPorEmpleado(empleadoCreado.getIdEmpleado()).forEach(hh -> model.addRow(new Object[]{
                        hh.getFecha() != null ? hh.getFecha().format(FMT) : "—",
                        s(hh.getAreaAnterior()), s(hh.getAreaNueva()),
                        s(hh.getPuestoAnterior()), s(hh.getPuestoNuevo()),
                        hh.getSalarioAnterior() != null ? hh.getSalarioAnterior()+" €" : "—",
                        hh.getSalarioNuevo()    != null ? hh.getSalarioNuevo()   +" €" : "—",
                        s(hh.getMotivo())
                })));
            } catch (Exception ex) { msg(lblMsg, false, "Datos inválidos: " + ex.getMessage()); }
        });

        return splitPanel(model, form);
    }


    // TAB 3 — CONTRATOS


    private JPanel tabContratos() {
        String[] cols = {"Tipo","Inicio","Fin previsto","Fin real","Salario","Renovación","Observaciones"};
        DefaultTableModel model = tableModel(cols);

        JPanel form = panelForm();
        form.setBorder(titledBorder("➕ Añadir contrato"));
        GridBagConstraints g = gbc();

        JComboBox<Empleado.TipoContrato> cmbTipo = new JComboBox<>(Empleado.TipoContrato.values());
        JSpinner spInicio  = spinner(LocalDate.now());
        JSpinner spFinPrev = spinner(LocalDate.now().plusYears(1));
        JSpinner spFinReal = spinner(LocalDate.now().plusYears(1)); spFinReal.setEnabled(false);
        JCheckBox chkFin   = new JCheckBox("Registrar fecha fin real"); chkFin.setOpaque(false); chkFin.setForeground(FG);
        chkFin.addActionListener(e -> spFinReal.setEnabled(chkFin.isSelected()));
        JTextField txtSal = tf(12), txtRenov = tf(18), txtObs = tf(24);
        JLabel lblMsg = msgLabel();

        int r = 0;
        fila(form, g, r++, "Tipo contrato",  cmbTipo);
        fila(form, g, r++, "Fecha inicio *", spInicio);
        fila(form, g, r++, "Fin previsto",   spFinPrev);
        g.gridx=0; g.gridy=r; g.gridwidth=2; form.add(chkFin, g); r++;
        g.gridwidth=1;
        fila(form, g, r++, "Fin real",       spFinReal);
        fila(form, g, r++, "Salario base €", txtSal);
        fila(form, g, r++, "Renovación",     txtRenov);
        fila(form, g, r++, "Observaciones",  txtObs);
        g.gridx=0; g.gridy=r; g.gridwidth=2; form.add(lblMsg, g);
        JButton btn = boton("➕ Guardar contrato"); g.gridy=++r; form.add(btn, g);

        btn.addActionListener(e -> {
            if (empleadoCreado == null) return;
            try {
                Contrato c = new Contrato();
                c.setEmpleado(empleadoCreado);
                c.setTipoContrato((Empleado.TipoContrato) cmbTipo.getSelectedItem());
                c.setFechaInicio(ld(spInicio)); c.setFechaFinPrevista(ld(spFinPrev));
                if (chkFin.isSelected()) c.setFechaFinReal(ld(spFinReal));
                if (!txtSal.getText().isBlank()) c.setSalarioBase(new BigDecimal(txtSal.getText().replace(",",".")));
                c.setRenovacion(bn(txtRenov.getText())); c.setObservaciones(bn(txtObs.getText()));
                boolean ok = ctrl.guardarContrato(c);
                msg(lblMsg, ok, ok ? "✅ Guardado." : "❌ Error al guardar.");
                if (ok) recargar(model, () -> ctrl.getContratosPorEmpleado(empleadoCreado.getIdEmpleado()).forEach(cc -> model.addRow(new Object[]{
                        cc.getTipoContrato(),
                        cc.getFechaInicio()      != null ? cc.getFechaInicio().format(FMT)      : "—",
                        cc.getFechaFinPrevista() != null ? cc.getFechaFinPrevista().format(FMT) : "—",
                        cc.getFechaFinReal()     != null ? cc.getFechaFinReal().format(FMT)     : "—",
                        cc.getSalarioBase()      != null ? cc.getSalarioBase()+" €"             : "—",
                        s(cc.getRenovacion()), s(cc.getObservaciones())
                })));
            } catch (Exception ex) { msg(lblMsg, false, "Datos inválidos: " + ex.getMessage()); }
        });

        return splitPanel(model, form);
    }


    // TAB 4 — FORMACIÓN


    private JPanel tabFormacion() {
        String[] cols = {"Curso","Tipo","Inicio","Fin","Horas","Institución","Certificado","Caducidad"};
        DefaultTableModel model = tableModel(cols);

        JPanel form = panelForm();
        form.setBorder(titledBorder("➕ Añadir formación"));
        GridBagConstraints g = gbc();

        JTextField txtCurso = tf(24);
        JComboBox<Formacion.TipoFormacion> cmbTipo = new JComboBox<>(Formacion.TipoFormacion.values());
        JSpinner spIni = spinner(LocalDate.now()), spFin = spinner(LocalDate.now().plusMonths(1));
        JTextField txtHoras = tf(6), txtInst = tf(20);
        JCheckBox chkCert = new JCheckBox("Certificado obtenido"); chkCert.setOpaque(false); chkCert.setForeground(FG);
        JSpinner spCad = spinner(LocalDate.now().plusYears(2)); spCad.setEnabled(false);
        chkCert.addActionListener(e -> spCad.setEnabled(chkCert.isSelected()));
        JLabel lblMsg = msgLabel();

        int r = 0;
        fila(form, g, r++, "Curso *",        txtCurso);
        fila(form, g, r++, "Tipo *",          cmbTipo);
        fila(form, g, r++, "Fecha inicio",    spIni);
        fila(form, g, r++, "Fecha fin",       spFin);
        fila(form, g, r++, "Horas",           txtHoras);
        fila(form, g, r++, "Institución",     txtInst);
        g.gridx=0; g.gridy=r; g.gridwidth=2; form.add(chkCert, g); r++;
        g.gridwidth=1;
        fila(form, g, r++, "Fecha caducidad", spCad);
        g.gridx=0; g.gridy=r; g.gridwidth=2; form.add(lblMsg, g);
        JButton btn = boton("➕ Guardar formación"); g.gridy=++r; form.add(btn, g);

        btn.addActionListener(e -> {
            if (empleadoCreado == null) return;
            if (txtCurso.getText().isBlank()) { msg(lblMsg, false, "El nombre del curso es obligatorio."); return; }
            try {
                Formacion f = new Formacion();
                f.setEmpleado(empleadoCreado); f.setCurso(txtCurso.getText().trim());
                f.setTipo((Formacion.TipoFormacion) cmbTipo.getSelectedItem());
                f.setFechaInicio(ld(spIni)); f.setFechaFin(ld(spFin));
                if (!txtHoras.getText().isBlank()) f.setDuracionHoras(Integer.parseInt(txtHoras.getText().trim()));
                f.setInstitucion(bn(txtInst.getText()));
                f.setCertificado(chkCert.isSelected());
                if (chkCert.isSelected()) f.setFechaCaducidad(ld(spCad));
                boolean ok = persistir(f);
                msg(lblMsg, ok, ok ? "✅ Guardado." : "❌ Error al guardar.");
                if (ok) recargar(model, () -> ctrl.getFormacionesConEmpleado().stream()
                        .filter(ff -> ff.getEmpleado().getIdEmpleado().equals(empleadoCreado.getIdEmpleado()))
                        .forEach(ff -> model.addRow(new Object[]{
                                ff.getCurso(), ff.getTipo(),
                                ff.getFechaInicio() != null ? ff.getFechaInicio().format(FMT) : "—",
                                ff.getFechaFin()    != null ? ff.getFechaFin().format(FMT)    : "—",
                                ff.getDuracionHoras() != null ? ff.getDuracionHoras()+"h"    : "—",
                                s(ff.getInstitucion()),
                                Boolean.TRUE.equals(ff.getCertificado()) ? "Sí" : "No",
                                ff.getFechaCaducidad() != null ? ff.getFechaCaducidad().format(FMT) : "—"
                        })));
            } catch (Exception ex) { msg(lblMsg, false, "Datos inválidos: " + ex.getMessage()); }
        });

        return splitPanel(model, form);
    }


    // UTILIDADES


    private JPanel splitPanel(DefaultTableModel model, JPanel form) {
        JTable tabla = new JTable(model);
        tabla.setRowHeight(24); tabla.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tabla.setGridColor(BORDER); tabla.setFillsViewportHeight(true);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tabla), new JScrollPane(form));
        split.setResizeWeight(0.4); split.setBorder(null);
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG); p.setBorder(new EmptyBorder(8,12,8,12));
        p.add(split, BorderLayout.CENTER);
        return p;
    }

    private void recargar(DefaultTableModel model, Runnable loader) {
        model.setRowCount(0); loader.run();
    }

    private JPanel panelForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG); p.setBorder(new EmptyBorder(12,16,12,16)); return p;
    }

    private JPanel scrollWrap(JPanel form) {
        JScrollPane sp = new JScrollPane(form); sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(12);
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(BG); p.add(sp); return p;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets=new Insets(4,6,4,6); g.fill=GridBagConstraints.HORIZONTAL;
        g.anchor=GridBagConstraints.WEST; g.weightx=1; return g;
    }

    private void fila(JPanel p, GridBagConstraints g, int row, String etiqueta, JComponent c) {
        g.gridx=0; g.gridy=row; g.gridwidth=1;
        JLabel l=new JLabel(etiqueta+":"); l.setFont(new Font("SansSerif",Font.BOLD,12)); l.setForeground(FG);
        p.add(l,g); g.gridx=1; p.add(c,g);
    }

    private DefaultTableModel tableModel(String[] cols) {
        return new DefaultTableModel(cols,0){ @Override public boolean isCellEditable(int r,int c){return false;} };
    }

    private JTextField tf(int cols) { return new JTextField(cols); }
    private JLabel msgLabel() { JLabel l=new JLabel(" "); l.setFont(new Font("SansSerif",Font.BOLD,12)); return l; }

    private JButton boton(String txt) {
        JButton b=new JButton(txt); b.setBackground(ACCENT); b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif",Font.BOLD,12)); b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    private JSpinner spinner(LocalDate d) {
        SpinnerDateModel m=new SpinnerDateModel(Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                null,null,java.util.Calendar.DAY_OF_MONTH);
        JSpinner sp=new JSpinner(m); sp.setEditor(new JSpinner.DateEditor(sp,"dd/MM/yyyy"));
        sp.setPreferredSize(new Dimension(130,26)); return sp;
    }

    private javax.swing.border.Border titledBorder(String t) {
        return BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER), t);
    }

    private JLabel jlabel(String txt, JList<?> list, boolean sel) {
        JLabel l=new JLabel(txt); l.setOpaque(true); l.setFont(new Font("SansSerif",Font.PLAIN,12));
        l.setBackground(sel?list.getSelectionBackground():list.getBackground());
        l.setForeground(sel?list.getSelectionForeground():list.getForeground());
        l.setBorder(BorderFactory.createEmptyBorder(2,6,2,6)); return l;
    }

    private void msg(JLabel l, boolean ok, String txt) {
        l.setForeground(ok?new Color(34,139,34):Color.RED); l.setText(txt);
    }

    private LocalDate ld(JSpinner sp) {
        return ((Date)sp.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private boolean persistir(Object entity) {
        Session s=null; Transaction tx=null;
        try {
            s=HibernateUtil.getSessionFactory().openSession(); tx=s.beginTransaction();
            s.persist(entity); tx.commit(); return true;
        } catch(Exception ex) {
            if(tx!=null&&tx.isActive()) try{tx.rollback();}catch(Exception ignored){}
            System.err.println("Error al persistir: "+ex.getMessage()); return false;
        } finally { if(s!=null&&s.isOpen()) s.close(); }
    }

    private String s(String v)  { return v!=null?v:"—"; }
    private String bn(String v) { return (v==null||v.isBlank())?null:v.trim(); }
}