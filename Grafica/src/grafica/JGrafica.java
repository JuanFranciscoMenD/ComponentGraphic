package grafica;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JGrafica extends JPanel {

    private JTextField textField;
    private JPanel graphPanel;

    public JGrafica() {
        setLayout(new BorderLayout());

        // Panel superior para el campo de texto y el botón
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        textField = new JTextField();
        textField.setPreferredSize(new Dimension(200,40));
        JButton button = new JButton();
        button.setIcon(new ImageIcon(getClass().getResource("/grafica/icon.png")));
        button.setSize(new Dimension(100,100));

        controlPanel.add(textField);
        controlPanel.add(button);

        // Panel central para la gráfica
        graphPanel = new JPanel();
        graphPanel.setLayout(new BorderLayout());

        add(controlPanel, BorderLayout.NORTH);
        add(graphPanel, BorderLayout.CENTER);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = textField.getText();
                if (!fileName.isEmpty()) {
                    graphPanel.removeAll();
                    Grafica grafica = new Grafica("Gráfica de Pastel", fileName);
                    graphPanel.add(grafica, BorderLayout.CENTER);
                    graphPanel.revalidate();
                    graphPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(JGrafica.this, "Por favor, ingrese un nombre de archivo.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private static class Grafica extends JPanel {

        public Grafica(String title, String nameFile) {
            this(title, readFile(nameFile));
        }

        public Grafica(String title, Map<String, Double> data) {
            // Crear dataset
            DefaultPieDataset dataset = new DefaultPieDataset();
            data.forEach(dataset::setValue);

            JFreeChart chart = ChartFactory.createPieChart(
                    title,
                    dataset,
                    true, true, false);

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(470, 270));
            chartPanel.setPopupMenu(null); // Desactivar el menú contextual de clic derecho
            this.add(chartPanel);
        }

        private static Map<String, Double> readFile(String nameFile) {
            Map<String, Double> divisions = new HashMap<>();
            String line;
            String[] parts;

            try (BufferedReader br = new BufferedReader(new FileReader(nameFile))) {
                // Leer el archivo línea por línea
                while ((line = br.readLine()) != null) {
                    parts = line.split(",");
                    if (parts.length == 2) {
                        divisions.put(parts[0], Double.valueOf(parts[1]));
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            }

            return divisions;
        }
    }
}
