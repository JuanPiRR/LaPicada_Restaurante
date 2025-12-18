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
import javax.swing.table.TableCellEditor;
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
        if (tablaRecepciones != null && tablaRecepciones.getClientProperty("doubleClickListenerAdded") == null) {
            tablaRecepciones.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    // Solo doble clic izquierdo
                    if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() != 2) return;

                    int viewRow = tablaRecepciones.rowAtPoint(e.getPoint());
                    if (viewRow == -1) return;
                    int modelRow = tablaRecepciones.convertRowIndexToModel(viewRow);

                    Object idObj = tablaRecepciones.getModel().getValueAt(modelRow, 0); // asume primera columna = idRecepcion
                    if (idObj == null) return;
                    String idRecepcion = idObj.toString();

                    modelo.Recepcion recepcion = controladorPicada.getRecepciones().stream()
                            .filter(r -> idRecepcion.equals(r.getIdRecepcion()))
                            .findFirst().orElse(null);
                    if (recepcion == null) return;

                    modelo.Transportista t = recepcion.getTransportista();

                    StringBuilder sb = new StringBuilder();
                    sb.append("ID Recepción: ").append(recepcion.getIdRecepcion()).append("\n");
                    sb.append("Estado: ").append(recepcion.getEstado()).append("\n");
                    sb.append("Observaciones: ").append(recepcion.getObservaciones() != null ? recepcion.getObservaciones() : "").append("\n\n");

                    if (t != null) {
                        sb.append("----- Transportista -----\n");
                        sb.append("ID: ").append(t.getIdTransportista()).append("\n");
                        sb.append("Nombre: ").append(t.getNombre()).append("\n");
                        sb.append("Empresa: ").append(t.getEmpresa()).append("\n");
                        sb.append("Patente: ").append(t.getPatente()).append("\n");
                        sb.append("Teléfono: ").append(t.getTelefono()).append("\n");
                    } else {
                        sb.append("Transportista: No asignado\n");
                    }

                    javax.swing.JTextArea ta = new javax.swing.JTextArea(sb.toString());
                    ta.setEditable(false);
                    ta.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
                    javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(ta);
                    scroll.setPreferredSize(new java.awt.Dimension(420, 320));
                    javax.swing.JOptionPane.showMessageDialog(panelRecepciones.this, scroll, "Detalle Recepción", javax.swing.JOptionPane.INFORMATION_MESSAGE);

                    e.consume(); // evita re-procesos extra
                }
            });
            tablaRecepciones.putClientProperty("doubleClickListenerAdded", Boolean.TRUE);
        }

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

        if (!"PENDIENTE".equalsIgnoreCase(r.getEstado()) && !"INCOMPLETA".equalsIgnoreCase(r.getEstado())) {
            JOptionPane.showMessageDialog(this, "Solo se pueden procesar recepciones PENDIENTE o INCOMPLETA.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        OrdenCompra oc = r.getOrdenCompra();
        if (oc == null || oc.getDetalles() == null) {
            JOptionPane.showMessageDialog(this, "Orden asociada no encontrada o sin detalles.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Si hay una celda en edición, forzar que termine y se copie el valor al modelo
        if (tablaDetallesOrden.isEditing()) {
            TableCellEditor editor = tablaDetallesOrden.getCellEditor();
            if (editor != null) editor.stopCellEditing();
        }

        DefaultTableModel model = (DefaultTableModel) tablaDetallesOrden.getModel();

        // Detectar columnas por nombre
        int colId = -1, colSolicitado = -1, colEntregado = -1;
        for (int c = 0; c < model.getColumnCount(); c++) {
            String name = model.getColumnName(c).toLowerCase();
            if (colId == -1 && (name.contains("id") || name.contains("insumo"))) colId = c;
            if (colSolicitado == -1 && (name.contains("solicit") || name.contains("pedido") || (name.contains("cantidad") && !name.contains("entreg")))) colSolicitado = c;
            if (colEntregado == -1 && name.contains("entreg")) colEntregado = c;
        }
        if (colId == -1) colId = 0;
        if (colSolicitado == -1) colSolicitado = Math.min(1, Math.max(0, model.getColumnCount() - 1));
        if (colEntregado == -1) colEntregado = Math.min(2, Math.max(0, model.getColumnCount() - 1));

        boolean completa = true;

        for (int i = 0; i < model.getRowCount(); i++) {
            Object idObj = model.getValueAt(i, colId);
            String idInsumo = idObj != null ? idObj.toString().trim() : "";

            int entregado = 0;
            try {
                Object valEntregado = model.getValueAt(i, colEntregado);
                entregado = valEntregado != null ? Integer.parseInt(valEntregado.toString().trim()) : 0;
            } catch (NumberFormatException ex) {
                entregado = 0;
            }

            int solicitado = 0;
            try {
                Object valSolicitado = model.getValueAt(i, colSolicitado);
                solicitado = valSolicitado != null ? Integer.parseInt(valSolicitado.toString().trim()) : 0;
            } catch (NumberFormatException ex) {
                solicitado = 0;
            }

            DetalleOrdenCompra detalleEncontrado = null;
            for (DetalleOrdenCompra d : oc.getDetalles()) {
                if (d.getInsumo() != null && idInsumo.equals(d.getInsumo().getIdInsumo())) {
                    detalleEncontrado = d;
                    break;
                }
            }

            if (detalleEncontrado != null) {
                // CAMBIO CLAVE: usar setCantidadRecibida() en lugar de setCantidad()
                detalleEncontrado.setCantidadRecibida(entregado);
            }

            if (entregado < solicitado) {
                completa = false;
            }
        }

        String estadoFinal = completa ? "COMPLETA" : "INCOMPLETA";

        try {
            controladorPicada.procesarRecepcion(r.getIdRecepcion(), estadoFinal);
            actualizarTablaRecepciones();
            cargarDetallesSeleccion();
            JOptionPane.showMessageDialog(this, "Recepción procesada como " + estadoFinal + ".", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al procesar recepción: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
            tablaDetallesOrden.setModel(new DefaultTableModel(new String[]{"ID Insumo", "Nombre", "Solicitado", "Entregado"}, 0));
            return;
        }

        List<Recepcion> lista = controladorPicada.getRecepciones();
        Recepcion r = lista.get(row);
        OrdenCompra oc = r.getOrdenCompra();

        if (oc == null || oc.getDetalles() == null || oc.getDetalles().isEmpty()) {
            tablaDetallesOrden.setModel(new DefaultTableModel(new String[]{"ID Insumo", "Nombre", "Solicitado", "Entregado"}, 0));
            return;
        }

        String[] columnas = {"ID Insumo", "Nombre", "Solicitado", "Entregado"};
        Object[][] datos = new Object[oc.getDetalles().size()][4];

        for (int i = 0; i < oc.getDetalles().size(); i++) {
            DetalleOrdenCompra d = oc.getDetalles().get(i);
            datos[i][0] = d.getInsumo() != null ? d.getInsumo().getIdInsumo() : "";
            datos[i][1] = d.getInsumo() != null ? d.getInsumo().getNombre() : "";
            datos[i][2] = d.getCantidad(); // solicitado (no editable)

            // CAMBIO CLAVE: usar getCantidadRecibida() si ya fue procesada
            int cantidadMostrar = d.getCantidad(); // por defecto mostrar lo solicitado
            if (d.getCantidadRecibida() > 0) {
                // Si ya se procesó y hay cantidad recibida registrada, mostrar esa
                cantidadMostrar = d.getCantidadRecibida();
            } else if ("PENDIENTE".equalsIgnoreCase(r.getEstado())) {
                // Si está PENDIENTE, inicializar con lo solicitado para permitir edición
                cantidadMostrar = d.getCantidad();
            }
            datos[i][3] = cantidadMostrar;
        }

        DefaultTableModel modelo = new DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo columna "Entregado" (3) es editable, y solo si está PENDIENTE o INCOMPLETA
                return column == 3 && (r.getEstado().equalsIgnoreCase("PENDIENTE") || r.getEstado().equalsIgnoreCase("INCOMPLETA"));
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex >= 2 ? Integer.class : String.class;
            }
        };

        tablaDetallesOrden.setModel(modelo);
    }
}