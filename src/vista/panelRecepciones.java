package vista;

import controlador.ControladorPicada;
import modelo.DetalleOrdenCompra;
import modelo.OrdenCompra;
import modelo.Recepcion;
import modelo.Transportista;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

public class panelRecepciones extends JPanel{
    private ControladorPicada controladorPicada;
    private VistaPrincipal mainFrame;

    private CardLayout parentCardLayout;
    private JPanel parentContentPanel;
    private JPanel MainPanel;
    private JTable tablaRecepciones;
    private JTable tablaDetallesOrden;
    private JButton nuevaRecepciónButton;
    private JButton procesarRecepciónButton;
    private JButton volverButton;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public panelRecepciones(VistaPrincipal mainFrame, CardLayout cl, JPanel contentPanel) {
        this.mainFrame = mainFrame;
        this.parentCardLayout = cl;
        this.parentContentPanel = contentPanel;

        // Usar singleton compartido
        controladorPicada = ControladorPicada.getInstance();

        setLayout(new BorderLayout());
        add(MainPanel, BorderLayout.CENTER);

        // Asegurar que la tabla exista (formularios GUI suelen inicializarla)
        if (tablaRecepciones != null) {
            tablaRecepciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        } else {
            tablaRecepciones = new JTable();
            tablaRecepciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            add(new JScrollPane(tablaRecepciones), BorderLayout.CENTER);
        }

        // Botón Nueva Recepción (registrar solo una vez)
        if (nuevaRecepciónButton != null) {
            nuevaRecepciónButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    nuevaRecepcion();
                }
            });
        }

        // Asegurar tabla de detalles (lado derecho) exista
        if (tablaDetallesOrden == null) {
            tablaDetallesOrden = new JTable();
            DefaultTableModel modelDet = new DefaultTableModel(new String[]{"Insumo ID", "Nombre", "Cant Pedida", "Recibido"}, 0) {
                @Override public boolean isCellEditable(int row, int column) {
                    // por defecto solo la columna Recibido es editable; habilitación real depende del estado y se controla al cargar detalles
                    return column == 3;
                }
            };
            tablaDetallesOrden.setModel(modelDet);
            // Añadir a la derecha si el diseño lo permite
            add(new JScrollPane(tablaDetallesOrden), BorderLayout.EAST);
        }

        // Listener: cuando se selecciona una recepción, cargar detalles en la tabla de la derecha
        tablaRecepciones.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    cargarDetallesSeleccion();
                }
            }
        });

        // Botón Procesar Recepción
        if (procesarRecepciónButton != null) {
            procesarRecepciónButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    procesarRecepcion();
                }
            });
        }

        // Botón Volver: regresa al primer panel del CardLayout (ajustar si usa otro nombre)
        if (volverButton != null) {
            volverButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (parentCardLayout != null && parentContentPanel != null) {
                        parentCardLayout.first(parentContentPanel);
                    }
                }
            });
        }

        // Doble clic en la fila para ver detalles
        tablaRecepciones.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    verDetalles();
                }
            }
        });

        // Rellenar tabla al crear el panel
        actualizarTablaRecepciones();
    }

    public void actualizarTablaRecepciones() {
        List<Recepcion> lista = controladorPicada.getRecepciones();
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
    }

    public void nuevaRecepcion() {
        // Obtener IDs ya presentes en la tabla para excluirlos
        java.util.Set<String> idsEnTabla = new java.util.HashSet<>();
        if (tablaRecepciones != null && tablaRecepciones.getModel() != null) {
            DefaultTableModel currentModel = (DefaultTableModel) tablaRecepciones.getModel();
            for (int i = 0; i < currentModel.getRowCount(); i++) {
                Object val = currentModel.getValueAt(i, 0);
                if (val != null) idsEnTabla.add(val.toString());
            }
        }

        List<Recepcion> todas = controladorPicada.getRecepciones();
        List<Recepcion> pendientes = new java.util.ArrayList<>();
        if (todas != null) {
            for (Recepcion r : todas) {
                if (r == null) continue;
                if (!"PENDIENTE".equalsIgnoreCase(r.getEstado())) continue;
                // Excluir si ya está en la tabla
                if (idsEnTabla.contains(r.getIdRecepcion())) continue;
                pendientes.add(r);
            }
        }

        if (pendientes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay recepciones pendientes nuevas para mostrar.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<String> cb = new JComboBox<>();
        for (Recepcion r : pendientes) {
            String fecha = r.getFecha() != null ? sdf.format(r.getFecha()) : "";
            String ordenId = r.getOrdenCompra() != null ? r.getOrdenCompra().getIdOrden() : "";
            cb.addItem(r.getIdRecepcion() + "  -  Orden: " + ordenId + "  -  " + fecha);
        }

        int opt = JOptionPane.showConfirmDialog(this, new Object[] { "Recepción pendiente:", cb }, "Seleccionar Recepción", JOptionPane.OK_CANCEL_OPTION);
        if (opt != JOptionPane.OK_OPTION) return;

        int selIndex = cb.getSelectedIndex();
        if (selIndex < 0 || selIndex >= pendientes.size()) return;

        Recepcion seleccionada = pendientes.get(selIndex);

        // Actualizar tabla y seleccionar la recepción elegida
        actualizarTablaRecepciones();

        DefaultTableModel model = (DefaultTableModel) tablaRecepciones.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object val = model.getValueAt(i, 0);
            if (val != null && val.toString().equals(seleccionada.getIdRecepcion())) {
                tablaRecepciones.setRowSelectionInterval(i, i);
                Rectangle rect = tablaRecepciones.getCellRect(i, 0, true);
                tablaRecepciones.scrollRectToVisible(rect);
                cargarDetallesSeleccion();
                break;
            }
        }
    }

    public void procesarRecepcion() {
        int row = tablaRecepciones.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una recepción primero.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Recepcion> lista = controladorPicada.getRecepciones();
        Recepcion r = lista.get(row);

        if (!"PENDIENTE".equals(r.getEstado()) && !"INCOMPLETA".equals(r.getEstado())) {
            JOptionPane.showMessageDialog(this, "Solo se pueden procesar recepciones PENDIENTE o INCOMPLETA.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- 1. Recoger cantidades de la tabla (TU CÓDIGO ACTUAL, ESTÁ BIEN) ---
        DefaultTableModel model = (DefaultTableModel) tablaDetallesOrden.getModel();
        OrdenCompra oc = r.getOrdenCompra();
        if (oc != null && oc.getDetalles() != null) {
            for (int i = 0; i < model.getRowCount(); i++) {
                String idInsumo = model.getValueAt(i, 0) != null ? model.getValueAt(i, 0).toString() : "";
                Object recibidoObj = model.getValueAt(i, 3);
                int recibido = 0;
                try {
                    if (recibidoObj != null && !recibidoObj.toString().trim().isEmpty()) {
                        recibido = Integer.parseInt(recibidoObj.toString().trim());
                        if (recibido < 0) recibido = 0;
                    }
                } catch (NumberFormatException ex) {
                    recibido = 0;
                }

                // Buscar detalle correspondiente y actualizar la cantidad a la recibida
                for (DetalleOrdenCompra d : oc.getDetalles()) {
                    if (d.getInsumo() != null && idInsumo.equals(d.getInsumo().getIdInsumo())) {
                        try {
                            d.setCantidad(recibido);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Error al actualizar cantidad para insumo " + idInsumo + ": " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }
        // --- FIN DE RECOLECCIÓN DE CANTIDADES ---

        // --- 2. SOLICITAR ESTADO FINAL ---
        JComboBox<String> cbEstado = new JComboBox<>(new String[]{"COMPLETA", "INCOMPLETA"});
        int opt = JOptionPane.showConfirmDialog(this, new Object[]{"Seleccione el estado final:", cbEstado}, "Finalizar Recepción", JOptionPane.OK_CANCEL_OPTION);

        if (opt == JOptionPane.OK_OPTION) {
            String estadoFinal = (String) cbEstado.getSelectedItem();

            try {
                // Usar el método del controlador para aplicar stock/precio y persistir
                controladorPicada.procesarRecepcion(r.getIdRecepcion(), estadoFinal);

                JOptionPane.showMessageDialog(this, "Recepción procesada y marcada como: " + estadoFinal, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                actualizarTablaRecepciones();
                cargarDetallesSeleccion();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al procesar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                // no es necesario revertir manualmente aquí porque el controlador no habrá persistido si falla
            }
        }
        // Si el usuario presiona CANCEL, no se hace nada y el estado sigue siendo PENDIENTE/INCOMPLETA.
    }

    public void verDetalles() {
        int row = tablaRecepciones.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una recepción para ver detalles.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Recepcion> lista = controladorPicada.getRecepciones();
        if (lista == null || row >= lista.size()) {
            JOptionPane.showMessageDialog(this, "Recepción inválida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Recepcion r = lista.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(r.getIdRecepcion()).append("\n");
        sb.append("Fecha: ").append(r.getFecha() != null ? sdf.format(r.getFecha()) : "").append("\n");
        sb.append("Estado: ").append(r.getEstado()).append("\n");
        sb.append("Transportista: ").append(r.getTransportista() != null ? r.getTransportista().getNombre() : "").append("\n");
        sb.append("Observaciones: ").append(r.getObservaciones()).append("\n\n");

        OrdenCompra oc = r.getOrdenCompra();
        if (oc != null) {
            sb.append("Orden: ").append(oc.getIdOrden()).append(" - Estado: ").append(oc.getEstado()).append("\n");
            sb.append("Detalles de la orden:\n");
            if (oc.getDetalles() != null) {
                for (DetalleOrdenCompra d : oc.getDetalles()) {
                    String ins = d.getInsumo() != null ? d.getInsumo().getNombre() : (d.getInsumo() != null ? d.getInsumo().getIdInsumo() : "");
                    sb.append(" - ").append(ins).append(" | Cant: ").append(d.getCantidad()).append(" | Precio: ").append(d.getPrecioUnitario()).append("\n");
                }
            }
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Detalles de Recepción", JOptionPane.INFORMATION_MESSAGE);

        if (oc.getDetalles() != null) {
            for (DetalleOrdenCompra d : oc.getDetalles()) {
                String ins = d.getInsumo() != null ? d.getInsumo().getNombre() : (d.getInsumo() != null ? d.getInsumo().getIdInsumo() : "");
                sb.append(" - ").append(ins).append(" | Cant: ").append(d.getCantidad()).append(" | Precio: ").append(d.getPrecioUnitario()).append("\n");
            }
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Detalles de Recepción", JOptionPane.INFORMATION_MESSAGE);
    }

    // Cargar detalles de la recepción seleccionada en la tabla de detalles (con columna editable "Recibido")
    private void cargarDetallesSeleccion() {
        int row = tablaRecepciones.getSelectedRow();
        if (row < 0) {
            // limpiar detalles
            DefaultTableModel empty = new DefaultTableModel(new String[]{"Insumo ID", "Nombre", "Cant Pedida", "Recibido"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return c == 3; }
            };
            tablaDetallesOrden.setModel(empty);
            return;
        }

        List<Recepcion> lista = controladorPicada.getRecepciones();
        if (lista == null || row >= lista.size()) {
            JOptionPane.showMessageDialog(this, "Recepción inválida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Recepcion r = lista.get(row);
        OrdenCompra oc = r.getOrdenCompra();

        String[] cols = {"Insumo ID", "Nombre", "Cant Pedida", "Recibido"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                String estado = lista.get(row).getEstado();
                boolean editable = c == 3 && "PENDIENTE".equalsIgnoreCase(estado);
                return editable;
            }
        };

        if (oc != null && oc.getDetalles() != null) {
            for (DetalleOrdenCompra d : oc.getDetalles()) {
                String idIn = d.getInsumo() != null ? d.getInsumo().getIdInsumo() : "";
                String nombre = d.getInsumo() != null ? d.getInsumo().getNombre() : "";
                int pedida = d.getCantidad();
                int recibidoDefault = pedida; // valor por defecto sugerido
                model.addRow(new Object[]{ idIn, nombre, pedida, recibidoDefault });
            }
        }

        tablaDetallesOrden.setModel(model);
    }
}