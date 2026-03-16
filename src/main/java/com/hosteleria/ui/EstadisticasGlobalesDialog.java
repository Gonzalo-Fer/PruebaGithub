package com.hosteleria.ui;

import com.hosteleria.service.EstadisticasEmpleadoService;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo con estadísticas globales de la plantilla.
 */
public class EstadisticasGlobalesDialog extends JDialog {

    private final EstadisticasEmpleadoService servicio;

    public EstadisticasGlobalesDialog(Frame parent, EstadisticasEmpleadoService servicio) {
        super(parent, "Estadísticas globales", true);
        this.servicio = servicio;
        setSize(480, 380);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(26, 32, 44));

        EstadisticasEmpleadoService.EstadisticasGlobalesDTO g = servicio.estadisticasGlobales();

        JTextArea area = new JTextArea(22, 45);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setBackground(new Color(36, 44, 60));
        area.setForeground(new Color(237, 242, 247));
        area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════\n");
        sb.append("  RESUMEN DE PLANTILLA\n");
        sb.append("══════════════════════════════════════\n\n");
        sb.append("Total empleados: ").append(g.getTotalEmpleados()).append("\n");
        sb.append("  • Activos:        ").append(g.getActivos()).append("\n");
        sb.append("  • Baja definitiva: ").append(g.getBajaDefinitiva()).append("\n");
        sb.append("  • Vacaciones:     ").append(g.getVacaciones()).append("\n");
        sb.append("  • Baja temporal:  ").append(g.getBajaTemporal()).append("\n\n");
        sb.append("Por área:\n");
        g.getEmpleadosPorArea().forEach((nombre, num) -> sb.append("  • ").append(nombre).append(": ").append(num).append("\n"));
        sb.append("\nPor tipo de contrato:\n");
        g.getEmpleadosPorTipoContrato().forEach((tipo, num) -> sb.append("  • ").append(tipo).append(": ").append(num).append("\n"));
        area.setText(sb.toString());

        add(new JScrollPane(area), BorderLayout.CENTER);
    }
}
