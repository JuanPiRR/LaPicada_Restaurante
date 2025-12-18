// java
package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VistaPrincipal extends JFrame {
    private JPanel panel1;
    private JButton pedidos;
    private JButton carta;
    private JButton insumos;
    private JLabel banner;
    private JButton proveedores;
    private JButton button1;
    private JButton button2;
    private JButton button3;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;


    public VistaPrincipal() {
        setTitle("La Picada Restaurante Sistema");
        setSize(1500, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Inicializar el CardLayout y el contenedor
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        // 2. Crear las instancias de los paneles de vista
        // La vista de pedidos:
        panelPedidos vistaPedidos = new panelPedidos(this, cardLayout, mainContentPanel);
        panelCarta vistaCarta = new panelCarta(this, cardLayout, mainContentPanel);
        panelInsumos vistaInsumos = new panelInsumos(this, cardLayout, mainContentPanel);
        panelProveedores vistaProveedores = new panelProveedores(this, cardLayout, mainContentPanel);
        panelOrdenesCompra vistaOrdenesCompra = new panelOrdenesCompra(this, cardLayout, mainContentPanel);
        panelRecepciones vistaRecepciones = new panelRecepciones(this, cardLayout, mainContentPanel);

        // 3. Agregar las vistas al mainContentPanel con un nombre clave
        // La "carta" del menú principal (el panel1 que viene del .form)
        mainContentPanel.add(panel1, "MENU_PRINCIPAL");

        // La "carta" de la vista de pedidos
        //mainContentPanel.add(vistaPedidos.getMainPanel(), "VISTA_PEDIDOS");
        mainContentPanel.add(vistaPedidos, "VISTA_PEDIDOS");
        mainContentPanel.add(vistaCarta, "VISTA_CARTA");
        mainContentPanel.add(vistaInsumos, "VISTA_INSUMOS");
        mainContentPanel.add(vistaProveedores, "VISTA_PROVEEDORES");
        mainContentPanel.add(vistaOrdenesCompra, "VISTA_ORDENES_COMPRA");
        mainContentPanel.add(vistaRecepciones, "VISTA_RECEPCIONES");

        // 4. Agregar el contenedor principal al JFrame
        // Usamos setContentPane() solo una vez para agregar el contenedor principal
        setContentPane(mainContentPanel);

        // 5. Lógica del Botón "Pedidos"
        pedidos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainContentPanel, "VISTA_PEDIDOS");
            }
        });
        // 6. Lógica del Botón "Carta"
        carta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainContentPanel, "VISTA_CARTA");
            }
        });
        // 7. Lógica del Botón "Insumos"
        insumos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainContentPanel, "VISTA_INSUMOS");
            }
        });
        // 8. Lógica del Botón "Proveedores"
        proveedores.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainContentPanel, "VISTA_PROVEEDORES");
            }
        });
        // 9. Lógica del Botón "Órdenes de Compra"
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainContentPanel, "VISTA_ORDENES_COMPRA");
            }
        });
        // 10. Lógica del Botón "Recepciones"
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainContentPanel, "VISTA_RECEPCIONES");
            }
        });
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        setVisible(true);
    }
}
