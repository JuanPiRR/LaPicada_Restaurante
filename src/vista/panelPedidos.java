package vista;

import controlador.ControladorPicada;
import modelo.DetallePedido;
import modelo.Garzon;
import modelo.Mesa;
import modelo.Plato;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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

    private CardLayout parentCardLayout;
    private JPanel parentContentPanel;

    public panelPedidos(VistaPrincipal mainFrame, CardLayout cl, JPanel contentPanel) {
        this.mainFrame = mainFrame;
        this.parentCardLayout = cl;
        this.parentContentPanel = contentPanel;

        controladorPicada = ControladorPicada.getInstance();

        setLayout(new BorderLayout());
        add(MainPanel, BorderLayout.CENTER);

        //inicializar combos y listeners
        actualizarComboMesas();
        actualizarComboGarzones();

        if (comboCategorias != null) {
            // poblar con categorías únicas
            comboCategorias.removeAllItems();
            comboCategorias.addItem("Todas");
            List<String> categorias = controladorPicada.obtenerCategoriasUnicas();
            if (categorias != null) {
                for (String c : categorias) comboCategorias.addItem(c);
            }
            comboCategorias.setSelectedIndex(0);

            // listener que usa el método filtrarPlatos()
            comboCategorias.addActionListener(e -> filtrarPlatos());
        }

        actualizarCarta();

        habilitarControlesParaPedido(false);

        if (iniciarPedidoButton != null) {
            iniciarPedidoButton.addActionListener(e -> {
                Object selMesa = BoxMesas != null ? BoxMesas.getSelectedItem() : null;
                Object selGarzon = BoxGarzones != null ? BoxGarzones.getSelectedItem() : null;

                if (selMesa == null || selGarzon == null) {
                    JOptionPane.showMessageDialog(this, "Seleccione mesa y garzón antes de iniciar.", "Atención", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Mesa mesa = (Mesa) selMesa;
                Garzon garzon = (Garzon) selGarzon;

                //pedir datos de cliente
                String rutCliente = JOptionPane.showInputDialog(this, "Ingrese RUT del cliente:", "Cliente", JOptionPane.QUESTION_MESSAGE);
                if (rutCliente == null) return;

                String nombreCliente = JOptionPane.showInputDialog(this, "Ingrese nombre del cliente:", "Cliente", JOptionPane.QUESTION_MESSAGE);
                if (nombreCliente == null) return;
                try {
                    controladorPicada.iniciarAtencion(mesa.getNumero(), garzon.getIdGarzon(), rutCliente.trim(), nombreCliente.trim());
                    // Marcar UI: deshabilitar selección y habilitar controles de pedido
                    if (BoxMesas != null) BoxMesas.setEnabled(false);
                    if (BoxGarzones != null) BoxGarzones.setEnabled(false);
                    iniciarPedidoButton.setEnabled(false);

                    habilitarControlesParaPedido(true);

                    //refrescar carta/mesas para mostrar estado actualizado (opcional)
                    actualizarCarta();
                    // actualizarComboMesas(); // opcional: si se quiere refrescar la lista de mesas disponibles

                    if (lblTotal != null) lblTotal.setText("0");

                    JOptionPane.showMessageDialog(this, "Atención iniciada. Puede agregar platos al pedido.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al iniciar atención: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
        if (btnEliminarDelPedido != null) {
            btnEliminarDelPedido.addActionListener(e -> {
                if (tablaPedidoActual == null) return;
                int fila = tablaPedidoActual.getSelectedRow();
                if (fila < 0) {
                    JOptionPane.showMessageDialog(this, "Seleccione un ítem del pedido para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Object idObj = tablaPedidoActual.getValueAt(fila, 0);
                if (idObj == null) return;
                String idPlato = idObj.toString();

                try {
                    controladorPicada.eliminarDetalleDelPedidoActual(idPlato);
                    agregarFilaPLato();        // refresca tablaPedidoActual
                    actualizarCarta();        // refresca carta (stock u otros cambios)
                    if (lblTotal != null) lblTotal.setText(String.valueOf(controladorPicada.getTotalPedidoActual()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el plato: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        if (btnAgregarPlato != null) {
            btnAgregarPlato.addActionListener(e -> {
                if (tablaCarta == null) return;
                int fila = tablaCarta.getSelectedRow();
                if (fila < 0) {
                    JOptionPane.showMessageDialog(this, "Seleccione un plato de la carta.", "Atención", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                //idPlato que está en la columna 0
                Object idObj = tablaCarta.getValueAt(fila, 0);
                if (idObj == null) return;
                String idPlato = idObj.toString();

                //cantidad
                String sCantidad = JOptionPane.showInputDialog(this, "Ingrese cantidad:", "Cantidad", JOptionPane.QUESTION_MESSAGE);
                if (sCantidad == null) return; // cancelado
                int cantidad;
                try {
                    cantidad = Integer.parseInt(sCantidad.trim());
                    if (cantidad <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Cantidad inválida.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                //observaciones (opcional)
                String observaciones = JOptionPane.showInputDialog(this, "Observaciones (opcional):", "Observaciones", JOptionPane.QUESTION_MESSAGE);
                if (observaciones == null) observaciones = "";

                try {
                    controladorPicada.agregarPlatoAlPedido(idPlato, cantidad, observaciones);
                    agregarFilaPLato();
                    actualizarCarta(); // actualizar stock mostrado si aplica
                    if (lblTotal != null) lblTotal.setText(String.valueOf(controladorPicada.getTotalPedidoActual()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "No se pudo agregar el plato: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        if (confirmarPedidoButton != null) {
            confirmarPedidoButton.addActionListener(e -> {
                try {
                    controladorPicada.confirmarPedidoYEnviarCocina();

                    // Actualizar UI: limpiar tabla de pedido actual, refrescar carta y total, habilitar controles para nuevo pedido
                    agregarFilaPLato(); // quedará vacía porque pedidoActual ahora es null
                    actualizarCarta();
                    habilitarControlesParaPedido(false);
                    if (BoxMesas != null) BoxMesas.setEnabled(true);
                    if (BoxGarzones != null) BoxGarzones.setEnabled(true);
                    if (iniciarPedidoButton != null) iniciarPedidoButton.setEnabled(true);
                    if (lblTotal != null) lblTotal.setText("0");

                    JOptionPane.showMessageDialog(this, "Pedido confirmado y enviado a cocina.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "No se pudo confirmar el pedido: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        if (volverButton != null) {
            volverButton.addActionListener(e -> {
                parentCardLayout.show(parentContentPanel, "MENU_PRINCIPAL");
            });
        }

    }

    private void actualizarComboMesas() {
        if (BoxMesas == null) return;

        BoxMesas.removeAllItems();
        List<Mesa> listaMesas = controladorPicada.getMesasDisponibles();
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
        if (tablaCarta == null) return;

        //definir columnas
        String[] columnas = {"ID", "Nombre", "Categoría", "Precio", "Disponibilidad"};
        DefaultTableModel modelo = modeloTabla(columnas);

        tablaCarta.setModel(modelo);
        // Ajustes opcionales de ancho/orden
        if (tablaCarta.getColumnModel().getColumnCount() > 0) {
            tablaCarta.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
            tablaCarta.getColumnModel().getColumn(1).setPreferredWidth(200); // Nombre
            tablaCarta.getColumnModel().getColumn(2).setPreferredWidth(100); // Categoría
            tablaCarta.getColumnModel().getColumn(3).setPreferredWidth(80);  // Precio
            tablaCarta.getColumnModel().getColumn(4).setPreferredWidth(80);  // Disponibilidad
        }
    }


    private void habilitarControlesParaPedido(boolean habilitar) {
        if (comboCategorias != null) comboCategorias.setEnabled(habilitar);
        if (tablaCarta != null) tablaCarta.setEnabled(habilitar);
        if (btnAgregarPlato != null) btnAgregarPlato.setEnabled(habilitar);
        if (tablaPedidoActual != null) tablaPedidoActual.setEnabled(habilitar);
        if (btnEliminarDelPedido != null) btnEliminarDelPedido.setEnabled(habilitar);
        if (confirmarPedidoButton != null) confirmarPedidoButton.setEnabled(habilitar);
    }

    private DefaultTableModel modeloTabla(String[] columnas) {
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        List<Plato> platos = controladorPicada.getCarta();
        if (platos != null) {
            for (Plato p : platos) {
                Object precio;
                try {
                    precio = p.getPrecio();
                } catch (Throwable ex1) {
                    try {
                        precio = p.getPrecio();
                    } catch (Throwable ex2) {
                        precio = "";
                    }
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
        return modelo;
    }
    private void agregarFilaPLato() {
        if (tablaPedidoActual == null) return;

        String[] columnas = {"ID", "Nombre", "Cantidad", "Precio Unit.", "Subtotal", "Observaciones"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        List<DetallePedido> detalles = controladorPicada.getDetallesPedidoActual();
        if (detalles != null) {
            for (DetallePedido det : detalles) {
                Plato p = det.getPlato();
                int cantidad = det.getCantidad();
                Object precioUnit = p != null ? p.getPrecio() : "";
                int subtotal = 0;
                try {
                    subtotal = (int) ((Number) precioUnit).intValue() * cantidad;
                } catch (Throwable t) {
                    // si precio no es numérico, dejar subtotal 0
                }
                Object[] fila = {
                        p != null ? p.getIdPlato() : "",
                        p != null ? p.getNombre() : "",
                        cantidad,
                        precioUnit,
                        subtotal,
                        det.getObservaciones() != null ? det.getObservaciones() : ""
                };
                modelo.addRow(fila);
            }
        }

        tablaPedidoActual.setModel(modelo);
        // Ajustes opcionales de ancho
        if (tablaPedidoActual.getColumnModel().getColumnCount() > 0) {
            tablaPedidoActual.getColumnModel().getColumn(0).setPreferredWidth(60);
            tablaPedidoActual.getColumnModel().getColumn(1).setPreferredWidth(200);
            tablaPedidoActual.getColumnModel().getColumn(2).setPreferredWidth(60);
            tablaPedidoActual.getColumnModel().getColumn(3).setPreferredWidth(80);
            tablaPedidoActual.getColumnModel().getColumn(4).setPreferredWidth(80);
        }
    }

    private void filtrarPlatos() {
        if (tablaCarta == null) return;

        Object sel = comboCategorias != null ? comboCategorias.getSelectedItem() : null;
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


    public JPanel getMainPanel() {
        return MainPanel;
    }

}