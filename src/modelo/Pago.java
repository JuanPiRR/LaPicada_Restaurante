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
    private int montoProcesado; // monto efectivamente recibido (efectivo entregado)
    private Pedido pedidoAsociado;  // Referencia al pedido
    private String tipoDocumento; // "Boleta" o "Factura" (nuevo)

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
        this.tipoDocumento = null;
    }

    // Constructor simplificado
    public Pago(String idPago, int monto, String metodoPago, Pedido pedido) {
        this(idPago, monto, metodoPago, 0, pedido);
    }

    // Método para procesar el pago
    // Procesar pago sin monto entregado (tarjeta/transferencia o pago ya conocido)
    public void procesarPago() {
        this.montoProcesado = this.monto;
        this.vuelto = 0;
        this.procesado = true;
    }

    public void procesarPago(int montoEntregado) {
        this.montoProcesado = montoEntregado;
        this.vuelto = Math.max(0, montoEntregado - this.monto - this.propina);
        this.procesado = true;
    }

    // Procesar pago en efectivo (método auxiliar; puede lanzar excepción si necesario)
    private boolean procesarEfectivo(int montoEntregado) throws Exception {
        if (montoEntregado < monto + propina) {
            throw new Exception("Dinero insuficiente. Total: $" + getTotalAPagar() +
                    ", Entregado: $" + montoEntregado);
        }

        this.vuelto = montoEntregado - (monto + propina);
        this.montoProcesado = montoEntregado;
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

    // Getters y Setters
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

    public int getMontoProcesado() {
        return montoProcesado;
    }
    public void setMontoProcesado(int montoProcesado) {
        this.montoProcesado = montoProcesado;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    @Override
    public String toString() {
        return "Pago [" + idPago + "] " + metodoPago +
                " - Total: $" + getTotalPagado() +
                " - Estado: " + (procesado ? "PROCESADO" : "PENDIENTE") +
                (tipoDocumento != null ? " - Doc: " + tipoDocumento : "");
    }
}