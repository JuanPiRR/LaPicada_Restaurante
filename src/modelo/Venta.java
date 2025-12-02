package modelo;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Venta implements Serializable {

    private long id;
    private String fecha;
    private Cliente cliente;
    private List<DetalleVenta> detalles;
    private String metodoPago; // "EFECTIVO" o "TARJETA"
    private int total;         // suma de subtotales (sin propina)
    private int propina;       // >= 0
    private int valoracion;    // 0 = sin valoración, 1..5 válida
    private int vuelto;

    public Venta(long id, Cliente cliente, List<DetalleVenta> detalles,
                 String metodoPago, int total, int propina, int valoracion) {

        if (cliente == null) throw new IllegalArgumentException("Cliente nulo.");
        if (detalles == null || detalles.isEmpty()) throw new IllegalArgumentException("Detalles vacíos.");
        if (total < 0) throw new IllegalArgumentException("Total inválido.");
        if (propina < 0) throw new IllegalArgumentException("Propina inválida.");
        if (valoracion < 0 || valoracion > 5) throw new IllegalArgumentException("Valoración fuera de rango.");

        this.id = id;
        this.fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        this.cliente = cliente;
        this.detalles = detalles;
        this.metodoPago = metodoPago;
        this.total = total;
        this.propina = propina;
        this.valoracion = valoracion; // 0 si no hubo valoración
        this.vuelto = 0;
    }

    public void setVuelto(int vuelto) {
        if (vuelto < 0) throw new IllegalArgumentException("Vuelto inválido.");
        this.vuelto = vuelto;
    }

    public long getId() { return id; }
    public String getFecha() { return fecha; }
    public Cliente getCliente() { return cliente; }
    public List<DetalleVenta> getDetalles() { return detalles; }
    public String getMetodoPago() { return metodoPago; }
    public int getTotal() { return total; }
    public int getPropina() { return propina; }
    public int getValoracion() { return valoracion; }
    public int getVuelto() { return vuelto; }

    public int getTotalFinal() {
        return total + propina;
    }

    @Override
    public String toString() {
        return "Venta #" + id + " - " + fecha + " - Cliente: " + cliente.getNombre() + " - Total: $" + getTotalFinal();
    }
}
