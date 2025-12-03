package modelo;

import java.io.Serializable;

public class DetallePedido implements Serializable {
    private Plato plato;
    private int cantidad;
    private int subTotal;

    public DetallePedido(Plato plato, int cantidad) {
        this.plato = plato;
        this.cantidad = cantidad;
        this.subTotal = plato.getPrecio() * cantidad;
    }

    // Getters y Setters
    public Plato getPlato() { return plato; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        // Recalcular subtotal si cambia la cantidad
        this.subTotal = this.plato.getPrecio() * cantidad;
    }

    public int getSubTotal() { return subTotal; }
    public void setSubTotal(int subTotal) { this.subTotal = subTotal; }

    @Override
    public String toString() {
        return plato.getNombre() + " x" + cantidad + " = $" + subTotal;
    }
}
