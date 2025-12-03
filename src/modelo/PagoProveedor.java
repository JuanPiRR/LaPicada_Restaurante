package modelo;

import java.io.Serializable;
import java.util.Date;

public class PagoProveedor implements Serializable {
    private String idPago;
    private double monto;
    private Date fecha;
    private String metodoPago; // "TRANSFERENCIA", "CHEQUE", "EFECTIVO"
    private OrdenCompra ordenCompra;

    public PagoProveedor(String idPago, double monto, String metodoPago, OrdenCompra ordenCompra) {
        this.idPago = idPago;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.ordenCompra = ordenCompra;
        this.fecha = new Date();
    }

    public void procesarPago() {
        // Aquí ingresar el metodo de calcular el proceso
        ordenCompra.cambiarEstado("PAGADA");
    }

    // Getters y Setters
    public String getIdPago() { return idPago; }
    public double getMonto() { return monto; }
    public Date getFecha() { return fecha; }
    public String getMetodoPago() { return metodoPago; }
    public OrdenCompra getOrdenCompra() { return ordenCompra; }

    @Override
    public String toString() {
        return "Pago #" + idPago + " - $" + monto + " - " + metodoPago + " - " + fecha;
    }
}