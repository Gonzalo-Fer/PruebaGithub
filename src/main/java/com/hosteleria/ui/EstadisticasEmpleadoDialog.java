package com.hosteleria.ui;

import com.hosteleria.service.EstadisticasEmpleadoService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Diálogo con estadísticas de un empleado en un periodo.
 */
public class EstadisticasEmpleadoDialog extends JDialog {

    public EstadisticasEmpleadoDialog(Frame parent, EstadisticasEmpleadoService servicio, int idEmpleado, LocalDate desde, LocalDate hasta) {
        super(parent, "Estadísticas empleado", true);
        setSize(420, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(26, 32, 44));

        EstadisticasEmpleadoService.EstadisticasEmpleadoDTO dto = servicio.estadisticasEmpleado(idEmpleado, desde, hasta);
        if (dto == null) {
            add(new JLabel("Empleado no encontrado."), BorderLayout.CENTER);
            return;
        }

        JTextArea area = new JTextArea(22, 38);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setBackground(new Color(36, 44, 60));
        area.setForeground(new Color(237, 242, 247));
        area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════\n");
        sb.append("  ").append(dto.getNombreCompleto()).append("\n");
        sb.append("  Periodo: ").append(desde).append(" - ").append(hasta).append("\n");
        sb.append("══════════════════════════════════\n\n");
        sb.append("Horas trabajadas: ").append(String.format("%.2f", dto.getTotalHorasTrabajadas())).append(" h\n");
        sb.append("Horas extra:      ").append(String.format("%.2f", dto.getTotalHorasExtra())).append(" h\n");
        sb.append("Nº fichajes:      ").append(dto.getNumeroFichajes()).append("\n");
        sb.append("Retraso total:    ").append(dto.getRetrasoTotalMinutos()).append(" min\n");
        sb.append("Días ausencia:    ").append(dto.getDiasAusencia()).append("\n");
        sb.append("Nº evaluaciones: ").append(dto.getNumeroEvaluaciones()).append(" (media: ").append(String.format("%.1f", dto.getPuntuacionMedia())).append(")\n");
        BigDecimal prop = dto.getTotalPropinas();
        sb.append("Total propinas:   ").append(prop != null ? prop + " €" : "—").append("\n");
        BigDecimal nom = dto.getTotalNominas();
        sb.append("Total nóminas:    ").append(nom != null ? nom + " €" : "—").append("\n");
        area.setText(sb.toString());

        add(new JScrollPane(area), BorderLayout.CENTER);
    }
}
