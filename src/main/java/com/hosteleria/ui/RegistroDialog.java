package com.hosteleria.ui;

import com.hosteleria.controller.HosteleriaController;
import com.hosteleria.model.Usuario;
import com.hosteleria.service.LoginService;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo para registrar un nuevo usuario.
 * Modo público (desde login): solo nombre, usuario, email y contraseña — rol EMPLEADO, acceso PC.
 * Modo admin (desde menú): formulario completo con rol, acceso PC e ID empleado.
 */
public class RegistroDialog extends JDialog {

    private final LoginService loginService = new LoginService();
    private final boolean modoPublico;
    private JTextField campoNombre, campoUsername, campoEmail;
    private JPasswordField campoPassword;
    private JComboBox<String> comboSexoRol;
    private JCheckBox checkAccesoPC;
    private JSpinner spinnerIdEmpleado;
    private JLabel mensajeError;

    /** Modo admin: formulario completo. */
    public RegistroDialog(Frame parent) {
        this(parent, false);
    }

    /** @param modoPublico true = registro desde login (solo datos básicos, rol EMPLEADO) */
    public RegistroDialog(Frame parent, boolean modoPublico) {
        super(parent, modoPublico ? "Crear cuenta" : "Registrar nuevo usuario", true);
        this.modoPublico = modoPublico;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(480, modoPublico ? 320 : 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(26, 32, 44));

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(26, 32, 44));
        center.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        crearFila(center, gbc, row++, "Nombre:", campoNombre = new JTextField(22));
        crearFila(center, gbc, row++, "Usuario (login):", campoUsername = new JTextField(22));
        crearFila(center, gbc, row++, "Email:", campoEmail = new JTextField(22));
        crearFila(center, gbc, row++, "Contraseña (mín. 6):", campoPassword = new JPasswordField(22));

        if (!modoPublico) {
            gbc.gridy = row;
            gbc.gridx = 0;
            center.add(new JLabel("Rol:"), gbc);
            comboSexoRol = new JComboBox<>(new String[]{"EMPLEADO", "GERENTE", "ADMIN"});
            gbc.gridx = 1;
            center.add(comboSexoRol, gbc);
            row++;

            checkAccesoPC = new JCheckBox("Acceso a aplicación de escritorio (PC)");
            checkAccesoPC.setSelected(true);
            checkAccesoPC.setForeground(new Color(237, 242, 247));
            checkAccesoPC.setBackground(new Color(26, 32, 44));
            gbc.gridy = row;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            center.add(checkAccesoPC, gbc);
            row++;

            gbc.gridwidth = 1;
            gbc.gridy = row;
            gbc.gridx = 0;
            center.add(new JLabel("ID Empleado (opcional):"), gbc);
            spinnerIdEmpleado = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));
            gbc.gridx = 1;
            center.add(spinnerIdEmpleado, gbc);
            row++;
        } else {
            comboSexoRol = null;
            checkAccesoPC = null;
            spinnerIdEmpleado = null;
        }

        mensajeError = new JLabel(" ");
        mensajeError.setForeground(new Color(246, 100, 100));
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        center.add(mensajeError, gbc);
        row++;

        JButton btnRegistrar = new JButton("Registrar");
        btnRegistrar.setBackground(new Color(154, 230, 180));
        btnRegistrar.setForeground(new Color(18, 22, 30));
        btnRegistrar.addActionListener(e -> registrar());
        gbc.gridy = row;
        center.add(btnRegistrar, gbc);

        add(center, BorderLayout.CENTER);

        estilizarLabels(center);
    }

    private void crearFila(JPanel p, GridBagConstraints gbc, int row, String etiqueta, JComponent campo) {
        gbc.gridy = row;
        gbc.gridx = 0;
        JLabel l = new JLabel(etiqueta);
        p.add(l, gbc);
        gbc.gridx = 1;
        p.add(campo, gbc);
    }

    private void estilizarLabels(JPanel p) {
        for (Component c : p.getComponents()) {
            if (c instanceof JLabel l && l != mensajeError) {
                l.setForeground(new Color(237, 242, 247));
            }
        }
    }

    private void registrar() {
        String nombre = campoNombre.getText();
        String username = campoUsername.getText();
        String email = campoEmail.getText();
        String password = new String(campoPassword.getPassword());
        Usuario.Rol rol = modoPublico ? Usuario.Rol.EMPLEADO : Usuario.Rol.valueOf((String) comboSexoRol.getSelectedItem());
        boolean accesoPC = modoPublico ? true : checkAccesoPC.isSelected();
        Integer idEmpleado = modoPublico ? null : ((Integer) spinnerIdEmpleado.getValue());
        if (idEmpleado != null && idEmpleado <= 0) idEmpleado = null;

        mensajeError.setText(" ");
        LoginService.ResultadoRegistro res = loginService.registrar(nombre, username, email, password, rol, accesoPC, idEmpleado);
        if (res.isOk()) {
            JOptionPane.showMessageDialog(this, "Usuario registrado correctamente.");
            dispose();
        } else {
            mensajeError.setText(res.getMensajeError() != null ? res.getMensajeError() : "Error al registrar.");
        }
    }
}
