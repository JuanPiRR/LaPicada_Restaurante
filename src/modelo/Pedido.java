package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Pedido implements Serializable {
    private String idPedido;
    private Date fechaHora;
    private String estado;         // Ej: "ABIERTO", "EN_PREPARACION", "PAGADO"
    private String observaciones;  // Notas adicionales

    // Relaciones del diagrama
    private Mesa mesa;
    private Garzon garzon;
    private Cliente cliente;
    private Pago pago;             // Puede ser null hasta que se pague

    // Lista de productos solicitados
    private List<DetallePedido> detalles;

    public Pedido(String idPedido, Date fechaHora, String estado, Mesa mesa, Garzon garzon, Cliente cliente) {
        this.idPedido = idPedido;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.mesa = mesa;
        this.garzon = garzon;
        this.cliente = cliente;
        this.detalles = new ArrayList<>();
        this.observaciones = "";
    }

    // Método Lógico: Agregar un plato a la lista de detalles
    public void agregarDetalle(Plato plato, int cantidad, String obs) {
        // Verificar si el plato ya está en el pedido para sumar cantidad y añadir observaciones
        for (DetallePedido d : detalles) {
            if (d.getPlato() != null && d.getPlato().getIdPlato().equals(plato.getIdPlato())) {
                d.setCantidad(d.getCantidad() + cantidad);
                d.setSubTotal(d.getCantidad() * plato.getPrecio());
                if (obs != null && !obs.trim().isEmpty()) {
                    String prev = d.getObservaciones() != null ? d.getObservaciones().toString() : "";
                    if (!prev.isEmpty()) prev = prev + " | ";
                    prev = prev + obs;
                    d.setObservaciones(prev);
                    // También mantener nota resumen en pedido
                    this.observaciones += "[" + plato.getNombre() + ": " + obs + "] ";
                }
                return;
            }
        }
        // Si no está, crear nuevo detalle y asignar observaciones
        DetallePedido nuevoDetalle = new DetallePedido(plato, cantidad);
        nuevoDetalle.setObservaciones(obs != null ? obs : "");
        this.detalles.add(nuevoDetalle);

        if (obs != null && !obs.trim().isEmpty()) {
            this.observaciones += "[" + plato.getNombre() + ": " + obs + "] ";
        }
    }

    // Método Lógico: Calcular el total $$$ del pedido
    public int calcularTotal() {
        int total = 0;
        for (DetallePedido det : detalles) {
            total += det.getSubTotal();
        }
        return total;
    }

    public Pago crearPago(String metodoPago, int propina) {
        int total = calcularTotal();
        String idPago = "PAG-" + System.currentTimeMillis();
        Pago pago = new Pago(idPago, total, metodoPago, propina, this);
        this.pago = pago;
        return pago;
    }

    public boolean procesarPago(int montoEntregado) throws Exception {
        if (pago == null) {
            throw new Exception("No hay pago asociado a este pedido");
        }

        // procesarPago en Pago devuelve void; invocarlo y luego comprobar el estado
        pago.procesarPago(montoEntregado);

        if (pago.isProcesado()) {
            this.estado = "PAGADO";
            // Liberar la mesa automáticamente
            if (mesa != null) {
                mesa.setEstado("DISPONIBLE");
            }
            return true;
        }

        return false;
    }

    // para verificar si el pedido está pagado
    public boolean isPagado() {
        return pago != null && pago.isProcesado();
    }

    // obtener el total con propina
    public int getTotalConPropina() {
        if (pago == null) {
            return calcularTotal();
        }
        return pago.getTotalAPagar();
    }

    // Getters y Setters
    public String getIdPedido() { return idPedido; }
    public List<DetallePedido> getDetalles() { return detalles; }
    public Mesa getMesa() { return mesa; }
    public Garzon getGarzon() { return garzon; }
    public Cliente getCliente() { return cliente; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Pago getPago() { return pago; }
    public void setPago(Pago pago) { this.pago = pago; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Date getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Date fechaHora) {
        this.fechaHora = fechaHora;
    }
}