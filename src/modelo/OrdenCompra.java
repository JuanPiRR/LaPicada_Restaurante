package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrdenCompra implements Serializable {
    private String idOrden;
    private Date fecha;
    private String estado; // "PENDIENTE", "ENVIADA", "RECIBIDA", "CANCELADA"
    private double total;
    private Proveedor proveedor;
    private List<DetalleOrdenCompra> detalles;
    private Recepcion recepcion;
    private PagoProveedor pago;

    public OrdenCompra(String idOrden, Proveedor proveedor) {
        this.idOrden = idOrden;
        this.proveedor = proveedor;
        this.fecha = new Date();
        this.estado = "PENDIENTE";
        this.total = 0.0;
        this.detalles = new ArrayList<>();
        this.recepcion = null;
        this.pago = null;
    }

    public void agregarDetalle(Insumo insumo, int cantidad, double precioUnitario) {
        // Verificar si el insumo ya está en la orden
        for (DetalleOrdenCompra detalle : detalles) {
            if (detalle.getInsumo().getIdInsumo().equals(insumo.getIdInsumo())) {
                detalle.setCantidad(detalle.getCantidad() + cantidad);
                detalle.setPrecioUnitario(precioUnitario);
                calcularTotal();
                return;
            }
        }

        // Si no está, crear nuevo detalle
        DetalleOrdenCompra nuevoDetalle = new DetalleOrdenCompra(insumo, cantidad, precioUnitario);
        detalles.add(nuevoDetalle);
        calcularTotal();
    }

    private void calcularTotal() {
        this.total = 0.0;
        for (DetalleOrdenCompra detalle : detalles) {
            this.total += detalle.getSubtotal();
        }
    }

    public void cambiarEstado(String nuevoEstado) {
        this.estado = nuevoEstado;
    }

    // Getters y Setters
    public String getIdOrden() { return idOrden; }
    public Date getFecha() { return fecha; }
    public String getEstado() { return estado; }
    public double getTotal() { return total; }
    public Proveedor getProveedor() { return proveedor; }
    public List<DetalleOrdenCompra> getDetalles() { return detalles; }
    public Recepcion getRecepcion() { return recepcion; }
    public void setRecepcion(Recepcion recepcion) { this.recepcion = recepcion; }
    public PagoProveedor getPago() { return pago; }
    public void setPago(PagoProveedor pago) { this.pago = pago; }

    @Override
    public String toString() {
        return "Orden #" + idOrden + " - " + proveedor.getNombre() +
                " - Total: $" + total + " - Estado: " + estado;
    }
}