package com.hosteleria.ui;

import com.hosteleria.service.EstadisticasEmpleadoService;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Panel que dibuja una representación visual de las estadísticas globales
 * (gráficos de barras y proporciones).
 */
public class PanelGraficosEstadisticas extends JPanel {

    private static final Color BG = new Color(255, 252, 247);
    private static final Color BAR_ACTIVOS = new Color(237, 137, 54);
    private static final Color BAR_BAJA = new Color(245, 101, 101);
    private static final Color BAR_VACACIONES = new Color(246, 173, 85);
    private static final Color BAR_TEMPORAL = new Color(129, 230, 217);
    private static final Color TEXT = new Color(26, 32, 44);
    private static final Color TEXT_MUTED = new Color(113, 128, 150);

    private final EstadisticasEmpleadoService.EstadisticasGlobalesDTO datos;

    public PanelGraficosEstadisticas(EstadisticasEmpleadoService.EstadisticasGlobalesDTO datos) {
        this.datos = datos;
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (datos == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int margin = 24;
        int chartW = w - 2 * margin;

        // Título
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(TEXT);
        g2.drawString("Distribución de la plantilla", margin, 22);

        int total = datos.getTotalEmpleados();
        if (total == 0) {
            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString("No hay datos de empleados", margin, h / 2);
            return;
        }

        // Gráfico de barras por estado
        int barHeight = 28;
        int barSpacing = 8;
        int y = 50;

        dibujarBarra(g2, margin, y, chartW, barHeight, "Activos", datos.getActivos(), total, BAR_ACTIVOS);
        y += barHeight + barSpacing;
        dibujarBarra(g2, margin, y, chartW, barHeight, "Baja definitiva", datos.getBajaDefinitiva(), total, BAR_BAJA);
        y += barHeight + barSpacing;
        dibujarBarra(g2, margin, y, chartW, barHeight, "Vacaciones", datos.getVacaciones(), total, BAR_VACACIONES);
        y += barHeight + barSpacing;
        dibujarBarra(g2, margin, y, chartW, barHeight, "Baja temporal", datos.getBajaTemporal(), total, BAR_TEMPORAL);

        // Gráfico de barras por área (si hay datos)
        if (!datos.getEmpleadosPorArea().isEmpty() && y + 60 < h) {
            y += 40;
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(TEXT);
            g2.drawString("Empleados por área", margin, y);
            y += 20;

            int maxArea = datos.getEmpleadosPorArea().values().stream()
                .mapToInt(Long::intValue).max().orElse(1);
            int areaBarH = 18;
            int areaSpacing = 4;

            for (Map.Entry<String, Long> e : datos.getEmpleadosPorArea().entrySet()) {
                String nombre = e.getKey();
                int valor = e.getValue().intValue();
                int barW = (int) ((double) valor / maxArea * (chartW - 120));
                if (barW < 2 && valor > 0) barW = 2;

                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.setColor(TEXT_MUTED);
                g2.drawString(nombre, margin, y + areaBarH - 4);
                g2.setColor(new Color(99, 179, 237));
                g2.fillRoundRect(margin + 100, y, barW, areaBarH - 2, 4, 4);
                g2.setColor(TEXT);
                g2.drawString(String.valueOf(valor), margin + 105 + barW, y + areaBarH - 4);
                y += areaBarH + areaSpacing;
            }
        }
    }

    private void dibujarBarra(Graphics2D g2, int x, int y, int w, int h,
                              String etiqueta, int valor, int total, Color color) {
        double pct = total > 0 ? (double) valor / total : 0;
        int barW = (int) (pct * (w - 130));
        if (barW < 2 && valor > 0) barW = 2;

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(TEXT_MUTED);
        g2.drawString(etiqueta, x, y + h - 6);
        g2.setColor(color);
        g2.fillRoundRect(x + 110, y + 2, barW, h - 4, 6, 6);
        g2.setColor(TEXT);
        g2.drawString(valor + " (" + (total > 0 ? (valor * 100 / total) : 0) + "%)", x + 115 + barW, y + h - 6);
    }
}
