package modelo;

import java.io.Serializable;

public class Cliente implements Serializable {

    private String rut;
    private String nombre;

    public Cliente(String rut, String nombre) {
        if (rut == null || rut.trim().isEmpty()) throw new IllegalArgumentException("RUT inválido.");
        if (nombre == null || nombre.trim().isEmpty()) throw new IllegalArgumentException("Nombre inválido.");
        this.rut = rut.trim();
        this.nombre = nombre.trim();
    }

    public String getRut() { return rut; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() {
        return nombre + " (" + rut + ")";
    }
}