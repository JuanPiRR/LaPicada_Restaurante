package modelo;

import java.io.Serializable;

public class DetalleOrdenCompra implements Serializable {

    private Insumo insumo;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    private int cantidadRecibida;

    public DetalleOrdenCompra(Insumo insumo, int cantidad, double precioUnitario) {
        this.insumo = insumo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad * precioUnitario;
        this.cantidadRecibida = 0; // Inicialmente 0, se actualiza al procesar recepción
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
    public int getCantidadRecibida() {
        return cantidadRecibida;
    }

    public void setCantidadRecibida(int cantidadRecibida) {
        this.cantidadRecibida = cantidadRecibida;
    }

    @Override
    public String toString() {
        return insumo.getNombre() + " x" + cantidad + " " + insumo.getUnidadMedida() +
                " @ $" + precioUnitario + " = $" + subtotal;
    }
}