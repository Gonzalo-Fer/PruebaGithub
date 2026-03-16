package com.hosteleria.ui;

import com.hosteleria.model.Empleado;
import com.hosteleria.model.Turno;
import com.hosteleria.service.PresenciaService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Diálogo para crear o editar un turno.
 * Se abre desde el CuadrantePanel (doble clic en celda) o desde el botón "Nuevo turno".
 *
 * Uso:
 *   TurnoDialog dlg = new TurnoDialog(parent, presencia, empleados, null);   // nuevo
 *   TurnoDialog dlg = new TurnoDialog(parent, presencia, empleados, turno);  // editar
 *   dlg.setVisible(true);
 *   if (dlg.isGuardado()) { recargar(); }
 */
public class TurnoDialog extends JDialog {

    private static final Color ACCENT      = new Color(237, 137, 54);
    private static final Color BG          = new Color(247, 250, 252);
    private static final Color BORDER_C    = new Color(226, 232, 240);

    private final PresenciaService presencia;
    private final List<Empleado>   empleados;
    private final Turno            turnoEditar;  // null = modo creación

    private JComboBox<String>  cbEmpleado;
    private JTextField         tfFecha, tfInicio, tfFin, tfArea;
    private JComboBox<String>  cbTipo, cbEstado;

    private boolean guardado = false;

    public TurnoDialog(Frame parent, PresenciaService presencia,
                       List<Empleado> empleados, Turno turnoEditar) {
        super(parent, turnoEditar == null ? "➕ Nuevo turno" : "✏️ Editar turno", true);
        this.presencia   = presencia;
        this.empleados   = empleados;
        this.turnoEditar = turnoEditar;
        construirUI();
        if (turnoEditar != null) rellenar();
        pack();
        setMinimumSize(new Dimension(440, 0));
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void construirUI() {
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        // ── Formulario ────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 8, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(5, 4, 5, 4);
        g.anchor  = GridBagConstraints.WEST;
        g.fill    = GridBagConstraints.HORIZONTAL;

        // Empleado
        String[] nombres = empleados.stream()
            .map(e -> e.getIdEmpleado() + " — " + e.getNombre() + " " + e.getApellidos())
            .toArray(String[]::new);
        cbEmpleado = new JComboBox<>(nombres);
        cbEmpleado.setBackground(Color.WHITE);

        // Tipo turno
        cbTipo = new JComboBox<>(new String[]{"mañana", "tarde", "noche", "partido", "completo"});
        cbTipo.setBackground(Color.WHITE);

        // Estado (solo en modo edición)
        cbEstado = new JComboBox<>(new String[]{"programado", "completado", "ausente", "cancelado"});
        cbEstado.setBackground(Color.WHITE);

        tfFecha  = campo(LocalDate.now().toString());
        tfInicio = campo("08:00");
        tfFin    = campo("16:00");
        tfArea   = campo("");

        int row = 0;
        fila(form, g, row++, "Empleado:",             cbEmpleado);
        fila(form, g, row++, "Fecha  (AAAA-MM-DD):",  tfFecha);
        fila(form, g, row++, "Inicio (HH:mm):",        tfInicio);
        fila(form, g, row++, "Fin    (HH:mm):",        tfFin);
        fila(form, g, row++, "Tipo de turno:",         cbTipo);
        fila(form, g, row++, "Área asignada:",         tfArea);
        if (turnoEditar != null) {
            fila(form, g, row++, "Estado:", cbEstado);
        }

        // ── Hint de horas calculadas ──────────────────────────────────
        JLabel hint = new JLabel(" ");
        hint.setFont(new Font("Monospaced", Font.PLAIN, 11));
        hint.setForeground(ACCENT);
        g.gridx = 0; g.gridy = row; g.gridwidth = 2;
        form.add(hint, g);
        g.gridwidth = 1;

        // Actualizar hint en tiempo real
        Runnable actualizarHint = () -> {
            try {
                LocalTime ini = LocalTime.parse(tfInicio.getText().trim());
                LocalTime fin = LocalTime.parse(tfFin.getText().trim());
                if (fin.isAfter(ini)) {
                    long mins = java.time.temporal.ChronoUnit.MINUTES.between(ini, fin);
                    hint.setText(String.format(" → %.2f horas", mins / 60.0));
                } else {
                    hint.setText(" ⚠ La hora de fin debe ser posterior al inicio");
                }
            } catch (DateTimeParseException e) {
                hint.setText(" ");
            }
        };
        tfInicio.addActionListener(e -> actualizarHint.run());
        tfFin.addActionListener(e    -> actualizarHint.run());
        tfInicio.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { actualizarHint.run(); }
        });
        tfFin.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { actualizarHint.run(); }
        });

        // ── Botones ───────────────────────────────────────────────────
        JButton btnGuardar  = new JButton(turnoEditar == null ? "Crear turno" : "Guardar cambios");
        JButton btnCancelar = new JButton("Cancelar");

        btnGuardar.setBackground(ACCENT);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnGuardar.setBorder(BorderFactory.createEmptyBorder(7, 18, 7, 18));
        btnGuardar.setFocusPainted(false);

        btnGuardar.addActionListener(e  -> guardar());
        btnCancelar.addActionListener(e -> dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        botones.setBackground(BG);
        botones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        add(form,    BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
    }

    private JTextField campo(String valorInicial) {
        JTextField tf = new JTextField(valorInicial, 14);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        return tf;
    }

    private void fila(JPanel p, GridBagConstraints g, int row, String etiqueta, JComponent comp) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        p.add(lbl, g);
        g.gridx = 1; g.weightx = 1;
        p.add(comp, g);
    }

    private void rellenar() {
        // Seleccionar empleado
        int idEmp = turnoEditar.getEmpleado().getIdEmpleado();
        for (int i = 0; i < empleados.size(); i++) {
            if (empleados.get(i).getIdEmpleado().equals(idEmp)) {
                cbEmpleado.setSelectedIndex(i); break;
            }
        }
        if (turnoEditar.getFecha()      != null) tfFecha.setText(turnoEditar.getFecha().toString());
        if (turnoEditar.getHoraInicio() != null) tfInicio.setText(turnoEditar.getHoraInicio().toString());
        if (turnoEditar.getHoraFin()    != null) tfFin.setText(turnoEditar.getHoraFin().toString());
        if (turnoEditar.getTipoTurno()  != null) cbTipo.setSelectedItem(turnoEditar.getTipoTurno().name());
        if (turnoEditar.getAreaAsignada() != null) tfArea.setText(turnoEditar.getAreaAsignada());
        if (turnoEditar.getEstado()     != null) cbEstado.setSelectedItem(turnoEditar.getEstado().name());
    }

    private void guardar() {
        try {
            LocalDate fecha  = LocalDate.parse(tfFecha.getText().trim());
            LocalTime inicio = LocalTime.parse(tfInicio.getText().trim());
            LocalTime fin    = LocalTime.parse(tfFin.getText().trim());
            Turno.TipoTurno tipo = Turno.TipoTurno.valueOf((String) cbTipo.getSelectedItem());
            String area = tfArea.getText().trim();
            int idEmp   = empleados.get(cbEmpleado.getSelectedIndex()).getIdEmpleado();

            PresenciaService.Resultado res;
            if (turnoEditar == null) {
                res = presencia.crearTurno(idEmp, fecha, inicio, fin, tipo, area);
            } else {
                Turno.EstadoTurno estado = Turno.EstadoTurno.valueOf((String) cbEstado.getSelectedItem());
                res = presencia.modificarTurno(turnoEditar.getIdTurno(),
                        fecha, inicio, fin, tipo, area, estado);
            }

            if (res.ok) {
                guardado = true;
                JOptionPane.showMessageDialog(this, res.mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, res.mensaje, "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                "Formato incorrecto.\n  Fecha:  AAAA-MM-DD\n  Hora:   HH:mm",
                "Error de formato", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** @return true si el usuario guardó el turno correctamente. */
    public boolean isGuardado() { return guardado; }
}
