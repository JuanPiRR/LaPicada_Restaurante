package modelo;

import java.io.Serializable;

public class Proveedor implements Serializable {
    private String idProveedor;
    private String nombre;
    private String telefono;
    private String email;
    private String tipoProducto;

    public Proveedor(String idProveedor, String nombre, String telefono,
                     String email, String tipoProducto) {
        this.idProveedor = idProveedor;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.tipoProducto = tipoProducto;
    }

    // Getters y Setters
    public String getIdProveedor() { return idProveedor; }
    public void setIdProveedor(String idProveedor) { this.idProveedor = idProveedor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(String tipoProducto) { this.tipoProducto = tipoProducto; }

    @Override
    public String toString() {
        return nombre + " (" + tipoProducto + ") - Tel: " + telefono;
    }
}