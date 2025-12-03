package modelo;

import java.io.Serializable;

public class Insumo implements Serializable {
    private String idInsumo;
    private String nombre;
    private String categoria;
    private String unidadMedida;
    private int stockMinimo;
    private int stockActual;
    private double precioUnitario;

    public Insumo(String idInsumo, String nombre, String categoria, String unidadMedida,
                  int stockMinimo, int stockActual, double precioUnitario) {
        this.idInsumo = idInsumo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.unidadMedida = unidadMedida;
        this.stockMinimo = stockMinimo;
        this.stockActual = stockActual;
        this.precioUnitario = precioUnitario;
    }

    // Getters y Setters
    public String getIdInsumo() { return idInsumo; }
    public void setIdInsumo(String idInsumo) { this.idInsumo = idInsumo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public boolean necesitaReposicion() {
        return stockActual <= stockMinimo;
    }

    public void agregarStock(int cantidad) {
        this.stockActual += cantidad;
    }

    public void consumirStock(int cantidad) throws Exception {
        if (cantidad > stockActual) {
            throw new Exception("Stock insuficiente de " + nombre);
        }
        this.stockActual -= cantidad;
    }

    @Override
    public String toString() {
        return nombre + " [" + categoria + "] - Stock: " + stockActual + " " + unidadMedida +
                " (Mín: " + stockMinimo + ") - $" + precioUnitario + "/" + unidadMedida;
    }
}