import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class BuscaMinas extends JFrame implements ActionListener {

    final int FILAS = 7;
    final int COLUMNAS = 7;

    JButton[][] botones = new JButton[FILAS][COLUMNAS];
    boolean[][] hayMina = new boolean[FILAS][COLUMNAS];
    boolean[][] descubierto = new boolean[FILAS][COLUMNAS];
    int[][] numeros = new int[FILAS][COLUMNAS];

    int minas;
    int tiempoRestante;
    JLabel etiquetaTiempo = new JLabel("Tiempo: ");
    Timer temporizador;

    Random random = new Random();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BuscaMinas());
    }

    public BuscaMinas() {
        String[] opciones = {"Basico", "Intermedio", "Avanzado"};
        int op = JOptionPane.showOptionDialog(
                null,
                "Elige el nivel",
                "Busca Minas",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]
        );

        if (op == 0) {           // Básico
            minas = 8;
            tiempoRestante = 3 * 60;
        } else if (op == 1) {    // Intermedio
            minas = 12;
            tiempoRestante = 2 * 60;
        } else if (op == 2) {    // Avanzado
            minas = 14;
            tiempoRestante = 1 * 60;
        } else {                 // Si cierra el diálogo
            minas = 8;
            tiempoRestante = 3 * 60;
        }

        setTitle("Busca Minas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior con el tiempo
        JPanel panelSuperior = new JPanel();
        etiquetaTiempo.setFont(new Font("Arial", Font.BOLD, 16));
        actualizarEtiquetaTiempo();
        panelSuperior.add(etiquetaTiempo);
        add(panelSuperior, BorderLayout.NORTH);

        // Panel central con la cuadrícula de botones
        JPanel panelCentro = new JPanel(new GridLayout(FILAS, COLUMNAS));
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                JButton b = new JButton();
                b.setFont(new Font("Arial", Font.BOLD, 18));
                b.setFocusPainted(false);
                b.setMargin(new Insets(0, 0, 0, 0)); // más cuadradito
                b.setBackground(new Color(200, 200, 200)); // gris
                b.setActionCommand(i + "," + j);
                b.addActionListener(this);

                botones[i][j] = b;
                panelCentro.add(b);

                hayMina[i][j] = false;
                descubierto[i][j] = false;
                numeros[i][j] = 0;
            }
        }
        add(panelCentro, BorderLayout.CENTER);

        // Lógica de minas y números
        colocarMinas();
        calcularNumeros();

        // Temporizador
        temporizador = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tiempoRestante--;
                actualizarEtiquetaTiempo();
                if (tiempoRestante <= 0) {
                    temporizador.stop();
                    mostrarMinas();
                    deshabilitarTodo();
                    JOptionPane.showMessageDialog(null, "Se acabó el tiempo. Perdiste.");
                }
            }
        });
        temporizador.start();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    void actualizarEtiquetaTiempo() {
        etiquetaTiempo.setText("Tiempo: " + tiempoRestante + " s");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = ((JButton) e.getSource()).getActionCommand();
        String[] partes = cmd.split(",");
        int fila = Integer.parseInt(partes[0]);
        int col = Integer.parseInt(partes[1]);

        if (hayMina[fila][col]) {
            mostrarMinas();
            deshabilitarTodo();
            temporizador.stop();
            JOptionPane.showMessageDialog(this, "Pisaste una mina. Perdiste.");
        } else {
            revelar(fila, col);
            if (ganaste()) {
                mostrarMinas();
                deshabilitarTodo();
                temporizador.stop();
                JOptionPane.showMessageDialog(this, "¡Ganaste!");
            }
        }
    }

    void colocarMinas() {
        int colocadas = 0;
        while (colocadas < minas) {
            int f = random.nextInt(FILAS);
            int c = random.nextInt(COLUMNAS);
            if (!hayMina[f][c]) {
                hayMina[f][c] = true;
                colocadas++;
            }
        }
    }

    void calcularNumeros() {
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                if (hayMina[i][j]) {
                    numeros[i][j] = -1; // casilla con mina
                } else {
                    int cuenta = 0;
                    for (int fi = i - 1; fi <= i + 1; fi++) {
                        for (int co = j - 1; co <= j + 1; co++) {
                            if (esValida(fi, co) && hayMina[fi][co]) {
                                cuenta++;
                            }
                        }
                    }
                    numeros[i][j] = cuenta;
                }
            }
        }
    }

    // Revelar casillas mostrando SIEMPRE el número (0,1,2,3...)
    void revelar(int fila, int col) {
        if (!esValida(fila, col)) return;
        if (descubierto[fila][col]) return;
        if (hayMina[fila][col]) return; // por si entra por recursión

        descubierto[fila][col] = true;
        JButton b = botones[fila][col];
        b.setEnabled(false);
        b.setBackground(new Color(230, 230, 230)); // celda destapada

        int n = numeros[fila][col];

        // 🔹 Siempre mostramos el número, incluso si es 0
        b.setText(Integer.toString(n));
        if (n > 0) {
            b.setForeground(colorNumero(n));
        } else {
            b.setForeground(Color.DARK_GRAY); // color para el 0
        }

        // Si es 0, seguimos abriendo alrededor
        if (n == 0) {
            for (int fi = fila - 1; fi <= fila + 1; fi++) {
                for (int co = col - 1; co <= col + 1; co++) {
                    if (fi == fila && co == col) continue;
                    revelar(fi, co);
                }
            }
        }
    }

    // Colores estilo BuscaMinas
    Color colorNumero(int n) {
        switch (n) {
            case 1: return Color.BLUE;
            case 2: return new Color(0, 128, 0); // verde
            case 3: return Color.RED;
            case 4: return new Color(0, 0, 128);
            case 5: return new Color(128, 0, 0);
            case 6: return new Color(0, 128, 128);
            case 7: return Color.BLACK;
            case 8: return Color.GRAY;
            default: return Color.BLACK;
        }
    }

    boolean ganaste() {
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                if (!hayMina[i][j] && !descubierto[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    // Al terminar, mostramos minas y números
    void mostrarMinas() {
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                JButton b = botones[i][j];
                if (hayMina[i][j]) {
                    b.setText("X");
                    b.setForeground(Color.BLACK);
                    b.setBackground(Color.RED);
                } else {
                    int n = numeros[i][j];
                    b.setText(Integer.toString(n));
                    if (n > 0) {
                        b.setForeground(colorNumero(n));
                    } else {
                        b.setForeground(Color.DARK_GRAY);
                    }
                    b.setBackground(new Color(230, 230, 230));
                }
            }
        }
    }

    void deshabilitarTodo() {
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                botones[i][j].setEnabled(false);
            }
        }
    }

    boolean esValida(int fila, int col) {
        return fila >= 0 && fila < FILAS && col >= 0 && col < COLUMNAS;
    }
}

