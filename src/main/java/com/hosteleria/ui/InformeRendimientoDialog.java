package com.hosteleria.ui;

import com.hosteleria.service.EvaluacionService;
import com.hosteleria.service.EvaluacionService.InformeEquipoDTO;
import com.hosteleria.service.EvaluacionService.ResumenEmpleadoDTO;
import com.hosteleria.service.GeneradorDocumentosService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Diálogo de informe de rendimiento del equipo.
 *
 * Muestra una tabla de ranking con todos los empleados evaluados en el periodo
 * seleccionado y un gráfico de barras con las puntuaciones medias.
 * Permite exportar el informe a PDF.
 */
public class InformeRendimientoDialog extends JDialog {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Color ACCENT       = MainUI.ACCENT;
    private static final Color BG_DARK      = MainUI.BG_DARK;
    private static final Color BG_PANEL     = MainUI.BG_PANEL;
    private static final Color TEXT_PRIMARY = MainUI.TEXT_PRIMARY;
    private static final Color TEXT_MUTED   = MainUI.TEXT_MUTED;
    private static final Color BORDER_COLOR = MainUI.BORDER_COLOR;

    private final EvaluacionService       service  = new EvaluacionService();
    private final GeneradorDocumentosService genDocs = new GeneradorDocumentosService();

    // Referencias para refresco
    private DefaultTableModel modelTabla;
    private GraficoBarra      panelGrafico;
    private JLabel            lblMedia;
    private JTextField        txtDesde;
    private JTextField        txtHasta;

    public InformeRendimientoDialog(Frame parent) {
        super(parent, "📊 Informe de Rendimiento del Equipo", true);
        setSize(1000, 680);
        setMinimumSize(new Dimension(820, 540));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(BG_DARK);

        add(crearFiltro(),   BorderLayout.NORTH);
        add(crearCentro(),   BorderLayout.CENTER);
        add(crearBotones(),  BorderLayout.SOUTH);
    }

    // ── Panel de filtro ───────────────────────────────────────────────

    private JPanel crearFiltro() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR), new EmptyBorder(6, 12, 6, 12)));

        LocalDate hoy  = LocalDate.now();
        LocalDate ini  = hoy.minusMonths(3).withDayOfMonth(1);

        txtDesde = new JTextField(ini.format(FMT), 10);
        txtHasta = new JTextField(hoy.format(FMT),  10);

        JButton btnGenerar = boton("🔍 Generar informe", ACCENT);
        btnGenerar.addActionListener(e -> generarInforme());

        lblMedia = new JLabel(" ");
        lblMedia.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblMedia.setForeground(TEXT_MUTED);

        p.add(new JLabel("Desde:"));
        p.add(txtDesde);
        p.add(new JLabel("Hasta:"));
        p.add(txtHasta);
        p.add(btnGenerar);
        p.add(Box.createHorizontalStrut(16));
        p.add(lblMedia);
        return p;
    }

    // ── Zona central: tabla + gráfico ────────────────────────────────

    private JSplitPane crearCentro() {
        // Tabla ranking
        String[] cols = {"#", "Empleado", "Evaluaciones",
                         "Puntualidad", "Atención", "Equipo", "Producto", "Higiene",
                         "Media total", "Obj. completados"};
        modelTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return (c == 0 || c == 2) ? Integer.class : (c >= 3 ? Double.class : String.class);
            }
        };
        JTable tabla = new JTable(modelTabla);
        tabla.setRowHeight(24);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tabla.setSelectionBackground(MainUI.ROW_HOVER);
        tabla.setGridColor(BORDER_COLOR);
        tabla.setFillsViewportHeight(true);
        tabla.setDefaultRenderer(Double.class, new DecimalRenderer());

        // Columna media total coloreada
        tabla.getColumnModel().getColumn(8).setCellRenderer(new PuntuacionRenderer());

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Gráfico de barras
        panelGrafico = new GraficoBarra();
        panelGrafico.setPreferredSize(new Dimension(0, 220));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTabla, panelGrafico);
        split.setDividerLocation(330);
        split.setResizeWeight(0.6);
        split.setBorder(new EmptyBorder(0, 12, 0, 12));
        return split;
    }

    // ── Botones inferiores ────────────────────────────────────────────

    private JPanel crearBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        p.setBackground(BG_DARK);

        JButton btnPdf = boton("📄 Exportar PDF", new Color(46, 125, 50));
        btnPdf.addActionListener(e -> exportarPdf());

        JButton btnCerrar = boton("Cerrar", new Color(113, 128, 150));
        btnCerrar.addActionListener(e -> dispose());

        p.add(btnPdf);
        p.add(btnCerrar);
        return p;
    }

    // ── Lógica ────────────────────────────────────────────────────────

    private void generarInforme() {
        LocalDate desde, hasta;
        try {
            desde = LocalDate.parse(txtDesde.getText().trim(), FMT);
            hasta = LocalDate.parse(txtHasta.getText().trim(), FMT);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Formato de fecha incorrecto (dd/MM/yyyy).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (desde.isAfter(hasta)) {
            JOptionPane.showMessageDialog(this, "La fecha de inicio debe ser anterior a la final.");
            return;
        }

        InformeEquipoDTO informe = service.informeEquipo(desde, hasta);
        modelTabla.setRowCount(0);

        List<ResumenEmpleadoDTO> filas = informe.getFilas();
        for (int i = 0; i < filas.size(); i++) {
            ResumenEmpleadoDTO f = filas.get(i);
            modelTabla.addRow(new Object[]{
                i + 1,
                f.getNombreEmpleado(),
                f.getNumEvaluaciones(),
                round(f.getMediaPuntualidad()),
                round(f.getMediaAtencionCliente()),
                round(f.getMediaTrabajoEquipo()),
                round(f.getMediaConocimientoProducto()),
                round(f.getMediaHigienePresentacion()),
                round(f.getMediaGeneral()),
                String.format("%d / %d (%.0f%%)",
                    f.getObjetivosCompletados(), f.getObjetivosTotales(), f.getPorcentajeObjetivos())
            });
        }

        panelGrafico.setDatos(filas);
        panelGrafico.repaint();

        if (filas.isEmpty()) {
            lblMedia.setText("  Sin evaluaciones completadas en el periodo.");
        } else {
            lblMedia.setText(String.format(
                "  %d empleados evaluados · Media global del equipo: %.1f / 100",
                filas.size(), informe.getMediaGlobalEquipo()));
        }
    }

    private void exportarPdf() {
        LocalDate desde, hasta;
        try {
            desde = LocalDate.parse(txtDesde.getText().trim(), FMT);
            hasta = LocalDate.parse(txtHasta.getText().trim(), FMT);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Genera el informe antes de exportar.");
            return;
        }
        try {
            File dir = new File(System.getProperty("user.home"), "Documents");
            if (!dir.exists()) dir = new File(System.getProperty("user.home"));
            java.nio.file.Path ruta = genDocs.generarInformeRendimiento(desde, hasta, dir.toPath());
            JOptionPane.showMessageDialog(this, "PDF generado:\n" + ruta,
                "Exportación completada", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private double round(double v) { return Math.round(v * 10.0) / 10.0; }

    private JButton boton(String txt, Color bg) {
        JButton b = new JButton(txt);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        return b;
    }

    // ══════════════════════════════════════════════════════════════════
    // GRÁFICO DE BARRAS PERSONALIZADO
    // ══════════════════════════════════════════════════════════════════

    static class GraficoBarra extends JPanel {

        private List<ResumenEmpleadoDTO> datos;

        GraficoBarra() {
            setBackground(BG_PANEL);
            setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR),
                new EmptyBorder(10, 12, 10, 12)));
        }

        void setDatos(List<ResumenEmpleadoDTO> datos) { this.datos = datos; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (datos == null || datos.isEmpty()) {
                g.setColor(TEXT_MUTED);
                g.setFont(new Font("SansSerif", Font.ITALIC, 12));
                g.drawString("Genera el informe para visualizar el gráfico.", 20, getHeight() / 2);
                return;
            }
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int margenIzq = 140;
            int margenDer = 20;
            int margenSup = 20;
            int margenInf = 30;
            int w = getWidth()  - margenIzq - margenDer;
            int h = getHeight() - margenSup - margenInf;

            // Eje Y
            g2.setColor(BORDER_COLOR);
            g2.drawLine(margenIzq, margenSup, margenIzq, margenSup + h);
            g2.drawLine(margenIzq, margenSup + h, margenIzq + w, margenSup + h);

            int n = Math.min(datos.size(), 10);
            int anchoBarra = Math.max(6, (w / n) - 8);

            Color[] paleta = {
                new Color(237, 137, 54), new Color(72, 187, 120), new Color(66, 153, 225),
                new Color(159, 122, 234), new Color(237, 100, 166), new Color(246, 173, 85),
                new Color(129, 230, 217), new Color(252, 129, 74), new Color(154, 230, 180),
                new Color(144, 205, 244)
            };

            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));

            for (int i = 0; i < n; i++) {
                ResumenEmpleadoDTO f = datos.get(i);
                double media = f.getMediaGeneral();
                int altBarra = (int) (h * (media / 100.0));
                int x = margenIzq + i * (w / n) + (w / n - anchoBarra) / 2;
                int y = margenSup + h - altBarra;

                Color c = paleta[i % paleta.length];
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(x, y, anchoBarra, altBarra, 4, 4));

                // Valor encima
                g2.setColor(TEXT_PRIMARY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                String val = String.format("%.1f", media);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(val, x + (anchoBarra - fm.stringWidth(val)) / 2, y - 3);

                // Nombre (rotado o truncado)
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                String nombre = truncar(f.getNombreEmpleado(), 14);
                g2.setColor(TEXT_MUTED);
                g2.drawString(nombre, margenIzq + i * (w / n), margenSup + h + 16);
            }
        }

        private String truncar(String s, int max) {
            if (s == null) return "";
            return s.length() <= max ? s : s.substring(0, max - 1) + "…";
        }
    }

    // ── Renderers ─────────────────────────────────────────────────────

    private static class DecimalRenderer extends DefaultTableCellRenderer {
        DecimalRenderer() { setHorizontalAlignment(JLabel.CENTER); }
        @Override public void setValue(Object v) {
            setText(v instanceof Double d ? String.format("%.1f", d) : "");
        }
    }

    private static class PuntuacionRenderer extends DefaultTableCellRenderer {
        PuntuacionRenderer() { setHorizontalAlignment(JLabel.CENTER); setFont(new Font("SansSerif", Font.BOLD, 12)); }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            if (v instanceof Double d) {
                setText(String.format("%.1f", d));
                if (!sel) {
                    setForeground(d >= 75 ? new Color(39, 103, 73) :
                                  d >= 50 ? new Color(133, 77, 14) :
                                            new Color(157, 23, 23));
                }
            }
            return this;
        }
    }
}
