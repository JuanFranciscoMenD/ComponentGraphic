Estructura del Código
1. Importaciones
El código comienza con importaciones de bibliotecas necesarias:
java
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

package grafica;: Define el paquete al que pertenece la clase.
Importaciones de JFreeChart: Estas son necesarias para crear gráficos de pastel y de barras.
Importaciones de Swing: Se utilizan para crear la interfaz gráfica de usuario (GUI).
Importaciones de IO: Para manejar la lectura de archivos.

2. Clase Principal JGrafica
java
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
                    if (datosSeleccionados != null) {
                        updateGraph();
                    }
                } else {
                    // Mensaje si no se seleccionó ningún archivo
                    JOptionPane.showMessageDialog(JGrafica.this, "No se seleccionó ningún archivo.", "Información", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }

JGrafica: Extiende JPanel, lo que permite que esta clase sea un componente visual.
Variables:
graphPanel: Panel donde se mostrará el gráfico.
chartType: Tipo de gráfico predeterminado (pastel).
datosSeleccionados: Mapa que almacenará los datos seleccionados por el usuario.
Constructor: Configura la interfaz gráfica y añade un botón para seleccionar archivos.
3. Métodos Principales
a. getChartType y setChartType
java
public String getChartType() { return chartType; }
public void setChartType(String chartType) {
    this.chartType = chartType;
    updateGraph(); // Actualizar el gráfico cuando cambie el tipo
}

Métodos para obtener y establecer el tipo de gráfico.
b. updateGraph
java
private void updateGraph() {
    graphPanel.removeAll();
    Grafica grafica = new Grafica(chartType, "Gráfico", datosSeleccionados);
    graphPanel.add(grafica, BorderLayout.CENTER);
    graphPanel.revalidate();
    graphPanel.repaint();
}

Este método actualiza el gráfico en función del tipo seleccionado y los datos disponibles.
4. Clase Interna Grafica
java
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

Constructor: Crea un gráfico basado en el tipo especificado (pastel o barras) y añade el gráfico al panel.
5. Métodos Estáticos para Manejo de Archivos
a. readFile
java
private static Map<String, Double> readFile(String nameFile) {
    Map<String, Double> divisions = new HashMap<>();
    String line;
    String[] parts;
    try (BufferedReader br = new BufferedReader(new FileReader(nameFile))) {
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

Este método lee un archivo línea por línea y extrae pares clave-valor que se almacenan en un mapa.
b. datosGenerales
java
public static Map<String, Double> datosGenerales(Map<String, Double> datos) {
    DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Categoría", "Valor"}, 0);
    JTable table = new JTable(tableModel);
    
    datos.forEach((categoria, valor) -> tableModel.addRow(new Object[]{categoria, valor}));
    
    JScrollPane scrollPane = new JScrollPane(table);
    
    JDialog dialog = new JDialog();
    dialog.setTitle("Vista Previa de Datos");
    dialog.setModal(true);
    dialog.setSize(500, 400);
    
    JButton acceptButton = new JButton("Aceptar");
    JButton cancelButton = new JButton("Cancelar");
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(acceptButton);
    buttonPanel.add(cancelButton);

    dialog.setLayout(new BorderLayout());
    dialog.add(scrollPane, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    final Map<String, Double>[] selectedData = new Map[]{null};

    acceptButton.addActionListener((ActionEvent e) -> {
        int[] selectedRows = table.getSelectedRows();
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

    cancelButton.addActionListener((ActionEvent e) -> {
        JOptionPane.showMessageDialog(dialog, "Proceso Cancelado", "Información", JOptionPane.INFORMATION_MESSAGE);
        dialog.dispose();
    });

    dialog.setVisible(true);
    
    return selectedData[0]; // Devuelve el mapa de datos seleccionados
}

Este método muestra una ventana emergente con una tabla que permite al usuario seleccionar qué datos graficar.
