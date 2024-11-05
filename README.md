# JGrafica: Visualización de Datos en Java

## Descripción
**JGrafica** es una aplicación de escritorio en Java que permite a los usuarios visualizar datos numéricos a través de gráficos interactivos utilizando la biblioteca JFreeChart. La aplicación permite seleccionar un archivo que contenga datos, y visualizar los datos seleccionados.

## Estructura del Código

### 1. Importaciones
El código comienza con las importaciones necesarias para utilizar las bibliotecas de gráficos y la interfaz gráfica:

```java

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

```

2. Clase Principal: JGrafica

La clase principal JGrafica extiende JPanel, permitiendo que esta clase sea un componente visual en la interfaz gráfica.

Variables
graphPanel: Panel donde se mostrará el gráfico.
chartType: Tipo de gráfico predeterminado (pastel).
datosSeleccionados: Mapa que almacena los datos seleccionados por el usuario.

Constructor
El constructor configura la interfaz gráfica y añade un botón para seleccionar archivos:

```java

public JGrafica() {
    setLayout(new BorderLayout());
    JPanel controlPanel = new JPanel(new FlowLayout());
    JButton button = new JButton();
    button.setIcon(new ImageIcon(getClass().getResource("/grafica/icon.png")));
    controlPanel.add(button);
    
    graphPanel = new JPanel(new BorderLayout());
    add(controlPanel, BorderLayout.NORTH);
    add(graphPanel, BorderLayout.CENTER);
    
    button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(JGrafica.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                graphPanel.removeAll();
                datosSeleccionados = Grafica.datosGenerales(Grafica.readFile(selectedFile.getAbsolutePath()));
                if (datosSeleccionados != null) {
                    updateGraph();
                }
            } else {
                JOptionPane.showMessageDialog(JGrafica.this, "No se seleccionó ningún archivo.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    });
}

```

3. Métodos Principales
getChartType y setChartType
Estos métodos permiten obtener y establecer el tipo de gráfico:

```java

public String getChartType() { return chartType; }
public void setChartType(String chartType) {
    this.chartType = chartType;
    updateGraph(); // Actualizar el gráfico cuando cambie el tipo
}
,,,
updateGraph
Este método actualiza el gráfico en función del tipo seleccionado y los datos disponibles:


private void updateGraph() {
    graphPanel.removeAll();
    Grafica grafica = new Grafica(chartType, "Gráfico", datosSeleccionados);
    graphPanel.add(grafica, BorderLayout.CENTER);
    graphPanel.revalidate();
    graphPanel.repaint();
}
```

4. Clase Interna: Grafica

La clase interna Grafica es responsable de crear y mostrar los gráficos:

```java
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
}
```

5. Métodos Estáticos para Manejo de Archivos

readFile
Este método lee un archivo línea por línea y extrae pares clave-valor que se almacenan en un mapa:

```java
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
```

datosGenerales
Este método muestra una ventana emergente con una tabla que permite al usuario seleccionar qué datos graficar:

```java
public static Map<String, Double> datosGenerales(Map<String, Double> datos) {
    DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Categoría", "Valor"}, 0);
    JTable table = new JTable(tableModel);
    datos.forEach((categoria, valor) -> tableModel.addRow(new Object[]{categoria, valor}));
    
    JScrollPane scrollPane = new JScrollPane(table);
    
    JDialog dialog = new JDialog();
    dialog.setTitle("Vista Previa de Datos");
    dialog.setModal(true);
    
    JButton acceptButton = new JButton("Aceptar");
    JButton cancelButton = new JButton("Cancelar");
    
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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
        dialog.dispose();
    });
    
    cancelButton.addActionListener((ActionEvent e) -> {
        JOptionPane.showMessageDialog(dialog, "Proceso Cancelado", "Información", JOptionPane.INFORMATION_MESSAGE);
        dialog.dispose();
    });

    dialog.pack();
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
    
    return selectedData[0];
}
```

## Video de Demostración

[![Ver la demostración en YouTube](https://img.youtube.com/vi/8vT4TwmKPSw/maxresdefault.jpg)](https://www.youtube.com/watch?v=8vT4TwmKPSw)

