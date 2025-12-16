package modelo;

import java.io.Serializable;
import java.util.Date;

public class Pago implements Serializable {
    private String idPago;
    private int monto;           // Monto del consumo
    private String metodoPago;  // "EFECTIVO", "TARJETA", "TRANSFERENCIA"
    private Date fechaHora;
    private int propina;        // Propina opcional
    private int vuelto;         // Solo para efectivo
    private boolean procesado;  // Si ya se procesó el pago
    private Pedido pedidoAsociado;  // Referencia al pedido

    // Constructor principal
    public Pago(String idPago, int monto, String metodoPago, int propina, Pedido pedido) {
        this.idPago = idPago;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.propina = propina;
        this.pedidoAsociado = pedido;
        this.fechaHora = new Date();
        this.vuelto = 0;
        this.procesado = false;
    }

    // Constructor simplificado
    public Pago(String idPago, int monto, String metodoPago, Pedido pedido) {
        this(idPago, monto, metodoPago, 0, pedido);
    }

    // Método para procesar el pago
    public boolean procesarPago(int montoEntregado) throws Exception {
        if (procesado) {
            throw new Exception("El pago ya fue procesado anteriormente");
        }

        switch (metodoPago.toUpperCase()) {
            case "EFECTIVO":
                return procesarEfectivo(montoEntregado);

            case "TARJETA":
            case "TRANSFERENCIA":
                // Para tarjeta/transferencia, asumimos que el pago es exacto
                this.procesado = true;
                this.vuelto = 0;
                return true;

            default:
                throw new Exception("Método de pago no válido: " + metodoPago);
        }
    }

    // Procesar pago en efectivo
    private boolean procesarEfectivo(int montoEntregado) throws Exception {
        if (montoEntregado < monto + propina) {
            throw new Exception("Dinero insuficiente. Total: $" + getTotalAPagar() +
                    ", Entregado: $" + montoEntregado);
        }

        this.vuelto = montoEntregado - (monto + propina);
        this.procesado = true;
        return true;
    }

    // Métodos para cálculos
    public int getTotalAPagar() {
        return monto + propina;
    }

    public int getTotalPagado() {
        return monto + propina;  // Cuando se procesa, es lo que realmente pagó
    }

    // Método para aplicar propina porcentual
    public void aplicarPropinaPorcentual(int porcentaje) {
        if (porcentaje > 0 && porcentaje <= 100) {
            this.propina = (monto * porcentaje) / 100;
        }
    }

    // Método para verificar si el pago está completo
    public boolean isPagoCompleto() {
        return procesado;
    }

    // Getters y Setters adicionales
    public Pedido getPedidoAsociado() {
        return pedidoAsociado;
    }

    public void setPedidoAsociado(Pedido pedidoAsociado) {
        this.pedidoAsociado = pedidoAsociado;
    }

    public boolean isProcesado() {
        return procesado;
    }

    public void setProcesado(boolean procesado) {
        this.procesado = procesado;
    }

    // Getters y Setters antiguos
    public String getIdPago() { return idPago; }
    public void setIdPago(String idPago) { this.idPago = idPago; }

    public int getMonto() { return monto; }
    public void setMonto(int monto) { this.monto = monto; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public Date getFechaHora() { return fechaHora; }
    public void setFechaHora(Date fechaHora) { this.fechaHora = fechaHora; }

    public int getPropina() { return propina; }
    public void setPropina(int propina) { this.propina = propina; }

    public int getVuelto() { return vuelto; }
    public void setVuelto(int vuelto) { this.vuelto = vuelto; }

    @Override
    public String toString() {
        return "Pago [" + idPago + "] " + metodoPago +
                " - Total: $" + getTotalPagado() +
                " - Estado: " + (procesado ? "PROCESADO" : "PENDIENTE");
    }
}