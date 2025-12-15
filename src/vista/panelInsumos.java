package vista;

import controlador.ControladorPicada;
import modelo.Insumo;
import modelo.Plato;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.stream.Collectors;

public class panelInsumos extends JPanel {

    private ControladorPicada controladorPicada;
    private VistaPrincipal mainFrame;

    private CardLayout parentCardLayout;
    private JPanel parentContentPanel;
    private JPanel MainPanel;
    private JTable tablaInsumos;
    private JTextField textoBuscar;
    private JComboBox comboFiltrarCategoria;
    private JButton agregarInsumoButton;
    private JButton editarInsumoButton;
    private JButton eliminarInsumoButton;
    private JButton volverButton;

    public panelInsumos(VistaPrincipal mainFrame, CardLayout cl, JPanel contentPanel) {
        this.mainFrame = mainFrame;
        this.parentCardLayout = cl;
        this.parentContentPanel = contentPanel;

        controladorPicada = new ControladorPicada();


        // Usar singleton compartido
        controladorPicada = ControladorPicada.getInstance();

        setLayout(new BorderLayout());
        add(MainPanel, BorderLayout.CENTER);

        // Suscribirse a cambios persistidos para mantener la tabla siempre actualizada
        controladorPicada.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (ControladorPicada.PROP_DATOS_PERSISTIDOS.equals(evt.getPropertyName())) {
                    SwingUtilities.invokeLater(() -> actualizarTabla());
                }
            }
        });

        actualizarTabla();

        // Agregar listener para filtrar mientras se escribe (case-insensitive)
        if (textoBuscar != null) {
            textoBuscar.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    filtrarPorCategoria();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    filtrarPorCategoria();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    filtrarPorCategoria();
                }
            });
        }
        if (comboFiltrarCategoria != null) {
            // Poblar combo inicialmente
            comboFiltrarCategoria.removeAllItems();
            comboFiltrarCategoria.addItem("Todas");
            List<String> cats = controladorPicada.obtenerCategoriasInsumos();
            if (cats != null) {
                for (String c : cats) comboFiltrarCategoria.addItem(c);
            }
            comboFiltrarCategoria.addItem("Nueva...");
            comboFiltrarCategoria.addActionListener(e -> filtrarPorCategoria());
        }

        if (agregarInsumoButton != null) {
            agregarInsumoButton.addActionListener(e -> agregarInsumo());
            agregarInsumoButton.setEnabled(true);
        }
        if (editarInsumoButton != null) {
            editarInsumoButton.addActionListener(e -> editarIsumo());
            editarInsumoButton.setEnabled(true);
        }
        if (eliminarInsumoButton != null) {
            eliminarInsumoButton.addActionListener(e -> eliminarInsumo());
            eliminarInsumoButton.setEnabled(true);
        }
        if (volverButton != null) {
            volverButton.addActionListener(e -> {
                parentCardLayout.show(parentContentPanel, "MENU_PRINCIPAL");
            });
        }
    }

    public void actualizarTabla() {
        controladorPicada.actualizarTablaInsumos(tablaInsumos);
    }

    public void filtrarInsumos(String texto) {
        if (tablaInsumos == null) return;

        List<Insumo> lista = controladorPicada.getInsumos();
        if (texto == null || texto.trim().isEmpty()) {
            // Mostrar todos si el filtro está vacío
            actualizarTabla();
            return;
        }

        String q = texto.trim().toLowerCase();

        List<Insumo> filtrados = lista.stream()
                .filter(i ->
                        (i.getIdInsumo() != null && i.getIdInsumo().toLowerCase().startsWith(q)) ||
                                (i.getNombre() != null && i.getNombre().toLowerCase().startsWith(q)) ||
                                (i.getUnidadMedida() != null && i.getUnidadMedida().toLowerCase().startsWith(q))
                )
                .collect(Collectors.toList());

        String[] columnas = {"ID", "Nombre", "Categoría", "Unidad Medida", "Stock Mínimo", "Stock Actual", "Precio Unitario"};
        Object[][] datos = new Object[filtrados.size()][7];

        for (int i = 0; i < filtrados.size(); i++) {
            Insumo insumo = filtrados.get(i);
            datos[i][0] = insumo.getIdInsumo();
            datos[i][1] = insumo.getNombre();
            datos[i][2] = insumo.getCategoria();
            datos[i][3] = insumo.getUnidadMedida();
            datos[i][4] = insumo.getStockMinimo();
            datos[i][5] = insumo.getStockActual();
            datos[i][6] = insumo.getPrecioUnitario();
        }

        tablaInsumos.setModel(new DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    public void filtrarPorCategoria() {
        if (tablaInsumos == null || comboFiltrarCategoria == null) return;

        String seleccion = (String) comboFiltrarCategoria.getSelectedItem();
        if (seleccion == null) seleccion = "Todas";

        List<Insumo> lista = controladorPicada.getInsumos();
        if (lista == null) lista = java.util.Collections.emptyList();

        String q = "";
        if (textoBuscar != null && textoBuscar.getText() != null) {
            q = textoBuscar.getText().trim().toLowerCase();
        }
        final String filtroTexto = q;
        final boolean usarTexto = !filtroTexto.isEmpty();
        final boolean filtrarTodas = "Todas".equalsIgnoreCase(seleccion);

        String finalSeleccion = seleccion;
        List<Insumo> filtrados = lista.stream()
                .filter(i -> {
                    if (i == null) return false;
                    // filtro por categoría (si no es "Todas")
                    if (!filtrarTodas) {
                        if (i.getCategoria() == null || !i.getCategoria().equalsIgnoreCase(finalSeleccion)) {
                            return false;
                        }
                    }
                    // si no hay texto, ya cumple
                    if (!usarTexto) return true;

                    // filtro por texto usando startsWith (case-insensitive) en id, nombre o unidad
                    boolean matchId = i.getIdInsumo() != null && i.getIdInsumo().toLowerCase().startsWith(filtroTexto);
                    boolean matchNombre = i.getNombre() != null && i.getNombre().toLowerCase().startsWith(filtroTexto);
                    boolean matchUnidad = i.getUnidadMedida() != null && i.getUnidadMedida().toLowerCase().startsWith(filtroTexto);
                    return matchId || matchNombre || matchUnidad;
                })
                .collect(Collectors.toList());

        String[] columnas = {"ID", "Nombre", "Categoría", "Unidad Medida", "Stock Mínimo", "Stock Actual", "Precio Unitario"};
        Object[][] datos = new Object[filtrados.size()][7];

        for (int i = 0; i < filtrados.size(); i++) {
            Insumo insumo = filtrados.get(i);
            datos[i][0] = insumo.getIdInsumo();
            datos[i][1] = insumo.getNombre();
            datos[i][2] = insumo.getCategoria();
            datos[i][3] = insumo.getUnidadMedida();
            datos[i][4] = insumo.getStockMinimo();
            datos[i][5] = insumo.getStockActual();
            datos[i][6] = insumo.getPrecioUnitario();
        }

        tablaInsumos.setModel(new DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    public void agregarInsumo() {
        try {
            List<String> categoriasExistentes = controladorPicada.obtenerCategoriasInsumos();
            JComboBox<String> cbCat = new JComboBox<>();
            cbCat.addItem("Nueva...");
            if (categoriasExistentes != null) {
                for (String c : categoriasExistentes) cbCat.addItem(c);
                if (!categoriasExistentes.isEmpty()) cbCat.setSelectedIndex(1);
            }

            JTextField tfId = new JTextField();
            JTextField tfNombre = new JTextField();
            JTextField tfUnidad = new JTextField();
            JTextField tfStockMin = new JTextField();
            // Mostrar precio pero no editable, con mensaje informativo
            JTextField tfPrecio = new JTextField("Se asignará al recibir");
            tfPrecio.setEnabled(false);

            JTextField tfNuevaCat = new JTextField();
            tfNuevaCat.setEnabled("Nueva...".equals(cbCat.getSelectedItem()));

            cbCat.addActionListener(e -> {
                boolean nueva = "Nueva...".equals(cbCat.getSelectedItem());
                tfNuevaCat.setEnabled(nueva);
                if (!nueva) tfNuevaCat.setText("");
            });

            JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
            panel.add(new JLabel("ID:"));
            panel.add(tfId);
            panel.add(new JLabel("Nombre:"));
            panel.add(tfNombre);
            panel.add(new JLabel("Categoría:"));
            panel.add(cbCat);
            panel.add(new JLabel("Nueva categoría (si aplica):"));
            panel.add(tfNuevaCat);
            panel.add(new JLabel("Unidad (ej: kg):"));
            panel.add(tfUnidad);
            panel.add(new JLabel("Stock mínimo (entero):"));
            panel.add(tfStockMin);
            panel.add(new JLabel("Precio unitario (se establecerá al recibir):"));
            panel.add(tfPrecio);

            int opt = JOptionPane.showConfirmDialog(this, panel, "Nuevo Insumo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opt != JOptionPane.OK_OPTION) return;

            String id = tfId.getText() != null ? tfId.getText().trim() : "";
            String nombre = tfNombre.getText() != null ? tfNombre.getText().trim() : "";
            String unidad = tfUnidad.getText() != null ? tfUnidad.getText().trim() : "";
            String categoria = cbCat.getSelectedItem() != null ? cbCat.getSelectedItem().toString() : "";

            if (id.isEmpty() || nombre.isEmpty() || unidad.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID, Nombre y Unidad son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if ("Nueva...".equals(categoria)) {
                String nueva = tfNuevaCat.getText() != null ? tfNuevaCat.getText().trim() : "";
                if (nueva.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Ingrese el nombre de la nueva categoría.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                categoria = nueva;
            }

            int stockMinimo;
            try {
                stockMinimo = Integer.parseInt(tfStockMin.getText().trim());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Stock mínimo inválido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Precio inicial 0.0; se actualizará cuando se procese la recepcion en inventario
            double precioUnitario = 0.0;
            int stockActual = 0; // iniciar en 0

            controladorPicada.agregarInsumos(id, nombre, categoria, unidad, stockMinimo, stockActual, precioUnitario);

            // Actualizar tabla y repoblar combo de categorías
            actualizarTabla();
            if (comboFiltrarCategoria != null) {
                comboFiltrarCategoria.removeAllItems();
                comboFiltrarCategoria.addItem("Todas");
                List<String> cats = controladorPicada.obtenerCategoriasInsumos();
                if (cats != null) {
                    for (String c : cats) comboFiltrarCategoria.addItem(c);
                }
                comboFiltrarCategoria.addItem("Nueva...");
                comboFiltrarCategoria.setSelectedItem(categoria);
            }

            JOptionPane.showMessageDialog(this, "Insumo agregado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al agregar insumo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarIsumo() {
        int row = tablaInsumos.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un insumo para editar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener id desde la tabla (columna 0) y buscar el objeto en el controlador
        String id = tablaInsumos.getModel().getValueAt(row, 0) != null ? tablaInsumos.getModel().getValueAt(row, 0).toString() : null;
        if (id == null || id.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Insumo insumo = controladorPicada.getInsumos().stream()
                .filter(i -> id.equals(i.getIdInsumo()))
                .findFirst()
                .orElse(null);

        if (insumo == null) {
            JOptionPane.showMessageDialog(this, "Insumo no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Preparar campos prellenados. NOTA: Se evita cambiar el ID para no romper referencias.
            JTextField tfId = new JTextField(insumo.getIdInsumo());
            tfId.setEnabled(false);

            JTextField tfNombre = new JTextField(insumo.getNombre());
            List<String> categoriasExistentes = controladorPicada.obtenerCategoriasInsumos();
            JComboBox<String> cbCat = new JComboBox<>();
            cbCat.addItem("Nueva...");
            if (categoriasExistentes != null) {
                for (String c : categoriasExistentes) cbCat.addItem(c);
            }
            if (insumo.getCategoria() != null && !insumo.getCategoria().trim().isEmpty()) {
                cbCat.setSelectedItem(insumo.getCategoria());
                // Si la categoría actual no está en la lista, añadirla en posición 1
                boolean presente = false;
                for (int i = 0; i < cbCat.getItemCount(); i++) {
                    if (insumo.getCategoria().equals(cbCat.getItemAt(i))) {
                        presente = true;
                        break;
                    }
                }
                if (!presente) cbCat.insertItemAt(insumo.getCategoria(), 1);
                cbCat.setSelectedItem(insumo.getCategoria());
            } else {
                if (cbCat.getItemCount() > 1) cbCat.setSelectedIndex(1);
            }

            JTextField tfNuevaCat = new JTextField();
            tfNuevaCat.setEnabled("Nueva...".equals(cbCat.getSelectedItem()));
            cbCat.addActionListener(e -> tfNuevaCat.setEnabled("Nueva...".equals(cbCat.getSelectedItem())));

            JTextField tfUnidad = new JTextField(insumo.getUnidadMedida());
            JTextField tfStockMin = new JTextField(String.valueOf(insumo.getStockMinimo()));
            JTextField tfStockActual = new JTextField(String.valueOf(insumo.getStockActual()));

            // Precio se muestra pero no es editable
            JTextField tfPrecio = new JTextField(String.valueOf(insumo.getPrecioUnitario()));
            tfPrecio.setEnabled(false);

            JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
            panel.add(new JLabel("ID (no editable):"));
            panel.add(tfId);
            panel.add(new JLabel("Nombre:"));
            panel.add(tfNombre);
            panel.add(new JLabel("Categoría:"));
            panel.add(cbCat);
            panel.add(new JLabel("Nueva categoría (si aplica):"));
            panel.add(tfNuevaCat);
            panel.add(new JLabel("Unidad (ej: kg):"));
            panel.add(tfUnidad);
            panel.add(new JLabel("Stock mínimo (entero):"));
            panel.add(tfStockMin);
            panel.add(new JLabel("Stock actual (enfocado, se guarda tal cual):"));
            panel.add(tfStockActual);
            panel.add(new JLabel("Precio unitario (no editable):"));
            panel.add(tfPrecio);

            int opt = JOptionPane.showConfirmDialog(this, panel, "Editar Insumo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opt != JOptionPane.OK_OPTION) return;

            String nombre = tfNombre.getText() != null ? tfNombre.getText().trim() : "";
            String unidad = tfUnidad.getText() != null ? tfUnidad.getText().trim() : "";
            String categoria = cbCat.getSelectedItem() != null ? cbCat.getSelectedItem().toString() : "";

            if (nombre.isEmpty() || unidad.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre y unidad no pueden quedar vacíos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if ("Nueva...".equals(categoria)) {
                String nueva = tfNuevaCat.getText() != null ? tfNuevaCat.getText().trim() : "";
                if (nueva.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Indique el nombre de la nueva categoría.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                categoria = nueva;
                // repoblar combo filtro más abajo
            }

            int stockMinimo;
            int stockActual;
            try {
                stockMinimo = Integer.parseInt(tfStockMin.getText().trim());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Stock mínimo inválido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                stockActual = Integer.parseInt(tfStockActual.getText().trim());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Stock actual inválido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Llamar al controlador para actualizar y persistir
            controladorPicada.actualizarInsumo(insumo.getIdInsumo(), nombre, categoria, unidad, stockMinimo, stockActual);

            // Actualizar vista
            actualizarTabla();
            if (comboFiltrarCategoria != null) {
                comboFiltrarCategoria.removeAllItems();
                comboFiltrarCategoria.addItem("Todas");
                List<String> cats = controladorPicada.obtenerCategoriasInsumos();
                if (cats != null) for (String c : cats) comboFiltrarCategoria.addItem(c);
                comboFiltrarCategoria.addItem("Nueva...");
                comboFiltrarCategoria.setSelectedItem(categoria);
            }

            JOptionPane.showMessageDialog(this, "Insumo actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al editar insumo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarInsumo() {
        int row = tablaInsumos.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un insumo para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = tablaInsumos.getModel().getValueAt(row, 0) != null ? tablaInsumos.getModel().getValueAt(row, 0).toString() : "";
        String nombre = tablaInsumos.getModel().getValueAt(row, 1) != null ? tablaInsumos.getModel().getValueAt(row, 1).toString() : id;

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Confirma eliminar el insumo: " + nombre + " (" + id + ")?\nSe eliminarán también sus referencias en órdenes.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            controladorPicada.eliminarInsumo(id);

            // Actualizar vista
            actualizarTabla();
            if (comboFiltrarCategoria != null) {
                comboFiltrarCategoria.removeAllItems();
                comboFiltrarCategoria.addItem("Todas");
                List<String> cats = controladorPicada.obtenerCategoriasInsumos();
                if (cats != null) for (String c : cats) comboFiltrarCategoria.addItem(c);
                comboFiltrarCategoria.addItem("Nueva...");
                comboFiltrarCategoria.setSelectedIndex(0);
            }

            JOptionPane.showMessageDialog(this, "Insumo eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar insumo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}



