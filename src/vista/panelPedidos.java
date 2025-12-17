package vista;

import controlador.ControladorPicada;
import modelo.DetallePedido;
import modelo.Garzon;
import modelo.Mesa;
import modelo.Plato;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class panelPedidos extends JPanel {
    private ControladorPicada controladorPicada;
    private VistaPrincipal mainFrame;

    private JPanel panelServicioCliente;
    private JComboBox BoxMesas;
    private JComboBox BoxGarzones;
    private JButton iniciarPedidoButton;
    private JComboBox comboCategorias;
    private JTable tablaCarta;
    private JButton btnAgregarPlato;
    private JTable tablaPedidoActual;
    private JLabel lblTotal;
    private JButton btnEliminarDelPedido;
    private JButton confirmarPedidoButton;
    private JButton volverButton;
    private JPanel MainPanel;
    private JButton editarPedidoButton;
    private JTable tablaPedidosConfirmados;
    private JTable tablaDetallePedidoConfirmado;
    private JButton iniciarPreparaciónButton;
    private JButton pagarPedidoButton;
    private JButton pedidoListoButton;
    private JButton pedidoEntregadoButton;

    private CardLayout parentCardLayout;
    private JPanel parentContentPanel;


    public panelPedidos(VistaPrincipal mainFrame, CardLayout cl, JPanel contentPanel) {
        this.mainFrame = mainFrame;
        this.parentCardLayout = cl;
        this.parentContentPanel = contentPanel;

        controladorPicada = ControladorPicada.getInstance();

        setLayout(new BorderLayout());
        add(MainPanel, BorderLayout.CENTER);

        // Cargar datos iniciales en la UI (NO abrir diálogos)
        actualizarComboMesas();
        actualizarComboGarzones();
        actualizarCarta();
        actualizarTablaPedidosConfirmados();
        controladorPicada.cargarTablaPedidoActual(tablaPedidoActual);
        if (lblTotal != null) lblTotal.setText("Total: $" + controladorPicada.getTotalPedidoActual());

        // Inicializar listeners (incluye wiring de botones)
        initListeners();

        // Volver
        if (volverButton != null) {
            volverButton.addActionListener(e -> {
                parentCardLayout.show(parentContentPanel, "MENU_PRINCIPAL");
            });
        }

    }

    private void actualizarComboMesas() {
        if (BoxMesas == null) return;

        BoxMesas.removeAllItems();
        // Mostrar todas las mesas (para que la ocupada no desaparezca)
        List<Mesa> listaMesas = controladorPicada.getMesas();
        if (listaMesas != null) {
            for (Mesa mesa : listaMesas) {
                BoxMesas.addItem(mesa);
            }
            if (!listaMesas.isEmpty()) BoxMesas.setSelectedIndex(0);
        }
    }

    private void actualizarComboGarzones() {
        if (BoxGarzones == null) return;

        BoxGarzones.removeAllItems();
        List<Garzon> lista = controladorPicada.getGarzones();
        if (lista != null) {
            for (Garzon g : lista) {
                BoxGarzones.addItem(g);
            }
            if (!lista.isEmpty()) BoxGarzones.setSelectedIndex(0);
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
    }



    private void agregarFilaPLato() {
        if (tablaCarta == null) return;

        int filaSel = tablaCarta.getSelectedRow();
        if (filaSel == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un plato de la carta.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object idObj = tablaCarta.getModel().getValueAt(filaSel, 0);
        if (idObj == null) return;
        String idPlato = idObj.toString();

        JPanel input = new JPanel(new GridLayout(2, 2, 6, 6));
        input.add(new JLabel("Cantidad:"));
        JSpinner spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        input.add(spinnerCantidad);
        input.add(new JLabel("Observaciones:"));
        JTextField tfObserv = new JTextField();
        input.add(tfObserv);

        int opcion = JOptionPane.showConfirmDialog(this, input, "Agregar plato al pedido", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) return;

        int cantidad = (Integer) spinnerCantidad.getValue();
        String observ = tfObserv.getText();

        try {
            controladorPicada.agregarPlatoAlPedido(idPlato, cantidad, observ);
            controladorPicada.cargarTablaPedidoActual(tablaPedidoActual);
            if (lblTotal != null) lblTotal.setText("Total: $" + controladorPicada.getTotalPedidoActual());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrarPlatos() {
        if (tablaCarta == null) return;

        String sel = comboCategorias != null ? java.util.Objects.toString(comboCategorias.getSelectedItem(), "Todas") : "Todas";
        if ("Todas".equalsIgnoreCase(sel)) {
            actualizarCarta();
            return;
        }

        List<Plato> platos = controladorPicada.obtenerPlatosPorCategoria(sel);
        platos = platos != null ? platos : java.util.Collections.emptyList();

        String[] columnas = {"ID", "Nombre", "Categoría", "Precio", "Disponibilidad"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Plato p : platos) {
            modelo.addRow(new Object[]{
                    p.getIdPlato(),
                    p.getNombre(),
                    p.getTipo(),
                    p.getPrecio(),
                    p.getDisponibilidad()
            });
        }

        tablaCarta.setModel(modelo);

        int[] anchos = {60, 200, 100, 80, 80};
        for (int i = 0; i < anchos.length && i < tablaCarta.getColumnModel().getColumnCount(); i++) {
            tablaCarta.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }
    public void actualizarTablaPedidosConfirmados() {
        controladorPicada.cargarTablaPedidosConfirmados(tablaPedidosConfirmados);
    }
    public void actualizarTablaDetallePedidoConfirmado() {
        if (tablaPedidosConfirmados == null || tablaDetallePedidoConfirmado == null) return;

        int filaSel = tablaPedidosConfirmados.getSelectedRow();
        if (filaSel == -1) {
            // limpiar tabla detalle si no hay selección
            tablaDetallePedidoConfirmado.setModel(new DefaultTableModel(new Object[0][0], new String[]{"ID Plato","Nombre","Cantidad","Observaciones","Subtotal"}) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            });
            return;
        }

        // Convertir índice de vista a índice de modelo (evita mantener siempre el primer id)
        int modelRow = tablaPedidosConfirmados.convertRowIndexToModel(filaSel);
        Object idObj = tablaPedidosConfirmados.getModel().getValueAt(modelRow, 0);
        if (idObj == null) return;
        String idPedido = idObj.toString();

        controladorPicada.cargarTablaDetallePedidoConfirmado(tablaDetallePedidoConfirmado, idPedido);
    }
    public void editarPedido() {
        if (tablaPedidosConfirmados == null) return;

        int filaSel = tablaPedidosConfirmados.getSelectedRow();
        if (filaSel == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido para editar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tablaPedidosConfirmados.convertRowIndexToModel(filaSel);
        Object idObj = tablaPedidosConfirmados.getModel().getValueAt(modelRow, 0);
        if (idObj == null) return;
        String idPedido = idObj.toString();

        try {
            controladorPicada.empezarEdicionPedido(idPedido);

            // Actualizar UI: cargar pedido actual en la tabla de edición y habilitar controles
            controladorPicada.cargarTablaPedidoActual(tablaPedidoActual);
            actualizarTablaPedidosConfirmados(); // quita el pedido del historial visualmente

            // REFRESCAR tabla de carta porque empezarEdicionPedido devuelve stock
            controladorPicada.cargarTablaCarta(tablaCarta);

            if (btnAgregarPlato != null) btnAgregarPlato.setEnabled(true);
            if (btnEliminarDelPedido != null) btnEliminarDelPedido.setEnabled(true);
            if (confirmarPedidoButton != null) confirmarPedidoButton.setEnabled(true);
            if (lblTotal != null) lblTotal.setText("Total: $" + controladorPicada.getTotalPedidoActual());

            JOptionPane.showMessageDialog(this, "Pedido cargado para edición.", "Edición", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void iniciarPreparacionPedido() {
        if (tablaPedidosConfirmados == null) return;

        int filaSel = tablaPedidosConfirmados.getSelectedRow();
        if (filaSel == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido.", "Atención", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = tablaPedidosConfirmados.convertRowIndexToModel(filaSel);
        Object idObj = tablaPedidosConfirmados.getModel().getValueAt(modelRow, 0);
        if (idObj == null) return;
        String idPedido = idObj.toString();

        try {
            controladorPicada.iniciarPreparacionPedido(idPedido);
            actualizarTablaPedidosConfirmados();
            actualizarTablaDetallePedidoConfirmado();
            JOptionPane.showMessageDialog(this, "Pedido pasado a EN_PREPARACION.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void pagarPedido() {
        int filaSel = tablaPedidosConfirmados.getSelectedRow();
        if (filaSel == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido para pagar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object idObj = tablaPedidosConfirmados.getModel().getValueAt(filaSel, 0);
        if (idObj == null) return;
        String idPedido = idObj.toString();

        try {
            controladorPicada.procesarPagoPedido(idPedido, this.mainFrame, parentCardLayout, parentContentPanel);
            actualizarTablaPedidosConfirmados();
            tablaDetallePedidoConfirmado.setModel(new DefaultTableModel());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void initListeners() {
        // Inicializar estados
        if (btnAgregarPlato != null) btnAgregarPlato.setEnabled(false);
        if (btnEliminarDelPedido != null) btnEliminarDelPedido.setEnabled(false);
        if (confirmarPedidoButton != null) confirmarPedidoButton.setEnabled(false);
        if (pagarPedidoButton != null) pagarPedidoButton.setEnabled(false);
        if (iniciarPedidoButton != null) iniciarPedidoButton.setEnabled(false);

        // Listener general: habilita botón iniciar cuando hay mesa y garzón seleccionados
        ItemListener comboListener = e -> {
            boolean mesaSeleccionada = BoxMesas != null && BoxMesas.getSelectedItem() != null;
            boolean garzonSeleccionado = BoxGarzones != null && BoxGarzones.getSelectedItem() != null;
            if (iniciarPedidoButton != null) iniciarPedidoButton.setEnabled(mesaSeleccionada && garzonSeleccionado);
        };

        if (BoxMesas != null) BoxMesas.addItemListener(comboListener);
        if (BoxGarzones != null) BoxGarzones.addItemListener(comboListener);

        // Iniciar atención (RUT y nombre opcionales)
        if (iniciarPedidoButton != null) {
            iniciarPedidoButton.addActionListener(ev -> {
                try {
                    Object mesaObj = BoxMesas != null ? BoxMesas.getSelectedItem() : null;
                    Object garzonObj = BoxGarzones != null ? BoxGarzones.getSelectedItem() : null;
                    if (mesaObj == null || garzonObj == null) {
                        JOptionPane.showMessageDialog(this, "Seleccione mesa y garzón.", "Atención", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    int numeroMesa = (mesaObj instanceof Mesa) ? ((Mesa) mesaObj).getNumero()
                            : Integer.parseInt(mesaObj.toString());
                    String idGarzon = (garzonObj instanceof Garzon) ? ((Garzon) garzonObj).getIdGarzon()
                            : garzonObj.toString();

                    String rut = JOptionPane.showInputDialog(this, "Ingrese RUT del cliente (opcional):", "");
                    if (rut == null) rut = ""; // cancelar -> tratar como vacío
                    String nombre = JOptionPane.showInputDialog(this, "Ingrese nombre del cliente (opcional):", "");
                    if (nombre == null) nombre = "";

                    controladorPicada.iniciarAtencion(numeroMesa, idGarzon, rut.trim(), nombre.trim());

                    // Habilitar controles de pedido
                    if (btnAgregarPlato != null) btnAgregarPlato.setEnabled(true);
                    if (btnEliminarDelPedido != null) btnEliminarDelPedido.setEnabled(true);
                    if (confirmarPedidoButton != null) confirmarPedidoButton.setEnabled(true);

                    // Actualizar tablas/labels
                    actualizarComboMesas();
                    controladorPicada.cargarTablaPedidoActual(tablaPedidoActual);
                    if (lblTotal != null) lblTotal.setText("Total: $" + controladorPicada.getTotalPedidoActual());

                    JOptionPane.showMessageDialog(this, "Atención iniciada.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        // Listener específico para cambios en la caja de mesas (verifica estado DISPONIBLE)
        ItemListener comboMesaListener = e -> {
            if (e.getStateChange() != java.awt.event.ItemEvent.SELECTED) return;

            Object sel = BoxMesas != null ? BoxMesas.getSelectedItem() : null;
            if (sel == null) {
                actualizarEstadoIniciar();
                return;
            }

            int numeroMesa = -1;
            try {
                numeroMesa = Integer.parseInt(sel.toString());
            } catch (NumberFormatException ex) {
                try {
                    // Si se usan objetos Mesa en el combo, extraer getNumero por reflexión
                    java.lang.reflect.Method m = sel.getClass().getMethod("getNumero");
                    Object val = m.invoke(sel);
                    if (val instanceof Integer) numeroMesa = (Integer) val;
                    else numeroMesa = Integer.parseInt(val.toString());
                } catch (Exception ignored) { }
            }

            if (numeroMesa != -1) {
                final int mesaNum = numeroMesa; // hacerla effectively final
                Mesa m = controladorPicada.getMesas().stream()
                        .filter(x -> x.getNumero() == mesaNum)
                        .findFirst()
                        .orElse(null);
                if (m != null && !"DISPONIBLE".equalsIgnoreCase(m.getEstado())) {
                    if (iniciarPedidoButton != null) iniciarPedidoButton.setEnabled(false);
                    JOptionPane.showMessageDialog(this,
                            "La mesa seleccionada está ocupada. No se puede iniciar atención.",
                            "Atención", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }

            // Si la mesa está disponible, actualizar estado normal de botones
            actualizarEstadoIniciar();
        };

        if (BoxMesas != null) BoxMesas.addItemListener(comboMesaListener);

        // Agregar plato al pedido (solo una invocación)
        if (btnAgregarPlato != null) {
            btnAgregarPlato.addActionListener(ev -> agregarFilaPLato());
        }

        // Eliminar detalle seleccionado del pedido actual
        if (btnEliminarDelPedido != null) {
            btnEliminarDelPedido.addActionListener(ev -> {
                try {
                    int filaSel = tablaPedidoActual.getSelectedRow();
                    if (filaSel == -1) {
                        JOptionPane.showMessageDialog(this, "Seleccione un detalle para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    Object idObj = tablaPedidoActual.getModel().getValueAt(filaSel, 0);
                    if (idObj == null) return;
                    String idPlato = idObj.toString();

                    controladorPicada.eliminarDetalleDelPedidoActual(idPlato);
                    controladorPicada.cargarTablaPedidoActual(tablaPedidoActual);
                    if (lblTotal != null) lblTotal.setText("Total: $" + controladorPicada.getTotalPedidoActual());

                    JOptionPane.showMessageDialog(this, "Detalle eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        // Confirmar pedido y enviar a historial
        // 1) En el listener de confirmar pedido (donde llamas a controladorPicada.confirmarPedidoYEnviarCocina())
        if (confirmarPedidoButton != null) {
            confirmarPedidoButton.addActionListener(ev -> {
                try {
                    controladorPicada.confirmarPedidoYEnviarCocina();
                    // Después de confirmar, deshabilitar botones de edición del pedido
                    if (btnAgregarPlato != null) btnAgregarPlato.setEnabled(false);
                    if (btnEliminarDelPedido != null) btnEliminarDelPedido.setEnabled(false);
                    if (confirmarPedidoButton != null) confirmarPedidoButton.setEnabled(false);

                    // REFRESCAR UI: tabla de pedidos, detalle pedido actual y tabla de carta (stock)
                    actualizarTablaPedidosConfirmados();
                    controladorPicada.cargarTablaPedidoActual(tablaPedidoActual);
                    controladorPicada.cargarTablaCarta(tablaCarta); // <- actualiza disponibilidad visible
                    if (lblTotal != null) lblTotal.setText("Total: $" + controladorPicada.getTotalPedidoActual());
                    JOptionPane.showMessageDialog(this, "Pedido confirmado (estado ABIERTO).", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        // Pagar pedido seleccionado en la tabla de pedidos confirmados
        if (tablaPedidosConfirmados != null) {
            tablaPedidosConfirmados.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    actualizarTablaDetallePedidoConfirmado();
                    // Habilitar pagar solo si el pedido seleccionado está ENTREGADO y no PAGADO
                    int filaSel = tablaPedidosConfirmados.getSelectedRow();
                    boolean habilitarPago = false;
                    if (filaSel != -1) {
                        int modelRow = tablaPedidosConfirmados.convertRowIndexToModel(filaSel);
                        Object estadoObj = tablaPedidosConfirmados.getModel().getValueAt(modelRow, 2); // columna Estado
                        Object idObj = tablaPedidosConfirmados.getModel().getValueAt(modelRow, 0);
                        if (estadoObj != null && idObj != null) {
                            String estado = estadoObj.toString();
                            if ("ENTREGADO".equalsIgnoreCase(estado)) {
                                habilitarPago = true;
                            }
                        }
                    }
                    if (pagarPedidoButton != null) pagarPedidoButton.setEnabled(habilitarPago);
                }
            });
        }
        if (pedidoListoButton != null) {
            pedidoListoButton.addActionListener(ev -> {
                int filaSel = tablaPedidosConfirmados != null ? tablaPedidosConfirmados.getSelectedRow() : -1;
                if (filaSel == -1) {
                    JOptionPane.showMessageDialog(this, "Seleccione un pedido.", "Atención", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int modelRow = tablaPedidosConfirmados.convertRowIndexToModel(filaSel);
                Object idObj = tablaPedidosConfirmados.getModel().getValueAt(modelRow, 0);
                if (idObj == null) return;
                String idPedido = idObj.toString();
                try {
                    controladorPicada.marcarPedidoListo(idPedido);
                    actualizarTablaPedidosConfirmados();
                    actualizarTablaDetallePedidoConfirmado();
                    JOptionPane.showMessageDialog(this, "Pedido marcado como LISTO.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
        if (pedidoEntregadoButton != null) {
            pedidoEntregadoButton.addActionListener(ev -> {
                int filaSel = tablaPedidosConfirmados != null ? tablaPedidosConfirmados.getSelectedRow() : -1;
                if (filaSel == -1) {
                    JOptionPane.showMessageDialog(this, "Seleccione un pedido.", "Atención", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int modelRow = tablaPedidosConfirmados.convertRowIndexToModel(filaSel);
                Object idObj = tablaPedidosConfirmados.getModel().getValueAt(modelRow, 0);
                if (idObj == null) return;
                String idPedido = idObj.toString();
                try {
                    controladorPicada.marcarPedidoEntregado(idPedido);
                    actualizarTablaPedidosConfirmados();
                    actualizarTablaDetallePedidoConfirmado();
                    JOptionPane.showMessageDialog(this, "Pedido marcado como ENTREGADO.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
        if (pagarPedidoButton != null) {
            pagarPedidoButton.addActionListener(ev -> pagarPedido());
        }
        // Botones de acciones sobre pedidos confirmados
        if (iniciarPreparaciónButton != null) {
            iniciarPreparaciónButton.addActionListener(ev -> {
                iniciarPreparacionPedido();
                actualizarTablaPedidosConfirmados();
                actualizarTablaDetallePedidoConfirmado();
                controladorPicada.cargarTablaCarta(tablaCarta); // <- muestra el stock ya descontado
            });
        }

        if (editarPedidoButton != null) {
            editarPedidoButton.addActionListener(ev -> {
                editarPedido();
                actualizarTablaPedidosConfirmados();
                actualizarTablaDetallePedidoConfirmado();
            });
        }

        // Actualizar detalle cuando se selecciona un pedido confirmado
        if (tablaPedidosConfirmados != null) {
            tablaPedidosConfirmados.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    actualizarTablaDetallePedidoConfirmado();
                }
            });
        }

        // Doble clic en tablaPedidoActual para ver más detalle (observaciones, cantidad, subtotal)
        if (tablaPedidoActual != null) {
            tablaPedidoActual.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && tablaPedidoActual.getSelectedRow() != -1) {
                        int fila = tablaPedidoActual.getSelectedRow();
                        Object idObj = tablaPedidoActual.getModel().getValueAt(fila, 0);
                        if (idObj == null) return;
                        String idPlato = idObj.toString();

                        DetallePedido detalleEncontrado = controladorPicada.getDetallesPedidoActual().stream()
                                .filter(d -> d.getPlato() != null && idPlato.equals(d.getPlato().getIdPlato()))
                                .findFirst().orElse(null);

                        if (detalleEncontrado != null) {
                            String mensaje = String.format("Plato: %s\nCantidad: %d\nObservaciones: %s\nSubtotal: $%d",
                                    detalleEncontrado.getPlato() != null ? detalleEncontrado.getPlato().getNombre() : "",
                                    detalleEncontrado.getCantidad(),
                                    detalleEncontrado.getObservaciones() != null ? detalleEncontrado.getObservaciones() : "",
                                    detalleEncontrado.getSubTotal());
                            JOptionPane.showMessageDialog(panelPedidos.this, mensaje, "Detalle del ítem", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            });
        }
        }
    private void actualizarEstadoIniciar() {
        boolean mesaSeleccionada = BoxMesas != null && BoxMesas.getSelectedItem() != null;
        boolean garzonSeleccionado = BoxGarzones != null && BoxGarzones.getSelectedItem() != null;
        if (iniciarPedidoButton != null) iniciarPedidoButton.setEnabled(mesaSeleccionada && garzonSeleccionado);
    }
}