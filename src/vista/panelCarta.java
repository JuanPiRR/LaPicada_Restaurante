package vista;

import controlador.ControladorPicada;
import modelo.Plato;
import modelo.Recepcion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class panelCarta extends JPanel {

    private ControladorPicada controladorPicada;
    private VistaPrincipal mainFrame;

    private CardLayout parentCardLayout;
    private JPanel parentContentPanel;
    private JPanel MainPanel;
    private JLabel banner;
    private JTable tablaCarta;
    private JTextField textoBuscar;
    private JComboBox comboFiltrarCategoria;
    private JButton agregarPlatoButton;
    private JButton eliminarPlatoButton;
    private JButton volverButton;
    private JButton editarPlatoButton;

    public panelCarta(VistaPrincipal mainFrame, CardLayout cl, JPanel contentPanel) {
        this.mainFrame = mainFrame;
        this.parentCardLayout = cl;
        this.parentContentPanel = contentPanel;

        controladorPicada = new ControladorPicada();

        setLayout(new BorderLayout());
        add(MainPanel, BorderLayout.CENTER);

        if (comboFiltrarCategoria != null) {
            // poblar con categorías únicas
            comboFiltrarCategoria.removeAllItems();
            comboFiltrarCategoria.addItem("Todas");
            List<String> categorias = controladorPicada.obtenerCategoriasUnicas();
            if (categorias != null) {
                for (String c : categorias) comboFiltrarCategoria.addItem(c);
            }
            comboFiltrarCategoria.setSelectedIndex(0);
        }

        // Listener para buscar por texto (DocumentListener)
        if (textoBuscar != null) {
            textoBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    buscarPlato(textoBuscar.getText());
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    buscarPlato(textoBuscar.getText());
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    buscarPlato(textoBuscar.getText());
                }
            });
        }

        // Listener del combo para aplicar el mismo filtro combinado con el texto
        if (comboFiltrarCategoria != null) {
            comboFiltrarCategoria.addActionListener(e -> buscarPlato(textoBuscar != null ? textoBuscar.getText() : ""));
        }

        // Conectar botón Agregar
        if (agregarPlatoButton != null) {
            agregarPlatoButton.addActionListener(e -> agregarPlato());
        }
        if (tablaCarta != null) {
            tablaCarta.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // doble clic en fila => editar
            tablaCarta.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        editarPlato();
                    }
                }
            });
        }
        if (editarPlatoButton != null) {
            editarPlatoButton.addActionListener(e -> editarPlato());
        }
        if (eliminarPlatoButton != null) {
            eliminarPlatoButton.addActionListener(e -> {
                try {
                    eliminarPlato();
                } catch (Throwable ex) {
                    // prevenir fallo si método no existe o lanza excepción
                    JOptionPane.showMessageDialog(panelCarta.this, "No se pudo eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        // Carga inicial (muestra toda la carta)
        buscarPlato("");

        actualizarCarta();

        if (volverButton != null) {
            volverButton.addActionListener(e -> {
                parentCardLayout.show(parentContentPanel, "MENU_PRINCIPAL");
            });
        }


    }

    private void actualizarCarta() {
        List<Plato> lista = controladorPicada.getCarta();
        String[] cols = {"ID", "Nombre", "Categoría", "Precio", "Disponibilidad"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        if (lista != null){
            for (Plato p : lista){
               model.addRow(new Object[]{ p.getIdPlato(), p.getNombre(), p.getTipo(), p.getPrecio(), p.getDisponibilidad() });
            }
        }
        tablaCarta.setModel(model);
/*List<Recepcion> lista = controladorPicada.getRecepciones();
        String[] cols = {"ID", "Fecha", "Estado", "Orden", "Transportista", "Observaciones"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        if (lista != null) {
            for (Recepcion r : lista) {
                String fecha = r.getFecha() != null ? sdf.format(r.getFecha()) : "";
                String ordenId = r.getOrdenCompra() != null ? r.getOrdenCompra().getIdOrden() : "";
                String trans = r.getTransportista() != null ? r.getTransportista().getNombre() : "";
                model.addRow(new Object[]{ r.getIdRecepcion(), fecha, r.getEstado(), ordenId, trans, r.getObservaciones() });
            }
        }

        tablaRecepciones.setModel(model);
    }*/
    }

    public void filtrarPorCategoria() {
        if (tablaCarta == null) return;

        Object sel = comboFiltrarCategoria != null ? comboFiltrarCategoria.getSelectedItem() : null;
        String categoria = (sel == null || "Todas".equals(sel.toString())) ? null : sel.toString();

        if (categoria == null) {
            // Mostrar toda la carta usando el método existente
            actualizarCarta();
            return;
        }

        String[] columnas = {"ID", "Nombre", "Categoría", "Precio", "Disponibilidad"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Plato> platos = controladorPicada.obtenerPlatosPorCategoria(categoria);
        if (platos != null) {
            for (Plato p : platos) {
                Object precio;
                try {
                    precio = p.getPrecio();
                } catch (Throwable t) {
                    precio = "";
                }
                Object[] fila = {
                        p.getIdPlato(),
                        p.getNombre(),
                        p.getTipo(),
                        precio,
                        p.getDisponibilidad()
                };
                modelo.addRow(fila);
            }
        }

        tablaCarta.setModel(modelo);

        if (tablaCarta.getColumnModel().getColumnCount() > 0) {
            tablaCarta.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
            tablaCarta.getColumnModel().getColumn(1).setPreferredWidth(200); // Nombre
            tablaCarta.getColumnModel().getColumn(2).setPreferredWidth(100); // Categoría
            tablaCarta.getColumnModel().getColumn(3).setPreferredWidth(80);  // Precio
            tablaCarta.getColumnModel().getColumn(4).setPreferredWidth(80);  // Disponibilidad
        }
    }

    private void buscarPlato(String texto) {
        if (tablaCarta == null) return;

        String textoFiltro = (texto == null) ? "" : texto.trim().toLowerCase();

        // Si no hay texto, respetar filtro de categoría o mostrar toda la carta
        Object sel = comboFiltrarCategoria != null ? comboFiltrarCategoria.getSelectedItem() : null;
        String categoria = (sel == null || "Todas".equalsIgnoreCase(sel.toString())) ? null : sel.toString();

        String[] columnas = {"ID", "Nombre", "Categoría", "Precio", "Disponibilidad"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Plato> platos = controladorPicada.getCarta();
        if (platos != null) {
            platos.stream()
                    .filter(p -> {
                        // Filtrado por categoría si corresponde
                        if (categoria != null && !categoria.equalsIgnoreCase(p.getTipo())) return false;
                        // Si texto vacío aceptar todo, si no, comparar prefijo ignorando mayúsculas
                        if (textoFiltro.isEmpty()) return true;
                        return p.getNombre() != null && p.getNombre().toLowerCase().startsWith(textoFiltro);
                    })
                    .forEach(p -> {
                        Object precio;
                        try {
                            precio = p.getPrecio();
                        } catch (Throwable t) {
                            precio = "";
                        }
                        Object[] fila = {
                                p.getIdPlato(),
                                p.getNombre(),
                                p.getTipo(),
                                precio,
                                p.getDisponibilidad()
                        };
                        modelo.addRow(fila);
                    });
        }

        tablaCarta.setModel(modelo);

        if (tablaCarta.getColumnModel().getColumnCount() > 0) {
            tablaCarta.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
            tablaCarta.getColumnModel().getColumn(1).setPreferredWidth(200); // Nombre
            tablaCarta.getColumnModel().getColumn(2).setPreferredWidth(100); // Categoría
            tablaCarta.getColumnModel().getColumn(3).setPreferredWidth(80);  // Precio
            tablaCarta.getColumnModel().getColumn(4).setPreferredWidth(80);  // Disponibilidad
        }
    }

    private void agregarPlato() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Agregar Plato", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        JLabel lId = new JLabel("ID:");
        JLabel lNombre = new JLabel("Nombre:");
        JLabel lPrecio = new JLabel("Precio:");
        JLabel lTipo = new JLabel("Categoría:");
        JLabel lDisp = new JLabel("Disponibilidad:");

        JTextField fId = new JTextField(15);
        JTextField fNombre = new JTextField(20);
        JTextField fPrecio = new JTextField(8);
        // Usar JComboBox editable para categorías
        List<String> categorias = controladorPicada.obtenerCategoriasUnicas();
        JComboBox<String> cbTipo = new JComboBox<>(categorias == null ? new String[0] : categorias.toArray(new String[0]));
        cbTipo.setEditable(true); // permite escribir una nueva categoría
        JTextField fDisp = new JTextField(6);

        c.gridx = 0;
        c.gridy = 0;
        panel.add(lId, c);
        c.gridx = 1;
        panel.add(fId, c);
        c.gridx = 0;
        c.gridy = 1;
        panel.add(lNombre, c);
        c.gridx = 1;
        panel.add(fNombre, c);
        c.gridx = 0;
        c.gridy = 2;
        panel.add(lPrecio, c);
        c.gridx = 1;
        panel.add(fPrecio, c);
        c.gridx = 0;
        c.gridy = 3;
        panel.add(lTipo, c);
        c.gridx = 1;
        panel.add(cbTipo, c);
        c.gridx = 0;
        c.gridy = 4;
        panel.add(lDisp, c);
        c.gridx = 1;
        panel.add(fDisp, c);

        JPanel botones = new JPanel();
        JButton ok = new JButton("Aceptar");
        JButton cancelar = new JButton("Cancelar");
        botones.add(ok);
        botones.add(cancelar);

        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        panel.add(botones, c);

        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        ok.addActionListener(ev -> {
            String id = fId.getText().trim();
            String nombre = fNombre.getText().trim();
            String precioS = fPrecio.getText().trim();
            // obtener la categoría, funciona si el combo es editable o si seleccionan un item
            Object selTipo = cbTipo.getSelectedItem();
            String tipo = selTipo != null ? selTipo.toString().trim() : "";
            String dispS = fDisp.getText().trim();

            if (id.isEmpty() || nombre.isEmpty() || precioS.isEmpty() || tipo.isEmpty() || dispS.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Complete todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int precio;
            int disp;
            try {
                precio = Integer.parseInt(precioS);
                disp = Integer.parseInt(dispS);
                if (precio < 0 || disp < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Precio y disponibilidad deben ser números enteros no negativos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                controladorPicada.agregarPlato(id, nombre, precio, tipo, disp);

                // Si la categoría ingresada no existía, actualizar el combo de filtrado principal
                if (comboFiltrarCategoria != null) {
                    comboFiltrarCategoria.removeAllItems();
                    comboFiltrarCategoria.addItem("Todas");
                    List<String> nuevasCats = controladorPicada.obtenerCategoriasUnicas();
                    if (nuevasCats != null) {
                        for (String cstr : nuevasCats) comboFiltrarCategoria.addItem(cstr);
                    }
                    comboFiltrarCategoria.setSelectedItem(tipo); // seleccionar la categoría nueva/seleccionada
                }

                // refrescar la vista manteniendo el filtro actual
                buscarPlato(textoBuscar != null ? textoBuscar.getText() : "");
                JOptionPane.showMessageDialog(dialog, "Plato agregado.", "OK", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "No se pudo agregar plato: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelar.addActionListener(ev -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void editarPlato() {
        if (tablaCarta == null) return;
        int fila = tablaCarta.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un plato para editar.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // convertir índice de vista a modelo (por si hay sorter)
        int modelRow = tablaCarta.convertRowIndexToModel(fila);
        Object idObj = tablaCarta.getModel().getValueAt(modelRow, 0);
        if (idObj == null) return;
        String id = idObj.toString();

        // guardar selección actual de categoría y texto de búsqueda
        final String prevCategoria = (comboFiltrarCategoria != null && comboFiltrarCategoria.getSelectedItem() != null)
                ? comboFiltrarCategoria.getSelectedItem().toString()
                : "Todas";
        final String prevTexto = (textoBuscar != null) ? textoBuscar.getText() : "";

        // Buscar plato en el controlador
        Plato platoSeleccionado = controladorPicada.getCarta().stream()
                .filter(p -> id.equals(p.getIdPlato()))
                .findFirst()
                .orElse(null);

        if (platoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Plato no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Crear diálogo con campos prefijados
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Editar Plato", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        JLabel lId = new JLabel("ID:");
        JLabel lNombre = new JLabel("Nombre:");
        JLabel lPrecio = new JLabel("Precio:");
        JLabel lTipo = new JLabel("Categoría:");
        JLabel lDisp = new JLabel("Disponibilidad:");

        JTextField fId = new JTextField(15);
        fId.setText(platoSeleccionado.getIdPlato());
        fId.setEditable(false);

        JTextField fNombre = new JTextField(20);
        fNombre.setText(platoSeleccionado.getNombre());

        JTextField fPrecio = new JTextField(8);
        fPrecio.setText(String.valueOf(platoSeleccionado.getPrecio()));

        List<String> categorias = controladorPicada.obtenerCategoriasUnicas();
        JComboBox<String> cbTipo = new JComboBox<>(categorias == null ? new String[0] : categorias.toArray(new String[0]));
        cbTipo.setEditable(true);
        cbTipo.setSelectedItem(platoSeleccionado.getTipo());

        JTextField fDisp = new JTextField(6);
        fDisp.setText(String.valueOf(platoSeleccionado.getDisponibilidad()));

        c.gridx = 0; c.gridy = 0; panel.add(lId, c);
        c.gridx = 1; panel.add(fId, c);
        c.gridx = 0; c.gridy = 1; panel.add(lNombre, c);
        c.gridx = 1; panel.add(fNombre, c);
        c.gridx = 0; c.gridy = 2; panel.add(lPrecio, c);
        c.gridx = 1; panel.add(fPrecio, c);
        c.gridx = 0; c.gridy = 3; panel.add(lTipo, c);
        c.gridx = 1; panel.add(cbTipo, c);
        c.gridx = 0; c.gridy = 4; panel.add(lDisp, c);
        c.gridx = 1; panel.add(fDisp, c);

        JPanel botones = new JPanel();
        JButton ok = new JButton("Aceptar");
        JButton cancelar = new JButton("Cancelar");
        botones.add(ok);
        botones.add(cancelar);

        c.gridx = 0; c.gridy = 5; c.gridwidth = 2; panel.add(botones, c);

        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        ok.addActionListener(ev -> {
            String nombre = fNombre.getText().trim();
            String precioS = fPrecio.getText().trim();
            Object selTipo = cbTipo.getSelectedItem();
            String tipo = selTipo != null ? selTipo.toString().trim() : "";
            String dispS = fDisp.getText().trim();

            if (nombre.isEmpty() || precioS.isEmpty() || tipo.isEmpty() || dispS.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Complete todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int precio;
            int disp;
            try {
                precio = Integer.parseInt(precioS);
                disp = Integer.parseInt(dispS);
                if (precio < 0 || disp < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Precio y disponibilidad deben ser números enteros no negativos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                controladorPicada.actualizarPlato(id, nombre, precio, tipo, disp);

                // Recargar categorías y RESTAURAR la selección previa si aún existe
                if (comboFiltrarCategoria != null) {
                    comboFiltrarCategoria.removeAllItems();
                    comboFiltrarCategoria.addItem("Todas");
                    List<String> nuevasCats = controladorPicada.obtenerCategoriasUnicas();
                    if (nuevasCats != null) {
                        for (String cstr : nuevasCats) comboFiltrarCategoria.addItem(cstr);
                    }
                    // intentar restaurar la selección anterior
                    boolean encontrado = false;
                    for (int i = 0; i < comboFiltrarCategoria.getItemCount(); i++) {
                        Object it = comboFiltrarCategoria.getItemAt(i);
                        if (it != null && it.toString().equalsIgnoreCase(prevCategoria)) {
                            comboFiltrarCategoria.setSelectedItem(it);
                            encontrado = true;
                            break;
                        }
                    }
                    if (!encontrado) {
                        // si la anterior no existe, mantener "Todas"
                        comboFiltrarCategoria.setSelectedIndex(0);
                    }
                }

                // Restaurar búsqueda y refrescar la tabla con el mismo filtro
                buscarPlato(prevTexto);

                JOptionPane.showMessageDialog(dialog, "Plato actualizado.", "OK", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "No se pudo actualizar plato: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelar.addActionListener(ev -> dialog.dispose());

        dialog.setVisible(true);
    }
    private void eliminarPlato() {
        if (tablaCarta == null) return;
        int fila = tablaCarta.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un plato para eliminar.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Obtener ID desde la tabla (asume que la columna 0 es ID en el modelo actual)
        Object idObj = tablaCarta.getModel().getValueAt(fila, 0);
        if (idObj == null) return;
        String id = idObj.toString();

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar el plato seleccionado?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            controladorPicada.eliminarPlato(id);

            // Refrescar tabla manteniendo filtro de búsqueda
            buscarPlato(textoBuscar != null ? textoBuscar.getText() : "");
            JOptionPane.showMessageDialog(this, "Plato eliminado.", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar plato: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


}
