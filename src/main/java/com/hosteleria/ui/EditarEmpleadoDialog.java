
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

    public class EditarEmpleadoDialog extends JDialog {

        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private static final Color BG     = MainUI.BG_DARK;
        private static final Color ACCENT = MainUI.ACCENT;
        private static final Color FG     = MainUI.TEXT_PRIMARY;
        private static final Color BORDER = MainUI.BORDER_COLOR;

        private final HosteleriaController   ctrl;
        private final EmpleadoGestionService servicio;
        private final Empleado               empleado;

        public EditarEmpleadoDialog(Frame parent, HosteleriaController ctrl,
                                    EmpleadoGestionService servicio, Empleado empleado) {
            super(parent, "Modificar — " + empleado.getNombre() + " " + empleado.getApellidos(), true);
            this.ctrl = ctrl; this.servicio = servicio; this.empleado = empleado;
            setSize(700, 660);
            setMinimumSize(new Dimension(600, 540));
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());

            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(new Font("SansSerif", Font.PLAIN, 12));
            tabs.addTab("👤 Datos personales",  tabDatos());
            tabs.addTab("📋 Historial laboral", tabHistorial());
            tabs.addTab("📄 Contratos",         tabContratos());
            tabs.addTab("🎓 Formación",         tabFormacion());
            add(tabs, BorderLayout.CENTER);
        }

        // ══════════════════════════════════════════════════════════════════
        // TAB 1 — DATOS PERSONALES
        // ══════════════════════════════════════════════════════════════════

        private JPanel tabDatos() {
            JPanel form = panelForm();
            GridBagConstraints g = gbc();

            JTextField txtNombre    = tf(empleado.getNombre());
            JTextField txtApellidos = tf(empleado.getApellidos());
            JTextField txtEmail     = tf(s(empleado.getEmail()));
            JTextField txtTelefono  = tf(s(empleado.getTelefono()));
            JTextField txtNSS       = tf(s(empleado.getNumeroSeguridadSocial()));
            JTextField txtSalario   = tf(empleado.getSalarioBase() != null ? empleado.getSalarioBase().toString() : "");
            JTextField txtFoto      = tf(s(empleado.getFotoPerfil()));
            JTextField txtCNombre   = tf(s(empleado.getContactoEmergenciaNombre()));
            JTextField txtCTel      = tf(s(empleado.getContactoEmergenciaTelefono()));
            JTextField txtCRel      = tf(s(empleado.getContactoEmergenciaRelacion()));

            JSpinner spNac    = spinner(empleado.getFechaNacimiento()       != null ? empleado.getFechaNacimiento()       : LocalDate.of(1990,1,1));
            JSpinner spCarnet = spinner(empleado.getFechaCarnetManipulador() != null ? empleado.getFechaCarnetManipulador(): LocalDate.now());

            JComboBox<Area> cmbArea = new JComboBox<>(ctrl.getAllAreas().toArray(new Area[0]));
            cmbArea.insertItemAt(null, 0);
            cmbArea.setRenderer((l,v,i,s,f) -> jlabel(v instanceof Area a ? a.getNombre() : "— Sin área —", l, s));
            if (empleado.getArea() != null)
                for (int i=0; i<cmbArea.getItemCount(); i++)
                    if (cmbArea.getItemAt(i) != null && cmbArea.getItemAt(i).getIdArea().equals(empleado.getArea().getIdArea()))
                    { cmbArea.setSelectedIndex(i); break; }

            JComboBox<Puesto> cmbPuesto = new JComboBox<>(ctrl.getAllPuestos().toArray(new Puesto[0]));
            cmbPuesto.setRenderer((l,v,i,s,f) -> jlabel(v instanceof Puesto p ? p.getTitulo() : "", l, s));
            if (empleado.getPuesto() != null)
                for (int i=0; i<cmbPuesto.getItemCount(); i++)
                    if (cmbPuesto.getItemAt(i) != null && cmbPuesto.getItemAt(i).getIdPuesto().equals(empleado.getPuesto().getIdPuesto()))
                    { cmbPuesto.setSelectedIndex(i); break; }

            JComboBox<Empleado.TipoContrato>   cmbTipo   = new JComboBox<>(Empleado.TipoContrato.values());
            JComboBox<Empleado.EstadoEmpleado> cmbEstado = new JComboBox<>(Empleado.EstadoEmpleado.values());
            cmbTipo.setSelectedItem(empleado.getTipoContrato());
            cmbEstado.setSelectedItem(empleado.getEstado());

            JCheckBox chkCarnet = new JCheckBox("Tiene carnet de manipulador");
            chkCarnet.setSelected(Boolean.TRUE.equals(empleado.getCarnetManipulador()));
            chkCarnet.setOpaque(false); chkCarnet.setForeground(FG);
            spCarnet.setEnabled(chkCarnet.isSelected());
            chkCarnet.addActionListener(e -> spCarnet.setEnabled(chkCarnet.isSelected()));

            int r = 0;
            fila(form, g, r++, "Nombre *",               txtNombre);
            fila(form, g, r++, "Apellidos *",             txtApellidos);
            fila(form, g, r++, "Email",                   txtEmail);
            fila(form, g, r++, "Teléfono *",              txtTelefono);
            fila(form, g, r++, "Fecha nacimiento",        spNac);
            fila(form, g, r++, "Nº Seguridad Social",     txtNSS);
            fila(form, g, r++, "Área",                    cmbArea);
            fila(form, g, r++, "Puesto *",                cmbPuesto);
            fila(form, g, r++, "Salario base *",          txtSalario);
            fila(form, g, r++, "Tipo contrato *",         cmbTipo);
            fila(form, g, r++, "Estado",                  cmbEstado);
            fila(form, g, r++, "Foto perfil (ruta)",      txtFoto);
            g.gridx=0; g.gridy=r; g.gridwidth=2; form.add(chkCarnet, g); r++;
            g.gridwidth=1;
            fila(form, g, r++, "Fecha carnet",            spCarnet);
            fila(form, g, r++, "Contacto emergencia",     txtCNombre);
            fila(form, g, r++, "  Teléfono",              txtCTel);
            fila(form, g, r++, "  Relación",              txtCRel);

            JLabel lblMsg = msgLabel();
            g.gridx=0; g.gridy=r; g.gridwidth=2; form.add(lblMsg, g); r++;
            JButton btn = boton("💾 Guardar cambios");
            g.gridy=r; form.add(btn, g);

            btn.addActionListener(e -> {
                try {
                    Empleado orig = new Empleado();
                    orig.setArea(empleado.getArea()); orig.setPuesto(empleado.getPuesto()); orig.setSalarioBase(empleado.getSalarioBase());

                    empleado.setNombre(txtNombre.getText().trim());
                    empleado.setApellidos(txtApellidos.getText().trim());
                    empleado.setEmail(bn(txtEmail.getText()));
                    empleado.setTelefono(txtTelefono.getText().trim());
                    empleado.setNumeroSeguridadSocial(bn(txtNSS.getText()));
                    empleado.setFechaNacimiento(ld(spNac));
                    empleado.setArea((Area) cmbArea.getSelectedItem());
                    empleado.setPuesto((Puesto) cmbPuesto.getSelectedItem());
                    if (!txtSalario.getText().isBlank())
                        empleado.setSalarioBase(new BigDecimal(txtSalario.getText().replace(",",".")));
                    empleado.setTipoContrato((Empleado.TipoContrato) cmbTipo.getSelectedItem());
                    empleado.setEstado((Empleado.EstadoEmpleado) cmbEstado.getSelectedItem());
                    empleado.setFotoPerfil(bn(txtFoto.getText()));
                    empleado.setCarnetManipulador(chkCarnet.isSelected());
                    if (chkCarnet.isSelected()) empleado.setFechaCarnetManipulador(ld(spCarnet));
                    empleado.setContactoEmergenciaNombre(bn(txtCNombre.getText()));
                    empleado.setContactoEmergenciaTelefono(bn(txtCTel.getText()));
                    empleado.setContactoEmergenciaRelacion(bn(txtCRel.getText()));

                    if (ctrl.actualizarEmpleado(empleado)) {
                        servicio.registrarHistorialSiCambio(orig, empleado, "Edición desde escritorio");
                        msg(lblMsg, true, "✅ Guardado correctamente.");
                    } else {
                        msg(lblMsg, false, "Error al actualizar.");
                    }
                } catch (Exception ex) { msg(lblMsg, false, "Datos inválidos: " + ex.getMessage()); }
            });

            return scrollWrap(form);
        }

        // ══════════════════════════════════════════════════════════════════
        // TAB 2 — HISTORIAL LABORAL
        // ══════════════════════════════════════════════════════════════════

        private JPanel tabHistorial() {
            String[] cols = {"Fecha","Área ant.","Área nueva","Puesto ant.","Puesto nuevo","Sal. ant.","Sal. nuevo","Motivo"};
            DefaultTableModel model = tableModel(cols);
            recargarHistorial(model);

            JPanel form = panelForm();
            form.setBorder(titledBorder("➕ Añadir entrada de historial"));
            GridBagConstraints g = gbc();

            JSpinner   spFecha    = spinner(LocalDate.now());
            JTextField txtAAnt    = tf(""), txtANueva  = tf("");
            JTextField txtPAnt    = tf(""), txtPNuevo  = tf("");
            JTextField txtSalAnt  = tf(""), txtSalNuevo= tf("");
            JTextField txtMotivo  = tf("");
            JLabel lblMsg = msgLabel();

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
                try {
                    HistorialLaboral h = new HistorialLaboral();
                    h.setEmpleado(empleado); h.setFecha(ld(spFecha));
                    h.setAreaAnterior(bn(txtAAnt.getText()));    h.setAreaNueva(bn(txtANueva.getText()));
                    h.setPuestoAnterior(bn(txtPAnt.getText()));  h.setPuestoNuevo(bn(txtPNuevo.getText()));
                    if (!txtSalAnt.getText().isBlank())   h.setSalarioAnterior(new BigDecimal(txtSalAnt.getText().replace(",",".")));
                    if (!txtSalNuevo.getText().isBlank()) h.setSalarioNuevo(new BigDecimal(txtSalNuevo.getText().replace(",",".")));
                    h.setMotivo(bn(txtMotivo.getText()));
                    boolean ok = ctrl.guardarHistorial(h);
                    msg(lblMsg, ok, ok ? "✅ Guardado." : "❌ Error al guardar.");
                    if (ok) recargarHistorial(model);
                } catch (Exception ex) { msg(lblMsg, false, "Datos inválidos: " + ex.getMessage()); }
            });

            return splitPanel(model, form);
        }

        private void recargarHistorial(DefaultTableModel m) {
            m.setRowCount(0);
            ctrl.getHistorialLaboralPorEmpleado(empleado.getIdEmpleado()).forEach(h -> m.addRow(new Object[]{
                    h.getFecha() != null ? h.getFecha().format(FMT) : "—",
                    s(h.getAreaAnterior()), s(h.getAreaNueva()),
                    s(h.getPuestoAnterior()), s(h.getPuestoNuevo()),
                    h.getSalarioAnterior() != null ? h.getSalarioAnterior()+" €" : "—",
                    h.getSalarioNuevo()    != null ? h.getSalarioNuevo()   +" €" : "—",
                    s(h.getMotivo())
            }));
        }

        // ══════════════════════════════════════════════════════════════════
        // TAB 3 — CONTRATOS
        // ══════════════════════════════════════════════════════════════════

        private JPanel tabContratos() {
            String[] cols = {"Tipo","Inicio","Fin previsto","Fin real","Salario","Renovación","Observaciones"};
            DefaultTableModel model = tableModel(cols);
            recargarContratos(model);

            JPanel form = panelForm();
            form.setBorder(titledBorder("➕ Añadir contrato"));
            GridBagConstraints g = gbc();

            JComboBox<Empleado.TipoContrato> cmbTipo = new JComboBox<>(Empleado.TipoContrato.values());
            JSpinner spInicio  = spinner(LocalDate.now());
            JSpinner spFinPrev = spinner(LocalDate.now().plusYears(1));
            JSpinner spFinReal = spinner(LocalDate.now().plusYears(1)); spFinReal.setEnabled(false);
            JCheckBox chkFin   = new JCheckBox("Registrar fecha fin real"); chkFin.setOpaque(false); chkFin.setForeground(FG);
            chkFin.addActionListener(e -> spFinReal.setEnabled(chkFin.isSelected()));
            JTextField txtSal  = tf(""), txtRenov = tf(""), txtObs = tf("");
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
                try {
                    Contrato c = new Contrato();
                    c.setEmpleado(empleado);
                    c.setTipoContrato((Empleado.TipoContrato) cmbTipo.getSelectedItem());
                    c.setFechaInicio(ld(spInicio)); c.setFechaFinPrevista(ld(spFinPrev));
                    if (chkFin.isSelected()) c.setFechaFinReal(ld(spFinReal));
                    if (!txtSal.getText().isBlank()) c.setSalarioBase(new BigDecimal(txtSal.getText().replace(",",".")));
                    c.setRenovacion(bn(txtRenov.getText())); c.setObservaciones(bn(txtObs.getText()));
                    boolean ok = ctrl.guardarContrato(c);
                    msg(lblMsg, ok, ok ? "✅ Guardado." : "❌ Error al guardar.");
                    if (ok) recargarContratos(model);
                } catch (Exception ex) { msg(lblMsg, false, "Datos inválidos: " + ex.getMessage()); }
            });

            return splitPanel(model, form);
        }

        private void recargarContratos(DefaultTableModel m) {
            m.setRowCount(0);
            ctrl.getContratosPorEmpleado(empleado.getIdEmpleado()).forEach(c -> m.addRow(new Object[]{
                    c.getTipoContrato(),
                    c.getFechaInicio()      != null ? c.getFechaInicio().format(FMT)      : "—",
                    c.getFechaFinPrevista() != null ? c.getFechaFinPrevista().format(FMT) : "—",
                    c.getFechaFinReal()     != null ? c.getFechaFinReal().format(FMT)     : "—",
                    c.getSalarioBase()      != null ? c.getSalarioBase()+" €"             : "—",
                    s(c.getRenovacion()), s(c.getObservaciones())
            }));
        }

        // ══════════════════════════════════════════════════════════════════
        // TAB 4 — FORMACIÓN
        // ══════════════════════════════════════════════════════════════════

        private JPanel tabFormacion() {
            String[] cols = {"Curso","Tipo","Inicio","Fin","Horas","Institución","Certificado","Caducidad"};
            DefaultTableModel model = tableModel(cols);
            recargarFormacion(model);

            JPanel form = panelForm();
            form.setBorder(titledBorder("➕ Añadir formación"));
            GridBagConstraints g = gbc();

            JTextField txtCurso = new JTextField(24);
            JComboBox<Formacion.TipoFormacion> cmbTipo = new JComboBox<>(Formacion.TipoFormacion.values());
            JSpinner spIni = spinner(LocalDate.now()), spFin = spinner(LocalDate.now().plusMonths(1));
            JTextField txtHoras = tf(""), txtInst = tf("");
            JCheckBox chkCert = new JCheckBox("Certificado obtenido"); chkCert.setOpaque(false); chkCert.setForeground(FG);
            JSpinner spCad = spinner(LocalDate.now().plusYears(2)); spCad.setEnabled(false);
            chkCert.addActionListener(e -> spCad.setEnabled(chkCert.isSelected()));
            JLabel lblMsg = msgLabel();

            int r = 0;
            fila(form, g, r++, "Curso *",         txtCurso);
            fila(form, g, r++, "Tipo *",           cmbTipo);
            fila(form, g, r++, "Fecha inicio",     spIni);
            fila(form, g, r++, "Fecha fin",        spFin);
            fila(form, g, r++, "Horas",            txtHoras);
            fila(form, g, r++, "Institución",      txtInst);
            g.gridx=0; g.gridy=r; g.gridwidth=2; form.add(chkCert, g); r++;
            g.gridwidth=1;
            fila(form, g, r++, "Fecha caducidad",  spCad);
            g.gridx=0; g.gridy=r; g.gridwidth=2; form.add(lblMsg, g);
            JButton btn = boton("➕ Guardar formación"); g.gridy=++r; form.add(btn, g);

            btn.addActionListener(e -> {
                if (txtCurso.getText().isBlank()) { msg(lblMsg, false, "El nombre del curso es obligatorio."); return; }
                try {
                    Formacion f = new Formacion();
                    f.setEmpleado(empleado); f.setCurso(txtCurso.getText().trim());
                    f.setTipo((Formacion.TipoFormacion) cmbTipo.getSelectedItem());
                    f.setFechaInicio(ld(spIni)); f.setFechaFin(ld(spFin));
                    if (!txtHoras.getText().isBlank()) f.setDuracionHoras(Integer.parseInt(txtHoras.getText().trim()));
                    f.setInstitucion(bn(txtInst.getText()));
                    f.setCertificado(chkCert.isSelected());
                    if (chkCert.isSelected()) f.setFechaCaducidad(ld(spCad));
                    boolean ok = persistir(f);
                    msg(lblMsg, ok, ok ? "✅ Guardado." : "❌ Error al guardar.");
                    if (ok) recargarFormacion(model);
                } catch (Exception ex) { msg(lblMsg, false, "Datos inválidos: " + ex.getMessage()); }
            });

            return splitPanel(model, form);
        }

        private void recargarFormacion(DefaultTableModel m) {
            m.setRowCount(0);
            ctrl.getFormacionesConEmpleado().stream()
                    .filter(f -> f.getEmpleado().getIdEmpleado().equals(empleado.getIdEmpleado()))
                    .forEach(f -> m.addRow(new Object[]{
                            f.getCurso(), f.getTipo(),
                            f.getFechaInicio() != null ? f.getFechaInicio().format(FMT) : "—",
                            f.getFechaFin()    != null ? f.getFechaFin().format(FMT)    : "—",
                            f.getDuracionHoras() != null ? f.getDuracionHoras()+"h"    : "—",
                            s(f.getInstitucion()),
                            Boolean.TRUE.equals(f.getCertificado()) ? "Sí" : "No",
                            f.getFechaCaducidad() != null ? f.getFechaCaducidad().format(FMT) : "—"
                    }));
        }

        // ══════════════════════════════════════════════════════════════════
        // UTILIDADES
        // ══════════════════════════════════════════════════════════════════

        /** Tabla arriba + formulario abajo separados por JSplitPane. */
        private JPanel splitPanel(DefaultTableModel model, JPanel form) {
            JTable tabla = new JTable(model);
            tabla.setRowHeight(24); tabla.setFont(new Font("SansSerif", Font.PLAIN, 12));
            tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
            tabla.setGridColor(BORDER); tabla.setFillsViewportHeight(true);
            tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                    new JScrollPane(tabla), new JScrollPane(form));
            split.setResizeWeight(0.5); split.setBorder(null);

            JPanel p = new JPanel(new BorderLayout());
            p.setBackground(BG); p.setBorder(new EmptyBorder(8, 12, 8, 12));
            p.add(split, BorderLayout.CENTER);
            return p;
        }

        private JPanel panelForm() {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(BG); p.setBorder(new EmptyBorder(12, 16, 12, 16));
            return p;
        }

        private JPanel scrollWrap(JPanel form) {
            JScrollPane sp = new JScrollPane(form);
            sp.setBorder(null); sp.getVerticalScrollBar().setUnitIncrement(12);
            JPanel p = new JPanel(new BorderLayout());
            p.setBackground(BG); p.add(sp);
            return p;
        }

        private GridBagConstraints gbc() {
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(4,6,4,6); g.fill = GridBagConstraints.HORIZONTAL;
            g.anchor = GridBagConstraints.WEST; g.weightx = 1; return g;
        }

        private void fila(JPanel p, GridBagConstraints g, int row, String etiqueta, JComponent c) {
            g.gridx=0; g.gridy=row; g.gridwidth=1;
            JLabel l = new JLabel(etiqueta+":"); l.setFont(new Font("SansSerif",Font.BOLD,12)); l.setForeground(FG);
            p.add(l,g); g.gridx=1; p.add(c,g);
        }

        private DefaultTableModel tableModel(String[] cols) {
            return new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
        }

        private JTextField tf(String v)   { return new JTextField(v, 22); }
        private JLabel     msgLabel()     { JLabel l = new JLabel(" "); l.setFont(new Font("SansSerif",Font.BOLD,12)); return l; }

        private JButton boton(String txt) {
            JButton b = new JButton(txt); b.setBackground(ACCENT); b.setForeground(Color.WHITE);
            b.setFont(new Font("SansSerif",Font.BOLD,12)); b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
        }

        private JSpinner spinner(LocalDate d) {
            SpinnerDateModel m = new SpinnerDateModel(Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    null, null, java.util.Calendar.DAY_OF_MONTH);
            JSpinner sp = new JSpinner(m); sp.setEditor(new JSpinner.DateEditor(sp,"dd/MM/yyyy"));
            sp.setPreferredSize(new Dimension(130,26)); return sp;
        }

        private javax.swing.border.Border titledBorder(String title) {
            return BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER), title);
        }

        private JLabel jlabel(String txt, JList<?> list, boolean sel) {
            JLabel l = new JLabel(txt); l.setOpaque(true); l.setFont(new Font("SansSerif",Font.PLAIN,12));
            l.setBackground(sel ? list.getSelectionBackground() : list.getBackground());
            l.setForeground(sel ? list.getSelectionForeground() : list.getForeground());
            l.setBorder(BorderFactory.createEmptyBorder(2,6,2,6)); return l;
        }

        private void msg(JLabel l, boolean ok, String txt) {
            l.setForeground(ok ? new Color(34,139,34) : Color.RED); l.setText(txt);
        }

        private LocalDate ld(JSpinner sp) {
            return ((Date)sp.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        private boolean persistir(Object entity) {
            Session s = null; Transaction tx = null;
            try {
                s = HibernateUtil.getSessionFactory().openSession(); tx = s.beginTransaction();
                s.persist(entity); tx.commit(); return true;
            } catch (Exception ex) {
                if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ignored) {}
                System.err.println("Error al persistir: " + ex.getMessage()); return false;
            } finally { if (s != null && s.isOpen()) s.close(); }
        }

        private String s(String v)  { return v != null ? v : ""; }
        private String bn(String v) { return (v == null || v.isBlank()) ? null : v.trim(); }
    }