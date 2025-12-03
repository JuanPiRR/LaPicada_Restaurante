package modelo;

import java.io.Serializable;

public class Garzon implements Serializable {
    private String idGarzon;
    private String nombre;
    private String turno; // Ej: "Mañana", "Tarde"

    public Garzon(String idGarzon, String nombre, String turno) {
        this.idGarzon = idGarzon;
        this.nombre = nombre;
        this.turno = turno;
    }

    // Getters y Setters
    public String getIdGarzon() { return idGarzon; }
    public void setIdGarzon(String idGarzon) { this.idGarzon = idGarzon; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }

    @Override
    public String toString() {
        return nombre + " (Turno: " + turno + ")";
    }
}