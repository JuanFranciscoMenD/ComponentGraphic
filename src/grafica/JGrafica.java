package grafica;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.DefaultTableModel;

public class JGrafica extends JPanel {

    private JPanel graphPanel;
    private String chartType = "PASTEL"; // Tipo de gráfica predeterminado
    Map<String, Double> datosSeleccionados;

    public JGrafica() {
        setLayout(new BorderLayout());

        // Panel superior para el botón de selección de archivo
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        JButton button = new JButton();
        button.setIcon(new ImageIcon(getClass().getResource("/grafica/icon.png")));
        button.setSize(new Dimension(100, 100));
        controlPanel.add(button);

        // Panel central donde se añadirá el gráfico después de seleccionar los datos
        graphPanel = new JPanel();
        graphPanel.setLayout(new BorderLayout());

        // Añadir paneles al contenedor principal
        add(controlPanel, BorderLayout.NORTH);
        add(graphPanel, BorderLayout.CENTER);

        // Acción del botón para abrir un archivo y mostrar la vista previa de datos
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(JGrafica.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    // Limpia cualquier gráfico anterior del panel
                    graphPanel.removeAll();
                    // Llama al método para abrir el diálogo de selección de datos y obtener los seleccionados
                    datosSeleccionados = Grafica.datosGenerales(Grafica.readFile(selectedFile.getAbsolutePath()));
                    // Si hay datos seleccionados, muestra la gráfica
                    if (datosSeleccionados!= null) {
                        updateGraph();
                    }
                } else {
                    // Mensaje si no se seleccionó ningún archivo
                    JOptionPane.showMessageDialog(JGrafica.this, "No se seleccionó ningún archivo.", "Información", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
        updateGraph(); // Actualizar el gráfico cuando cambie el tipo
    }

    private void updateGraph() {
        // Lógica para actualizar el gráfico con el nuevo tipo
        graphPanel.removeAll();
        Grafica grafica = new Grafica(chartType, "Gráfico", datosSeleccionados);
        graphPanel.add(grafica, BorderLayout.CENTER);
        graphPanel.revalidate();
        graphPanel.repaint();
    }

    // Clase interna para gestionar la creación y visualización de la gráfica de pastel
    private static class Grafica extends JPanel {
        public Grafica(String chartType, String title, Map<String, Double> data) {
            JFreeChart chart = null;
            if (chartType.equalsIgnoreCase("PASTEL")) {
                DefaultPieDataset pieDataset = new DefaultPieDataset();
                data.forEach(pieDataset::setValue);
                chart = ChartFactory.createPieChart(title, pieDataset, true, true, false);
            } else if (chartType.equalsIgnoreCase("BARRAS")) {
                DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
                data.forEach((key, value) -> barDataset.addValue(value, key, key));
                chart = ChartFactory.createBarChart(title, "Categoría", "Valor", barDataset);
            }
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(470, 270));
            this.add(chartPanel);
        }

        // Método para leer el archivo y almacenar los datos en un mapa
        private static Map<String, Double> readFile(String nameFile) {
            Map<String, Double> divisions = new HashMap<>();
            String line;
            String[] parts;
            try (BufferedReader br = new BufferedReader(new FileReader(nameFile))) {
                // Leer el archivo línea por línea y extraer pares clave-valor
                while ((line = br.readLine())!= null) {
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

        // Método para mostrar una vista previa de datos y seleccionar las filas que el usuario quiere graficar
        public static Map<String, Double> datosGenerales(Map<String, Double> datos) {
            // Crear modelo de tabla y JTable para mostrar datos
            DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Categoría", "Valor"}, 0);
            JTable table = new JTable(tableModel);

            // Llenar la tabla con los datos de entrada
            datos.forEach((categoria, valor) -> tableModel.addRow(new Object[]{categoria, valor}));

            // Crear JScrollPane para añadir desplazamiento a la tabla
            JScrollPane scrollPane = new JScrollPane(table);

            // Crear el JDialog para la vista previa y selección de datos
            JDialog dialog = new JDialog();
            dialog.setTitle("Vista Previa de Datos");
            dialog.setModal(true); // Bloquea la ventana principal hasta cerrar el diálogo
            dialog.setSize(500, 400);
            dialog.setLocationRelativeTo(null); // Centrar el diálogo en la pantalla

            // Botones Aceptar y Cancelar
            JButton acceptButton = new JButton("Aceptar");
            JButton cancelButton = new JButton("Cancelar");

            // Panel para botones
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(acceptButton);
            buttonPanel.add(cancelButton);

            // Añadir la tabla y el panel de botones al diálogo
            dialog.setLayout(new BorderLayout());
            dialog.add(scrollPane, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            // Variable para almacenar los datos seleccionados
            final Map<String, Double>[] selectedData = new Map[]{null};

            // Acción del botón Aceptar
            acceptButton.addActionListener((ActionEvent e) -> {
                int[] selectedRows = table.getSelectedRows(); // Filas seleccionadas por el usuario
                if (selectedRows.length == 0) {
                    JOptionPane.showMessageDialog(dialog, "No hay filas seleccionadas.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Map<String, Double> selectedMap = new HashMap<>();
                for (int row : selectedRows) {
                    String category = (String) tableModel.getValueAt(row, 0);
                    Double value = (Double) tableModel.getValueAt(row, 1);
                    selectedMap.put(category, value);
                }
                selectedData[0] = selectedMap;
                dialog.dispose(); // Cerrar el diálogo
            });

            // Acción del botón Cancelar
            cancelButton.addActionListener((ActionEvent e) -> {
                JOptionPane.showMessageDialog(dialog, "Proceso Cancelado", "Información", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            });

            // Mostrar el diálogo
            dialog.setVisible(true);

            return selectedData[0]; // Devuelve el mapa de datos seleccionados
        }
    }
}