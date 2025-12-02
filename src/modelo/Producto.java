package modelo;

import java.io.Serializable;

public class Producto implements Serializable {

    private String id;
    private String nombre;
    private int precio;
    private int stock;
    private String categoria; // e.g. "Bebida", "Colacion", "MenuDelDia", "Snack"

    public Producto(String id, String nombre, int precio, int stock, String categoria) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("ID inválido.");
        if (nombre == null || nombre.trim().isEmpty()) throw new IllegalArgumentException("Nombre inválido.");
        if (precio < 0) throw new IllegalArgumentException("El precio no puede ser negativo.");
        if (stock < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");
        if (categoria == null || categoria.trim().isEmpty()) categoria = "SinCategoria";

        this.id = id.trim();
        this.nombre = nombre.trim();
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria.trim();
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public int getPrecio() { return precio; }
    public int getStock() { return stock; }
    public String getCategoria() { return categoria; }

    public void setPrecio(int precio) {
        if (precio < 0) throw new IllegalArgumentException("El precio no puede ser negativo.");
        this.precio = precio;
    }

    public void setStock(int stock) {
        if (stock < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");
        this.stock = stock;
    }

    public int calcularSubtotal(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida.");
        return precio * cantidad;
    }

    public void disminuirStock(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida.");
        if (cantidad > stock) throw new IllegalArgumentException("Stock insuficiente.");
        stock -= cantidad;
    }

    public void aumentarStock(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida.");
        stock += cantidad;
    }

    @Override
    public String toString() {
        return nombre + " ($" + precio + ") - Stock: " + stock + " - " + categoria;
    }
}
