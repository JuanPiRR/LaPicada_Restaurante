package modelo;

import java.io.Serializable;

public class Transportista implements Serializable {
    private String idTransportista;
    private String nombre;
    private String empresa;
    private String patente;
    private String telefono;

    public Transportista(String idTransportista, String nombre, String empresa,
                         String patente, String telefono) {
        this.idTransportista = idTransportista;
        this.nombre = nombre;
        this.empresa = empresa;
        this.patente = patente;
        this.telefono = telefono;
    }

    // Getters y Setters
    public String getIdTransportista() { return idTransportista; }
    public void setIdTransportista(String idTransportista) { this.idTransportista = idTransportista; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    @Override
    public String toString() {
        return nombre + " (" + empresa + ") - Patente: " + patente;
    }
}