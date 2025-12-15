package modelo;

import java.io.Serializable;

public class Plato implements Serializable {
    private String idPlato;
    private String nombre;
    private int precio;
    private String tipo;          // Ej: "Mariscos", "Carne", "Bebestible"
    private int disponibilidad;   // Esto es el Stock actual

    public Plato(String idPlato, String nombre, int precio, String tipo, int disponibilidad) {
        this.idPlato = idPlato;
        this.nombre = nombre;
        this.precio = precio;
        this.tipo = tipo;
        this.disponibilidad = disponibilidad;
    }

    // Método para actualizar stock
    public void restarStock(int cantidad) {
        if (this.disponibilidad >= cantidad) {
            this.disponibilidad -= cantidad;
        }
    }

    // Método para agregar stock (usado en abastecimiento)
    public void agregarStock(int cantidad) {
        this.disponibilidad += cantidad;
    }

    // Getters y Setters
    public String getIdPlato() { return idPlato; }
    public void setIdPlato(String idPlato) { this.idPlato = idPlato; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getPrecio() { return precio; }
    public void setPrecio(int precio) { this.precio = precio; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(int disponibilidad) { this.disponibilidad = disponibilidad; }


    @Override
    public String toString() {
        return nombre + " ($" + precio + ") - Stock: " + disponibilidad;
    }
}