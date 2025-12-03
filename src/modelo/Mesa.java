package modelo;

import java.io.Serializable;

public class Mesa implements Serializable {
    private int numero;
    private int capacidad;
    private String estado; // Ej: "DISPONIBLE", "OCUPADA", "RESERVADA"

    public Mesa(int numero, int capacidad, String estado) {
        this.numero = numero;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    // Getters y Setters
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return "Mesa " + numero + " (Cap: " + capacidad + ") [" + estado + "]";
    }
}