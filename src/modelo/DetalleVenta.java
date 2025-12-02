package modelo;

import java.io.Serializable;

public class DetalleVenta implements Serializable {

    private Producto producto;
    private int cantidad;
    private int subtotal;

    public DetalleVenta(Producto producto, int cantidad) {
        if (producto == null) throw new IllegalArgumentException("Producto nulo.");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
        if (cantidad > producto.getStock()) throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());

        this.producto = producto;
        this.cantidad = cantidad;
        this.subtotal = producto.calcularSubtotal(cantidad);
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public int getSubtotal() { return subtotal; }

    @Override
    public String toString() {
        return producto.getNombre() + " x" + cantidad + " = $" + subtotal;
    }
}
