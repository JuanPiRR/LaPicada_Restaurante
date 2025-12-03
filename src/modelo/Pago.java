package modelo;

import java.io.Serializable;
import java.util.Date;

public class Pago implements Serializable {

    // Atributos
    private String idPago;
    private int monto;
    private String metodoPago;  // Ej: "Efectivo", "Tarjeta", "Transferencia"
    private Date fechaHora;

    // Atributos adicionales lógicos para un Restaurante
    private int propina;        // La propina del 10% (o lo que deje el cliente)
    private int vuelto;         // Necesario si paga con efectivo

    public Pago(String idPago, int monto, String metodoPago, int propina) {
        this.idPago = idPago;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.propina = propina;
        this.fechaHora = new Date(); // Se asigna la fecha/hora actual automáticamente al crear el pago
        this.vuelto = 0;
    }

    // Método para obtener el total real que pagó el cliente (Consumo + Propina)
    public int getTotalPagado() {
        return monto + propina;
    }


    // Getters y Setters

    public String getIdPago() {
        return idPago;
    }

    public void setIdPago(String idPago) {
        this.idPago = idPago;
    }

    public int getMonto() {
        return monto;
    }

    public void setMonto(int monto) {
        this.monto = monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Date getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Date fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getPropina() {
        return propina;
    }

    public void setPropina(int propina) {
        this.propina = propina;
    }

    public int getVuelto() {
        return vuelto;
    }

    public void setVuelto(int vuelto) {
        this.vuelto = vuelto;
    }

    @Override
    public String toString() {
        return "Pago [" + idPago + "] " + metodoPago + " - Total: $" + getTotalPagado();
    }
}