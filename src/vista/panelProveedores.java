package vista;

import controlador.ControladorPicada;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class panelProveedores extends JPanel {
    private ControladorPicada controladorPicada;
    private VistaPrincipal mainFrame;

    private CardLayout parentCardLayout;
    private JPanel parentContentPanel;
    private JPanel MainPanel;
    private JTable tablaProveedores;
    private JButton agregarProveedorButton;
    private JButton editarProveedorButton;
    private JButton eliminarProveedorButton;
    private JButton volverButton;

    public panelProveedores(VistaPrincipal mainFrame, CardLayout cl, JPanel contentPanel) {
        this.mainFrame = mainFrame;
        this.parentCardLayout = cl;
        this.parentContentPanel = contentPanel;

        controladorPicada = ControladorPicada.getInstance();

        setLayout(new BorderLayout());
        add(MainPanel, BorderLayout.CENTER);

        actualizarTabla();
        if (agregarProveedorButton != null) {
            agregarProveedorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    agregarProveedor();
                }
            });
        }
        if (editarProveedorButton != null) {
            editarProveedorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editarProveedor();
                }
            });
        }
        if (eliminarProveedorButton != null) {
            eliminarProveedorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    eliminarProveedor();
                }
            });
        }

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
    }
    public void actualizarTabla() {
        controladorPicada.cargarProveedoresEnTabla(tablaProveedores);
    }
    public void agregarProveedor() {
        if (tablaProveedores == null) return;

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        JTextField idField = new JTextField();
        JTextField nombreField = new JTextField();
        JTextField telefonoField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField tipoField = new JTextField();

        panel.add(new JLabel("ID:"));
        panel.add(idField);
        panel.add(new JLabel("Nombre:"));
        panel.add(nombreField);
        panel.add(new JLabel("Teléfono:"));
        panel.add(telefonoField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Tipo de producto:"));
        panel.add(tipoField);

        int opcion = JOptionPane.showConfirmDialog(this, panel, "Agregar proveedor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) return;

        String id = idField.getText() != null ? idField.getText().trim() : "";
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El ID no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nombre = nombreField.getText() != null ? nombreField.getText().trim() : "";
        String telefono = telefonoField.getText() != null ? telefonoField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String tipo = tipoField.getText() != null ? tipoField.getText().trim() : "";

        try {
            controladorPicada.agregarProveedor(id, nombre, telefono, email, tipo);
            actualizarTabla();
            JOptionPane.showMessageDialog(this, "Proveedor agregado correctamente.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void editarProveedor() {
        if (tablaProveedores == null) return;
        int fila = tablaProveedores.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un proveedor para editar.", "Atención", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String id = String.valueOf(tablaProveedores.getValueAt(fila, 0));
        String nombreActual = String.valueOf(tablaProveedores.getValueAt(fila, 1));
        String telefonoActual = String.valueOf(tablaProveedores.getValueAt(fila, 2));
        String emailActual = String.valueOf(tablaProveedores.getValueAt(fila, 3));
        String tipoActual = String.valueOf(tablaProveedores.getValueAt(fila, 4));

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        JTextField idField = new JTextField(id);
        idField.setEditable(false);
        JTextField nombreField = new JTextField(nombreActual);
        JTextField telefonoField = new JTextField(telefonoActual);
        JTextField emailField = new JTextField(emailActual);
        JTextField tipoField = new JTextField(tipoActual);

        panel.add(new JLabel("ID:"));
        panel.add(idField);
        panel.add(new JLabel("Nombre:"));
        panel.add(nombreField);
        panel.add(new JLabel("Teléfono:"));
        panel.add(telefonoField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Tipo de producto:"));
        panel.add(tipoField);

        int opcion = JOptionPane.showConfirmDialog(this, panel, "Editar proveedor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) return;

        String nombre = nombreField.getText() != null ? nombreField.getText().trim() : "";
        String telefono = telefonoField.getText() != null ? telefonoField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String tipo = tipoField.getText() != null ? tipoField.getText().trim() : "";

        try {
            controladorPicada.actualizarProveedor(id, nombre, telefono, email, tipo);
            actualizarTabla();
            JOptionPane.showMessageDialog(this, "Proveedor actualizado correctamente.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void eliminarProveedor() {
        if (tablaProveedores == null) return;
        int fila = tablaProveedores.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un proveedor para eliminar.", "Atención", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String id = String.valueOf(tablaProveedores.getValueAt(fila, 0));
        int opcion = JOptionPane.showConfirmDialog(this, "¿Eliminar proveedor ID: " + id + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) return;

        try {
            controladorPicada.eliminarProveedor(id);
            actualizarTabla();
            JOptionPane.showMessageDialog(this, "Proveedor eliminado.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    //public void CrearOrdenCompra() {}
}
