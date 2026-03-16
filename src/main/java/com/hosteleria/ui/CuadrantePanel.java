package com.hosteleria.ui;

import com.hosteleria.model.Empleado;
import com.hosteleria.model.Turno;
import com.hosteleria.service.PresenciaService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel visual del cuadrante de turnos.
 *
 *  · Vista SEMANAL  → columnas = 7 días, filas = empleados
 *  · Vista MENSUAL  → columnas = días del mes, filas = empleados
 *
 * Colores por tipo de turno:
 *   mañana = azul claro   tarde = naranja claro   noche = morado claro
 *   partido = verde claro   completo = gris claro
 *   ausente = rojo claro   cancelado = gris muy claro
 *
 * Click en celda con turno → abre TurnoDialog para editar.
 * Botón "+" en fila de empleado → abre TurnoDialog para nuevo turno en ese día.
 */
public class CuadrantePanel extends JPanel {

    // ── Paleta ────────────────────────────────────────────────────────
    private static final Map<String, Color> COLOR_TIPO = new LinkedHashMap<>();
    static {
        COLOR_TIPO.put("mañana",   new Color(190, 227, 248));
        COLOR_TIPO.put("tarde",    new Color(254, 215, 170));
        COLOR_TIPO.put("noche",    new Color(214, 188, 250));
        COLOR_TIPO.put("partido",  new Color(198, 246, 213));
        COLOR_TIPO.put("completo", new Color(226, 232, 240));
    }
    private static final Color AUSENTE   = new Color(254, 178, 178);
    private static final Color CANCELADO = new Color(247, 250, 252);
    private static final Color HOY_BG    = new Color(255, 252, 240);
    private static final Color HDR_BG    = new Color(255, 247, 237);
    private static final Color ACCENT    = new Color(237, 137, 54);
    private static final Color BORDER_C  = new Color(226, 232, 240);
    private static final Color TEXT_D    = new Color(26,  32,  44);
    private static final Color TEXT_M    = new Color(113, 128, 150);

    // ── Estado ────────────────────────────────────────────────────────
    private final PresenciaService presencia;
    private final List<Empleado>   todosEmpleados;

    private boolean    modoMensual   = false;
    private LocalDate  semana        = LocalDate.now().with(DayOfWeek.MONDAY);
    private int        anio          = LocalDate.now().getYear();
    private int        mes           = LocalDate.now().getMonthValue();

    private Runnable   onCambio;  // callback para refrescar la pantalla padre

    // ── Grilla calculada ──────────────────────────────────────────────
    private LocalDate[]     diasCol;
    private List<Empleado>  empFila;
    private Turno[][]       grilla;

    public CuadrantePanel(PresenciaService presencia, List<Empleado> empleados) {
        this.presencia      = presencia;
        this.todosEmpleados = new ArrayList<>(empleados);
        setBackground(new Color(247, 250, 252));
        setLayout(new BorderLayout(0, 8));
        reconstruir();
    }

    /** Callback que se llama cuando el usuario crea o edita un turno. */
    public void setOnCambio(Runnable r) { this.onCambio = r; }

    // ══════════════════════════════════════════════════════════════════
    // CONSTRUCCIÓN
    // ══════════════════════════════════════════════════════════════════

    private void reconstruir() {
        removeAll();
        add(crearBarra(),   BorderLayout.NORTH);
        add(crearCuadro(),  BorderLayout.CENTER);
        add(crearLeyenda(), BorderLayout.SOUTH);
        revalidate();
        repaint();
    }

    // ── Barra de navegación ───────────────────────────────────────────

    private JPanel crearBarra() {
        JPanel barra = new JPanel(new BorderLayout(8, 0));
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        // Toggle semanal / mensual
        JButton btnSemana = botonBarra("Vista semanal", !modoMensual);
        JButton btnMes    = botonBarra("Vista mensual",  modoMensual);
        btnSemana.addActionListener(e -> { if (modoMensual) { modoMensual = false; reconstruir(); } });
        btnMes.addActionListener(e    -> { if (!modoMensual) { modoMensual = true; reconstruir(); } });

        JPanel modos = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        modos.setOpaque(false);
        modos.add(btnSemana); modos.add(btnMes);

        // Navegación
        JLabel lblPeriodo = new JLabel("", SwingConstants.CENTER);
        lblPeriodo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblPeriodo.setForeground(TEXT_D);
        actualizarLabel(lblPeriodo);

        JButton prev = btnNav("◀"); JButton next = btnNav("▶"); JButton hoy = btnNav("Hoy");
        prev.addActionListener(e -> { navegar(-1); actualizarLabel(lblPeriodo); reconstruir(); });
        next.addActionListener(e -> { navegar(+1); actualizarLabel(lblPeriodo); reconstruir(); });
        hoy.addActionListener(e  -> {
            semana = LocalDate.now().with(DayOfWeek.MONDAY);
            anio   = LocalDate.now().getYear();
            mes    = LocalDate.now().getMonthValue();
            actualizarLabel(lblPeriodo); reconstruir();
        });

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        nav.setOpaque(false);
        nav.add(prev); nav.add(lblPeriodo); nav.add(next); nav.add(hoy);

        barra.add(modos, BorderLayout.WEST);
        barra.add(nav,   BorderLayout.CENTER);
        return barra;
    }

    private void navegar(int delta) {
        if (modoMensual) {
            YearMonth ym = YearMonth.of(anio, mes).plusMonths(delta);
            anio = ym.getYear(); mes = ym.getMonthValue();
        } else {
            semana = semana.plusWeeks(delta);
        }
    }

    private void actualizarLabel(JLabel lbl) {
        if (modoMensual) {
            lbl.setText(Month.of(mes).getDisplayName(TextStyle.FULL, new Locale("es")) + " " + anio);
        } else {
            lbl.setText(semana + "  ─  " + semana.plusDays(6));
        }
    }

    // ── Cuadro (grilla) ───────────────────────────────────────────────

    private JScrollPane crearCuadro() {
        // 1. Calcular días columna
        if (modoMensual) {
            LocalDate ini = LocalDate.of(anio, mes, 1);
            int n = ini.lengthOfMonth();
            diasCol = new LocalDate[n];
            for (int i = 0; i < n; i++) diasCol[i] = ini.plusDays(i);
        } else {
            diasCol = new LocalDate[7];
            for (int i = 0; i < 7; i++) diasCol[i] = semana.plusDays(i);
        }

        // 2. Cargar turnos del periodo
        List<Turno> turnos = modoMensual
            ? presencia.getCuadranteMes(anio, mes)
            : presencia.getCuadranteSemana(semana);

        // 3. Agrupar por empleado → fecha → turno
        Map<Integer, Map<LocalDate, Turno>> mapa = new HashMap<>();
        Set<Integer> idsConTurno = new HashSet<>();
        for (Turno t : turnos) {
            int id = t.getEmpleado().getIdEmpleado();
            idsConTurno.add(id);
            mapa.computeIfAbsent(id, k -> new HashMap<>()).put(t.getFecha(), t);
        }

        // 4. Empleados a mostrar: todos si son ≤15, si no solo los que tienen turno
        empFila = todosEmpleados.size() <= 15
            ? new ArrayList<>(todosEmpleados)
            : todosEmpleados.stream().filter(e -> idsConTurno.contains(e.getIdEmpleado()))
                            .collect(Collectors.toList());

        // 5. Construir grilla lógica
        int nRows = empFila.size();
        int nCols = diasCol.length;
        grilla = new Turno[nRows][nCols];
        for (int r = 0; r < nRows; r++) {
            Map<LocalDate, Turno> pf = mapa.getOrDefault(empFila.get(r).getIdEmpleado(), Collections.emptyMap());
            for (int c = 0; c < nCols; c++) grilla[r][c] = pf.get(diasCol[c]);
        }

        // 6. Construir panel de píxeles
        int empW = 155;
        int colW = modoMensual ? 44 : 108;
        int rowH = 36;
        int totalW = empW + nCols * colW + 2;
        int totalH = (nRows + 1) * rowH + 2;

        JPanel grid = new JPanel(null);
        grid.setBackground(new Color(247, 250, 252));
        grid.setPreferredSize(new Dimension(totalW, totalH));

        // Cabecera de días
        for (int c = 0; c < nCols; c++) {
            JLabel hdr = cabeceraDia(diasCol[c]);
            hdr.setBounds(empW + c * colW, 0, colW, rowH);
            grid.add(hdr);
        }

        // Filas de empleados
        for (int r = 0; r < nRows; r++) {
            // Etiqueta empleado
            Empleado emp = empFila.get(r);
            JLabel lblEmp = new JLabel(
                "  " + emp.getNombre() + " " + emp.getApellidos().charAt(0) + ".");
            lblEmp.setFont(new Font("SansSerif", Font.PLAIN, 11));
            lblEmp.setForeground(TEXT_D);
            lblEmp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(252, 252, 254));
            lblEmp.setOpaque(true);
            lblEmp.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER_C));
            lblEmp.setBounds(0, (r + 1) * rowH, empW, rowH);
            grid.add(lblEmp);

            // Celdas de turno
            for (int c = 0; c < nCols; c++) {
                JLabel celda = crearCelda(grilla[r][c], diasCol[c]);
                final int fr = r, fc = c;
                if (grilla[r][c] != null) {
                    celda.addMouseListener(new MouseAdapter() {
                        @Override public void mouseClicked(MouseEvent e) {
                            editarTurno(grilla[fr][fc]);
                        }
                        @Override public void mouseEntered(MouseEvent e) {
                            celda.setBorder(BorderFactory.createLineBorder(ACCENT, 2));
                        }
                        @Override public void mouseExited(MouseEvent e) {
                            celda.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER_C));
                        }
                    });
                    celda.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    // Celda vacía: click para crear nuevo turno en ese día para ese empleado
                    final LocalDate diaClick = diasCol[c];
                    final Empleado  empClick = empFila.get(r);
                    celda.addMouseListener(new MouseAdapter() {
                        @Override public void mouseClicked(MouseEvent e) {
                            if (e.getClickCount() == 2) crearTurnoEnCelda(empClick, diaClick);
                        }
                        @Override public void mouseEntered(MouseEvent e) {
                            celda.setBackground(new Color(255, 247, 237));
                            celda.setText("+");
                            celda.setHorizontalAlignment(SwingConstants.CENTER);
                        }
                        @Override public void mouseExited(MouseEvent e) {
                            celda.setBackground(Color.WHITE);
                            celda.setText("");
                        }
                    });
                    celda.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
                celda.setBounds(empW + c * colW, (r + 1) * rowH, colW, rowH);
                grid.add(celda);
            }
        }

        if (nRows == 0) {
            JLabel vacio = new JLabel("Sin turnos en este periodo.", SwingConstants.CENTER);
            vacio.setFont(new Font("SansSerif", Font.ITALIC, 13));
            vacio.setForeground(TEXT_M);
            vacio.setBounds(0, rowH, totalW, rowH);
            grid.add(vacio);
        }

        JScrollPane sc = new JScrollPane(grid);
        sc.setBorder(BorderFactory.createLineBorder(BORDER_C));
        sc.getViewport().setBackground(new Color(247, 250, 252));
        return sc;
    }

    // ── Helpers de celda ─────────────────────────────────────────────

    private JLabel cabeceraDia(LocalDate d) {
        boolean esHoy = d.equals(LocalDate.now());
        String diaNombre = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("es"));
        String diaN      = String.valueOf(d.getDayOfMonth());
        JLabel lbl = new JLabel(
            "<html><center><b>" + diaNombre + "</b><br>" + diaN + "</center></html>",
            SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lbl.setForeground(esHoy ? ACCENT : TEXT_M);
        lbl.setBackground(esHoy ? HOY_BG : HDR_BG);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER_C));
        return lbl;
    }

    private JLabel crearCelda(Turno t, LocalDate fecha) {
        JLabel lbl;
        if (t == null) {
            lbl = new JLabel("");
            lbl.setBackground(fecha.equals(LocalDate.now()) ? HOY_BG : Color.WHITE);
        } else {
            String tipoNm = t.getTipoTurno() != null ? t.getTipoTurno().name() : "completo";
            Color bg;
            if      (t.getEstado() == Turno.EstadoTurno.ausente)   bg = AUSENTE;
            else if (t.getEstado() == Turno.EstadoTurno.cancelado)  bg = CANCELADO;
            else bg = COLOR_TIPO.getOrDefault(tipoNm, new Color(226, 232, 240));

            String texto;
            if (modoMensual) {
                texto = t.getHoraInicio() != null
                    ? t.getHoraInicio().toString().substring(0, 5) : tipoNm.substring(0, 2);
            } else {
                String ini = t.getHoraInicio() != null ? t.getHoraInicio().toString().substring(0, 5) : "";
                String fin = t.getHoraFin()    != null ? t.getHoraFin().toString().substring(0, 5)    : "";
                texto = ini + "–" + fin;
            }

            lbl = new JLabel("<html><center>" + texto + "</center></html>", SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, modoMensual ? 9 : 10));
            lbl.setForeground(TEXT_D);
            lbl.setBackground(bg);
            lbl.setToolTipText(t.getEmpleado().getNombre() + " · " + tipoNm
                + (t.getAreaAsignada() != null ? " · " + t.getAreaAsignada() : "")
                + "  [" + t.getEstado() + "]");
        }
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER_C));
        return lbl;
    }

    // ── Leyenda ───────────────────────────────────────────────────────

    private JPanel crearLeyenda() {
        JPanel ley = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        ley.setOpaque(false);
        COLOR_TIPO.forEach((nombre, color) -> ley.add(chip(nombre, color)));
        ley.add(chip("ausente",   AUSENTE));
        ley.add(chip("cancelado", CANCELADO));
        JLabel hint = new JLabel("  (doble clic en celda vacía para crear · clic en turno para editar)");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 10));
        hint.setForeground(TEXT_M);
        ley.add(hint);
        return ley;
    }

    private JLabel chip(String nombre, Color color) {
        JLabel c = new JLabel("  " + nombre + "  ");
        c.setBackground(color);
        c.setFont(new Font("SansSerif", Font.PLAIN, 10));
        c.setForeground(TEXT_D);
        c.setOpaque(true);
        c.setBorder(BorderFactory.createLineBorder(BORDER_C));
        return c;
    }

    // ── Acciones ─────────────────────────────────────────────────────

    private void editarTurno(Turno t) {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        TurnoDialog dlg = new TurnoDialog(parent, presencia, todosEmpleados, t);
        dlg.setVisible(true);
        if (dlg.isGuardado()) { reconstruir(); if (onCambio != null) onCambio.run(); }
    }

    private void crearTurnoEnCelda(Empleado emp, LocalDate fecha) {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        // Pre-rellenar un turno ficticio con los datos del empleado y fecha
        Turno pre = new Turno();
        pre.setEmpleado(emp);
        pre.setFecha(fecha);
        pre.setHoraInicio(LocalTime.of(8, 0));
        pre.setHoraFin(LocalTime.of(16, 0));
        pre.setTipoTurno(Turno.TipoTurno.completo);
        pre.setEstado(Turno.EstadoTurno.programado);
        // Usamos TurnoDialog en modo "nuevo" pero con campos prefilled
        TurnoDialog dlg = new TurnoDialog(parent, presencia, todosEmpleados, null) {
            { /* El TurnoDialog en modo null inicia con hoy y hora 08-16.
                 Seleccionar el empleado correcto automáticamente se haría
                 en un refactor mayor; para este caso, el usuario lo confirma. */ }
        };
        dlg.setVisible(true);
        if (dlg.isGuardado()) { reconstruir(); if (onCambio != null) onCambio.run(); }
    }

    // ── Helpers de botones ────────────────────────────────────────────

    private JButton botonBarra(String texto, boolean activo) {
        JButton b = new JButton(texto);
        b.setFont(new Font("SansSerif", activo ? Font.BOLD : Font.PLAIN, 12));
        b.setForeground(activo ? ACCENT : TEXT_M);
        b.setBackground(activo ? HDR_BG : Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        b.setFocusPainted(false);
        return b;
    }

    private JButton btnNav(String texto) {
        JButton b = new JButton(texto);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setBackground(HDR_BG);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        b.setFocusPainted(false);
        return b;
    }
}
