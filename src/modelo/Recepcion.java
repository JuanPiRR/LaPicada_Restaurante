package modelo;

import java.io.Serializable;
import java.util.Date;

public class Recepcion implements Serializable {
    private String idRecepcion;
    private Date fecha;
    private String estado; // "COMPLETA", "INCOMPLETA", "PENDIENTE"
    private String observaciones;
    private Transportista transportista;
    private OrdenCompra ordenCompra;

    public Recepcion(String idRecepcion, OrdenCompra ordenCompra, Transportista transportista) {
        this.idRecepcion = idRecepcion;
        this.ordenCompra = ordenCompra;
        this.transportista = transportista;
        this.fecha = new Date();
        this.estado = "PENDIENTE"; // Por defecto, se asume pendiente
        this.observaciones = "";
    }

    // Getters y Setters
    public String getIdRecepcion() { return idRecepcion; }
    public Date getFecha() { return fecha; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Transportista getTransportista() { return transportista; }
    public OrdenCompra getOrdenCompra() { return ordenCompra; }

    public void procesarRecepcion() throws Exception {
        if (ordenCompra == null) {
            throw new Exception("No hay orden de compra asociada");
        }

        if ("COMPLETA".equals(estado)) {
            // Actualizar stock de insumos
            for (DetalleOrdenCompra detalle : ordenCompra.getDetalles()) {
                detalle.getInsumo().agregarStock(detalle.getCantidad());
            }
            ordenCompra.cambiarEstado("RECIBIDA");
        } else {
            ordenCompra.cambiarEstado("INCOMPLETA");
        }
    }

    @Override
    public String toString() {
        return "Recepción #" + idRecepcion + " - " + fecha + " - Estado: " + estado;
    }

    public void setFecha(Date date) {
        this.fecha = date;
    }
}