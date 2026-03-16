package com.hosteleria.ui;

import com.hosteleria.service.LoginService;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo de login. Si el login es correcto, cierra el diálogo y el caller puede abrir la aplicación.
 */
public class LoginDialog extends JDialog {

    private final LoginService loginService = new LoginService();
    private JTextField campoUsuario;
    private JPasswordField campoPassword;
    private JLabel mensajeError;
    private boolean loginOk;

    public LoginDialog(Frame parent) {
        super(parent, "Iniciar sesión", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 280);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout(20, 20));
        getContentPane().setBackground(new Color(26, 32, 44));

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(26, 32, 44));
        center.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel titulo = new JLabel("  Hostelería Manager");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(new Color(99, 179, 237));
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        center.add(titulo, gbc);
        gbc.gridwidth = 1;

        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setForeground(new Color(237, 242, 247));
        gbc.gridy = 1;
        gbc.gridx = 0;
        center.add(lblUser, gbc);
        campoUsuario = new JTextField(20);
        campoUsuario.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 1;
        center.add(campoUsuario, gbc);

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setForeground(new Color(237, 242, 247));
        gbc.gridy = 2;
        gbc.gridx = 0;
        center.add(lblPass, gbc);
        campoPassword = new JPasswordField(20);
        campoPassword.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 1;
        center.add(campoPassword, gbc);

        mensajeError = new JLabel(" ");
        mensajeError.setForeground(new Color(246, 100, 100));
        mensajeError.setFont(new Font("SansSerif", Font.PLAIN, 11));
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        center.add(mensajeError, gbc);
        gbc.gridwidth = 1;

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        botones.setBackground(new Color(26, 32, 44));
        JButton btnEntrar = new JButton("Entrar");
        btnEntrar.setBackground(new Color(99, 179, 237));
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFocusPainted(false);
        btnEntrar.addActionListener(e -> intentarLogin());
        JButton btnRegistrarse = new JButton("Crear cuenta");
        btnRegistrarse.setForeground(new Color(154, 230, 180));
        btnRegistrarse.setBackground(new Color(26, 32, 44));
        btnRegistrarse.setBorderPainted(false);
        btnRegistrarse.setFocusPainted(false);
        btnRegistrarse.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRegistrarse.addActionListener(e -> abrirRegistro());
        botones.add(btnEntrar);
        botones.add(btnRegistrarse);
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        center.add(botones, gbc);

        add(center, BorderLayout.CENTER);

        campoPassword.addActionListener(e -> intentarLogin());
    }

    private void intentarLogin() {
        String user = campoUsuario.getText();
        String pass = new String(campoPassword.getPassword());
        mensajeError.setText(" ");
        LoginService.ResultadoLogin res = loginService.login(user, pass);
        if (res.isOk()) {
            loginOk = true;
            dispose();
        } else {
            mensajeError.setText(res.getMensajeError() != null ? res.getMensajeError() : "Error al iniciar sesión.");
        }
    }

    private void abrirRegistro() {
        RegistroDialog dlg = new RegistroDialog(null, true);
        dlg.setVisible(true);
    }

    /** Devuelve true si el usuario cerró el diálogo tras un login correcto. */
    public boolean isLoginOk() {
        return loginOk;
    }
}
