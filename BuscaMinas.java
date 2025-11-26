import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class BuscaMinas extends JFrame implements ActionListener {

    final int FILAS = 9;
    final int COLUMNAS = 9;

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

        if (op == 0) {
            minas = 8;
            tiempoRestante = 3 * 60;
        } else if (op == 1) {
            minas = 12;
            tiempoRestante = 2 * 60;
        } else if (op == 2) {
            minas = 14;
            tiempoRestante = 1 * 60;
        } else {
            minas = 8;
            tiempoRestante = 3 * 60;
        }

        setTitle("Busca Minas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelSuperior = new JPanel();
        etiquetaTiempo.setFont(new Font("Arial", Font.BOLD, 16));
        actualizarEtiquetaTiempo();
        panelSuperior.add(etiquetaTiempo);
        add(panelSuperior, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new GridLayout(FILAS, COLUMNAS));
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                botones[i][j] = new JButton();
                botones[i][j].setFont(new Font("Arial", Font.BOLD, 16));
                botones[i][j].setFocusPainted(false);
                botones[i][j].setActionCommand(i + "," + j);
                botones[i][j].addActionListener(this);
                panelCentro.add(botones[i][j]);
                hayMina[i][j] = false;
                descubierto[i][j] = false;
                numeros[i][j] = 0;
            }
        }
        add(panelCentro, BorderLayout.CENTER);

        colocarMinas();
        calcularNumeros();

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
                    numeros[i][j] = -1;
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

    void revelar(int fila, int col) {
        if (!esValida(fila, col)) return;
        if (descubierto[fila][col]) return;

        descubierto[fila][col] = true;
        JButton b = botones[fila][col];
        b.setEnabled(false);

        if (numeros[fila][col] > 0) {
            b.setText(Integer.toString(numeros[fila][col]));
        } else {
            b.setText("");
            for (int fi = fila - 1; fi <= fila + 1; fi++) {
                for (int co = col - 1; co <= col + 1; co++) {
                    if (fi == fila && co == col) continue;
                    revelar(fi, co);
                }
            }
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

    void mostrarMinas() {
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                if (hayMina[i][j]) {
                    botones[i][j].setText("*");
                } else if (numeros[i][j] > 0) {
                    if (botones[i][j].isEnabled()) {
                        botones[i][j].setText("");
                    }
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
