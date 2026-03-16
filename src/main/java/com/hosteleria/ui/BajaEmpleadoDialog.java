package com.hosteleria.ui;

import com.hosteleria.controller.HosteleriaController;
import com.hosteleria.model.Empleado;
import com.hosteleria.service.EmpleadoGestionService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Diálogo para dar de baja a un empleado.
 */
public class BajaEmpleadoDialog extends JDialog {

    private final EmpleadoGestionService servicio;
    private final HosteleriaController ctrl;
    private JComboBox<Empleado> comboEmpleado;
    private JComboBox<String> comboMotivo;

    public BajaEmpleadoDialog(Frame parent, EmpleadoGestionService servicio, HosteleriaController ctrl) {
        super(parent, "Baja de empleado", true);
        this.servicio = servicio;
        this.ctrl = ctrl;
        setSize(450, 220);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(26, 32, 44));

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(26, 32, 44));
        center.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        List<Empleado> empleados = ctrl.getEmpleadosConAreaYPuesto();
        comboEmpleado = new JComboBox<>(empleados.stream()
            .filter(e -> e.getEstado() != Empleado.EstadoEmpleado.baja_definitiva)
            .toArray(Empleado[]::new));
        comboMotivo = new JComboBox<>(new String[]{"baja_definitiva", "baja_temporal", "vacaciones"});

        gbc.gridy = 0;
        gbc.gridx = 0;
        center.add(new JLabel("Empleado:"), gbc);
        gbc.gridx = 1;
        center.add(comboEmpleado, gbc);
        gbc.gridy = 1;
        gbc.gridx = 0;
        center.add(new JLabel("Motivo:"), gbc);
        gbc.gridx = 1;
        center.add(comboMotivo, gbc);

        JButton btnBaja = new JButton("Dar de baja");
        btnBaja.setBackground(new Color(246, 100, 100));
        btnBaja.setForeground(Color.WHITE);
        btnBaja.addActionListener(e -> darBaja());
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        center.add(btnBaja, gbc);

        add(center, BorderLayout.CENTER);
        for (Component c : center.getComponents()) {
            if (c instanceof JLabel l) l.setForeground(new Color(237, 242, 247));
        }
    }

    private void darBaja() {
        Empleado e = (Empleado) comboEmpleado.getSelectedItem();
        if (e == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un empleado.");
            return;
        }
        String motivoStr = (String) comboMotivo.getSelectedItem();
        Empleado.EstadoEmpleado motivo = Empleado.EstadoEmpleado.valueOf(motivoStr);
        EmpleadoGestionService.ResultadoBaja res = servicio.bajaEmpleado(e.getIdEmpleado(), motivo, null);
        if (res.isOk()) {
            JOptionPane.showMessageDialog(this, "Baja registrada correctamente.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, res.getMensajeError(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
