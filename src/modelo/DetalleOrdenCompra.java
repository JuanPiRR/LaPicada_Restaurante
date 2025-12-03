package modelo;

import java.io.Serializable;

public class DetalleOrdenCompra implements Serializable {
    private Insumo insumo;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public DetalleOrdenCompra(Insumo insumo, int cantidad, double precioUnitario) {
        this.insumo = insumo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad * precioUnitario;
    }

    // Getters y Setters
    public Insumo getInsumo() { return insumo; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        this.subtotal = this.cantidad * precioUnitario;
    }
    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad * precioUnitario;
    }
    public double getSubtotal() { return subtotal; }

    @Override
    public String toString() {
        return insumo.getNombre() + " x" + cantidad + " " + insumo.getUnidadMedida() +
                " @ $" + precioUnitario + " = $" + subtotal;
    }
}