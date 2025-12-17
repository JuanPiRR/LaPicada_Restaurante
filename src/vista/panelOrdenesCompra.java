package vista;

import controlador.ControladorPicada;
import modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class panelOrdenesCompra extends JPanel {
    private ControladorPicada controladorPicada;
    private VistaPrincipal mainFrame;



    private CardLayout parentCardLayout;
    private JPanel parentContentPanel;
    private JPanel MainPanel;

    // Componentes UI (si el diseñador ya los creó, mantener nombres iguales)
    private JTable tablaOrdenes;
    private JTable tablaDetalles;
    private JButton btnNuevaOrden;
    private JButton btnAgregarInsumo;
    private JButton btnEnviarOrden;
    private JButton btnRegistrarRecepcion;
    private JButton btnRegistrarPago;
    private JButton volverButton;

    // Nueva bandera para suprimir manejo de selección durante actualizaciones programáticas
    private boolean suppressSelectionEvents = false;

    public panelOrdenesCompra(VistaPrincipal mainFrame, CardLayout cl, JPanel contentPanel) {
        this.mainFrame = mainFrame;
        this.parentCardLayout = cl;
        this.parentContentPanel = contentPanel;

        controladorPicada = ControladorPicada.getInstance();

        setLayout(new BorderLayout());
        add(MainPanel, BorderLayout.CENTER);
        // Inicializar listeners y estado de UI
        btnNuevaOrden.addActionListener(e -> nuevaOrdenCompra());
        btnAgregarInsumo.addActionListener(e -> agregarInsumoOrdenCompra());
        btnEnviarOrden.addActionListener(e -> enviarOrdenCompra());
        btnRegistrarRecepcion.addActionListener(e -> registrarRecepcion());
        btnRegistrarPago.addActionListener(e -> registrarPago());
        actualizarTabla();

        // Listener protegido por la bandera y evitando eventos intermedios
        tablaOrdenes.getSelectionModel().addListSelectionListener(e -> {
            if (suppressSelectionEvents) return;
            if (e.getValueIsAdjusting()) return;
            cargarDetallesOrdenCompra();
        });

        if (volverButton != null) {
            volverButton.addActionListener(e -> {
                parentCardLayout.show(parentContentPanel, "MENU_PRINCIPAL");
            });
        }
    }


    // Inicializa modelos básicos de tablas si son null y listeners de botones/selección

    // Helper: obtener todas las órdenes combinando estados expuestos por el controlador
    private List<OrdenCompra> getAllOrdenes() {
        List<OrdenCompra> res = new ArrayList<>();
        String[] estados = {"PENDIENTE", "ENVIADA", "EN_RECEPCION", "RECIBIDA", "PAGADA", "CANCELADA"};
        for (String s : estados) {
            List<OrdenCompra> list = controladorPicada.getOrdenesPorEstado(s);
            if (list != null) res.addAll(list);
        }
        return res;
    }


    private OrdenCompra getOrdenById(String idOrden) {
        for (OrdenCompra o : getAllOrdenes()) {
            if (o.getIdOrden().equals(idOrden)) return o;
        }
        return null;
    }

    private void actualizarTabla() {
        List<OrdenCompra> lista = getAllOrdenes();
        String[] cols = {"ID", "Fecha", "Proveedor", "Total", "Estado", "Detalles"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        // Guardar ID seleccionado actual para restituir la selección después de reconstruir el modelo
        String selectedId = null;
        int selRow = tablaOrdenes != null ? tablaOrdenes.getSelectedRow() : -1;
        if (selRow >= 0 && tablaOrdenes.getModel() != null) {
            Object val = tablaOrdenes.getModel().getValueAt(selRow, 0);
            if (val != null) selectedId = String.valueOf(val);
        }

        if (lista != null && !lista.isEmpty()) {
            for (OrdenCompra o : lista) {
                String fecha = o.getFecha() != null ? o.getFecha().toString() : "";
                String proveedor = o.getProveedor() != null ? o.getProveedor().getNombre() : "";
                int detalles = o.getDetalles() != null ? o.getDetalles().size() : 0;
                model.addRow(new Object[]{ o.getIdOrden(), fecha, proveedor, o.getTotal(), o.getEstado(), detalles });
            }
        }

        // Suprimir eventos de selección mientras se cambia el modelo y se restaura la selección
        suppressSelectionEvents = true;
        tablaOrdenes.setModel(model);

        // Restaurar selección si es posible
        if (selectedId != null) {
            for (int i = 0; i < model.getRowCount(); i++) {
                Object val = model.getValueAt(i, 0);
                if (val != null && selectedId.equals(String.valueOf(val))) {
                    tablaOrdenes.setRowSelectionInterval(i, i);
                    Rectangle rect = tablaOrdenes.getCellRect(i, 0, true);
                    tablaOrdenes.scrollRectToVisible(rect);
                    break;
                }
            }
        }

        suppressSelectionEvents = false;
    }

    private void cargarDetallesOrdenCompra() {
        int row = tablaOrdenes.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden primero.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idOrden = (String) tablaOrdenes.getModel().getValueAt(row, 0);
        OrdenCompra oc = getOrdenById(idOrden);
        if (oc == null) {
            JOptionPane.showMessageDialog(this, "Orden no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] cols = {"Insumo ID", "Nombre", "Cantidad", "Precio Unit.", "Subtotal"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        if (oc.getDetalles() != null) {
            for (DetalleOrdenCompra d : oc.getDetalles()) {
                String idInsumo = d.getInsumo() != null ? d.getInsumo().getIdInsumo() : "";
                String nombre = d.getInsumo() != null ? d.getInsumo().getNombre() : "";
                model.addRow(new Object[]{ idInsumo, nombre, d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal() });
            }
        }

        tablaDetalles.setModel(model);

        // Habilitar/inhabilitar botones según estado
        String estado = oc.getEstado();
        boolean esPendiente = "PENDIENTE".equals(estado);
        boolean esEnviada = "ENVIADA".equals(estado);
        boolean esRecibida = "RECIBIDA".equals(estado);
        boolean esPagada = "PAGADA".equals(estado);

        if (btnAgregarInsumo != null) btnAgregarInsumo.setEnabled(esPendiente);
        if (btnEnviarOrden != null) btnEnviarOrden.setEnabled(esPendiente && (oc.getDetalles() != null && !oc.getDetalles().isEmpty()));
        // El botón de registrar recepción sólo para ENVIADA
        if (btnRegistrarRecepcion != null) btnRegistrarRecepcion.setEnabled(esEnviada);
        // El botón de registrar pago sólo si está RECIBIDA (si ya está PAGADA debe quedar inhabilitado)
        if (btnRegistrarPago != null) btnRegistrarPago.setEnabled(esRecibida && !esPagada);
    }
    private void nuevaOrdenCompra() {
        List<Proveedor> proveedores = controladorPicada.getProveedores();
        if (proveedores == null || proveedores.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay proveedores disponibles.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<String> cbProv = new JComboBox<>();
        for (Proveedor p : proveedores) cbProv.addItem(p.getIdProveedor());

        int opt = JOptionPane.showConfirmDialog(this, new Object[]{"Proveedor:", cbProv}, "Nueva Orden de Compra", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String idProv = (String) cbProv.getSelectedItem();
            try {
                OrdenCompra nueva = controladorPicada.crearOrdenCompra(idProv);
                JOptionPane.showMessageDialog(this, "Orden creada: " + nueva.getIdOrden(), "Éxito", JOptionPane.INFORMATION_MESSAGE);

                // Actualizar tabla y seleccionar la fila recién creada para evitar el mensaje de "Seleccione una orden primero"
                actualizarTabla();

                if (tablaOrdenes != null && tablaOrdenes.getModel() != null) {
                    DefaultTableModel model = (DefaultTableModel) tablaOrdenes.getModel();
                    int foundRow = -1;
                    for (int i = 0; i < model.getRowCount(); i++) {
                        Object val = model.getValueAt(i, 0);
                        if (val != null && nueva.getIdOrden().equals(String.valueOf(val))) {
                            foundRow = i;
                            break;
                        }
                    }
                    if (foundRow >= 0) {
                        tablaOrdenes.setRowSelectionInterval(foundRow, foundRow);
                        // Asegura que la fila sea visible
                        Rectangle rect = tablaOrdenes.getCellRect(foundRow, 0, true);
                        tablaOrdenes.scrollRectToVisible(rect);
                        cargarDetallesOrdenCompra();
                    } else {
                        // Si por alguna razón no se encontró, no llamar a cargarDetallesOrdenCompra()
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error creando orden: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void agregarInsumoOrdenCompra() {
        int row = tablaOrdenes.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String idOrden = (String) tablaOrdenes.getModel().getValueAt(row, 0);
        OrdenCompra oc = getOrdenById(idOrden);
        if (oc == null) {
            JOptionPane.showMessageDialog(this, "Orden no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!"PENDIENTE".equals(oc.getEstado())) {
            JOptionPane.showMessageDialog(this, "Sólo se pueden agregar insumos a órdenes PENDIENTE.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Insumo> insumos = controladorPicada.getInsumos();
        if (insumos == null || insumos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay insumos disponibles.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<String> cbInsumo = new JComboBox<>();
        for (Insumo i : insumos) cbInsumo.addItem(i.getIdInsumo());

        JTextField tfCantidad = new JTextField();
        JTextField tfPrecio = new JTextField();

        Object[] message = {"Insumo (ID):", cbInsumo, "Cantidad:", tfCantidad, "Precio unitario:", tfPrecio};
        int opt = JOptionPane.showConfirmDialog(this, message, "Agregar Insumo a Orden", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String idInsumo = (String) cbInsumo.getSelectedItem();
            try {
                int cantidad = Integer.parseInt(tfCantidad.getText().trim());
                double precio = Double.parseDouble(tfPrecio.getText().trim());
                if (cantidad <= 0 || precio < 0) throw new NumberFormatException("Valores inválidos");
                controladorPicada.agregarInsumosAOrden(idOrden, idInsumo, cantidad, precio);
                JOptionPane.showMessageDialog(this, "Insumo agregado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                actualizarTabla();
                // No es necesario llamar a cargarDetallesOrdenCompra() aquí porque la selección actual se restaura en actualizarTabla()
                cargarDetallesOrdenCompra();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Cantidad o precio inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al agregar insumo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void enviarOrdenCompra() {
        int row = tablaOrdenes.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String idOrden = (String) tablaOrdenes.getModel().getValueAt(row, 0);
        OrdenCompra oc = getOrdenById(idOrden);
        if (oc == null) {
            JOptionPane.showMessageDialog(this, "Orden no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            controladorPicada.enviarOrdenCompra(idOrden);
            JOptionPane.showMessageDialog(this, "Orden enviada.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            actualizarTabla();
            cargarDetallesOrdenCompra();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al enviar orden: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }




    private void registrarRecepcion() {
        int row = tablaOrdenes.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden ENVIADA.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String idOrden = (String) tablaOrdenes.getModel().getValueAt(row, 0);
        OrdenCompra oc = getOrdenById(idOrden);
        if (oc == null) {
            JOptionPane.showMessageDialog(this, "Orden no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String estado = oc.getEstado();
        if (!"ENVIADA".equals(estado)) {
            if ("EN_RECEPCION".equals(estado)) {
                JOptionPane.showMessageDialog(this, "Esta orden ya tiene una recepción registrada y pendiente de procesamiento.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Selección inválida. La orden debe estar ENVIADA.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Transportista> tlist = controladorPicada.getTransportistas();

        // Combo con "ID - Nombre"
        JComboBox<String> cbTrans = new JComboBox<>();
        cbTrans.addItem("N/A"); // Opción para no seleccionar transportista
        if (tlist != null) {
            for (Transportista t : tlist) {
                String label = (t.getIdTransportista() != null ? t.getIdTransportista() : "") + " - " + (t.getNombre() != null ? t.getNombre() : "");
                cbTrans.addItem(label);
            }
        }

        // Checkbox y campos para nuevo transportista (ahora incluyen empresa, patente y teléfono obligatorios)
        JCheckBox chkNuevo = new JCheckBox("Nuevo transportista");
        JTextField tfNewId = new JTextField();
        JTextField tfNewNombre = new JTextField();
        JTextField tfEmpresa = new JTextField();
        JTextField tfPatente = new JTextField();
        JTextField tfTelefono = new JTextField();

        // Inicialmente deshabilitados (sólo habilitar si chkNuevo)
        tfNewId.setEnabled(false);
        tfNewNombre.setEnabled(false);
        tfEmpresa.setEnabled(false);
        tfPatente.setEnabled(false);
        tfTelefono.setEnabled(false);

        chkNuevo.addActionListener(ev -> {
            boolean nuevo = chkNuevo.isSelected();
            cbTrans.setEnabled(!nuevo);
            tfNewId.setEnabled(nuevo);
            tfNewNombre.setEnabled(nuevo);
            tfEmpresa.setEnabled(nuevo);
            tfPatente.setEnabled(nuevo);
            tfTelefono.setEnabled(nuevo);
        });

        // Observaciones
        JTextField tfObs = new JTextField();

        // Armar panel con layout sencillo
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Transportista (ID - Nombre):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; panel.add(cbTrans, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; panel.add(chkNuevo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Nuevo ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(tfNewId, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Nuevo Nombre:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(tfNewNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Empresa (obligatorio):"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; panel.add(tfEmpresa, gbc);

        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Patente (obligatorio):"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; panel.add(tfPatente, gbc);

        gbc.gridx = 0; gbc.gridy = 6; panel.add(new JLabel("Teléfono (obligatorio):"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; panel.add(tfTelefono, gbc);

        gbc.gridx = 0; gbc.gridy = 7; panel.add(new JLabel("Observaciones:"), gbc);
        gbc.gridx = 1; gbc.gridy = 7; panel.add(tfObs, gbc);

        // Mostrar diálogo
        int opt = JOptionPane.showConfirmDialog(this, panel, "Registrar Recepción de Orden", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
            String idTrans = null;
            String obs = tfObs.getText();

            if (chkNuevo.isSelected()) {
                String newId = tfNewId.getText().trim();
                String newName = tfNewNombre.getText().trim();
                String empresa = tfEmpresa.getText().trim();
                String patente = tfPatente.getText().trim();
                String telefono = tfTelefono.getText().trim();

                // Validaciones: todos obligatorios
                if (newId.isEmpty() || newName.isEmpty() || empresa.isEmpty() || patente.isEmpty() || telefono.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Debe ingresar ID, Nombre, Empresa, Patente y Teléfono para el nuevo transportista.", "Atención", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    // Crear y registrar transportista en el controlador
                    modelo.Transportista t = new modelo.Transportista(newId, newName, empresa, patente, telefono);
                    controladorPicada.agregarTransportista(newId, newName, empresa, patente, telefono);

                    idTrans = newId;
                    // Añadir al combo mostrando id junto al nombre (ej: "ID - Nombre")
                    cbTrans.addItem(newId + " - " + newName);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al registrar transportista: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                Object sel = cbTrans.getSelectedItem();
                if (sel != null && !"N/A".equals(sel.toString())) {
                    String selStr = sel.toString();
                    // Extraer ID antes del " - "
                    int idx = selStr.indexOf(" - ");
                    idTrans = idx > 0 ? selStr.substring(0, idx).trim() : selStr.trim();
                } else {
                    idTrans = null;
                }
            }

            try {
                controladorPicada.registrarRecepcion(idOrden, idTrans, obs);
                JOptionPane.showMessageDialog(this, "Recepción registrada con éxito. Pendiente de revisión en el panel de Recepciones.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                actualizarTabla();
                cargarDetallesOrdenCompra();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al registrar recepción: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void registrarPago() {
        int row = tablaOrdenes.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden RECIBIDA.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String idOrden = (String) tablaOrdenes.getModel().getValueAt(row, 0);
        OrdenCompra oc = getOrdenById(idOrden);
        if (oc == null || !"RECIBIDA".equals(oc.getEstado())) {
            JOptionPane.showMessageDialog(this, "La orden debe estar RECIBIDA para registrar pago.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField tfMonto = new JTextField(String.valueOf(oc.getTotal()));
        JComboBox<String> cbMetodo = new JComboBox<>(new String[]{"EFECTIVO", "TRANSFERENCIA", "CHEQUE"});
        Object[] message = {"Monto:", tfMonto, "Método:", cbMetodo};
        int opt = JOptionPane.showConfirmDialog(this, message, "Registrar Pago a Proveedor", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            try {
                double monto = Double.parseDouble(tfMonto.getText().trim());
                String metodo = (String) cbMetodo.getSelectedItem();
                if (monto < oc.getTotal()) {
                    int confirm = JOptionPane.showConfirmDialog(this, "El monto es menor al total. Continuar?", "Confirmar", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;
                }
                controladorPicada.registrarPagoProveedor(idOrden, monto, metodo);
                JOptionPane.showMessageDialog(this, "Pago registrado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                actualizarTabla();
                cargarDetallesOrdenCompra();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Monto inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al registrar pago: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}