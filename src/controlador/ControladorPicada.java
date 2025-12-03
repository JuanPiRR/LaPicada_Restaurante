// Controlador hecho por: Jesús Lema, Juan Recabal
package controlador;

import modelo.*; // Importar todas las clases del modelo
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ControladorPicada {
    private static final String ARCHIVO_DATOS = "datos_casino.bin";

    private List<Plato> carta;          // Reemplaza a 'productos'
    private List<Mesa> mesas;
    private List<Garzon> garzones;
    private List<Cliente> clientes;
    private List<Pedido> pedidos;       // Historial de pedidos

    private Pedido pedidoActual;        // Carrito en curso

    public ControladorPicada() {
        cargarDatosPersistentes();
        this.pedidoActual = null;
    }

    // GESTIÓN DE DATOS Y PERSISTENCIA
    @SuppressWarnings("unchecked")
    private void cargarDatosPersistentes() {
        File archivo = new File(ARCHIVO_DATOS);
        if (archivo.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
                carta = (List<Plato>) ois.readObject();
                mesas = (List<Mesa>) ois.readObject();
                garzones = (List<Garzon>) ois.readObject();
                clientes = (List<Cliente>) ois.readObject();
                pedidos = (List<Pedido>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                inicializarDatosPorDefecto();
            }
        } else {
            inicializarDatosPorDefecto();
        }
    }

    private void guardarDatosPersistentes() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_DATOS))) {
            oos.writeObject(carta);
            oos.writeObject(mesas);
            oos.writeObject(garzones);
            oos.writeObject(clientes);
            oos.writeObject(pedidos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void inicializarDatosPorDefecto() {
        carta = new ArrayList<>();
        mesas = new ArrayList<>();
        garzones = new ArrayList<>();
        clientes = new ArrayList<>();
        pedidos = new ArrayList<>();

        carta.add(new Plato("P001", "Paila Marina", 8500, "Mariscos", 20));
        carta.add(new Plato("P002", "Lomo a lo Pobre", 9000, "Carnes", 15));
        carta.add(new Plato("P003", "Reineta a la Plancha", 7500, "Pescados", 10));
        carta.add(new Plato("P004", "Pisco Sour", 3500, "Bebestible", 100));

        // Mesas
        mesas.add(new Mesa(1, 4, "DISPONIBLE"));
        mesas.add(new Mesa(2, 2, "DISPONIBLE"));
        mesas.add(new Mesa(3, 6, "DISPONIBLE"));

        // Garzones
        garzones.add(new Garzon("G01", "Juan Recabal", "Mañana"));
        garzones.add(new Garzon("G02", "Jesús Lema", "Tarde"));

        guardarDatosPersistentes();
    }

    // LÓGICA DEL NEGOCIO

    // 1. El Garzón asigna una mesa al Cliente.
    public void iniciarAtencion(int numeroMesa, String idGarzon, String runCliente, String nombreCliente) throws Exception {
        Mesa mesa = buscarMesa(numeroMesa);
        Garzon garzon = buscarGarzon(idGarzon);

        if (mesa == null || garzon == null) throw new Exception("Mesa o Garzón no encontrados.");
        if (!mesa.getEstado().equals("DISPONIBLE")) throw new Exception("La mesa está ocupada.");

        // Buscar o crear cliente
        Cliente cliente = buscarCliente(runCliente);
        if (cliente == null) {
            cliente = new Cliente(runCliente, nombreCliente, "General");
            clientes.add(cliente);
        }

        // Cambiar el estado de la mesa
        mesa.setEstado("OCUPADA");

        // Crear el encabezado del Pedido
        pedidoActual = new Pedido(generarIdPedido(), new Date(), "ABIERTO", mesa, garzon, cliente);
    }

    // 2. Agregar platos al pedido (es como tomar la orden)
    public void agregarPlatoAlPedido(String idPlato, int cantidad, String observaciones) throws Exception {
        if (pedidoActual == null) throw new Exception("No hay una atención iniciada.");

        Plato plato = buscarPlato(idPlato);
        if (plato == null) throw new Exception("Plato no existe.");

        // Validación de Stock
        if (plato.getDisponibilidad() < cantidad) {
            throw new Exception("Stock insuficiente para: " + plato.getNombre());
        }

        // Agregar detalle al pedido
        pedidoActual.agregarDetalle(plato, cantidad, observaciones);
    }

    // 3. Confirmar pedido y enviar a cocina
    public void confirmarPedidoYEnviarCocina() {
        if (pedidoActual != null) {
            // Descontar stock
            for (DetallePedido det : pedidoActual.getDetalles()) {
                det.getPlato().restarStock(det.getCantidad());
            }
            pedidoActual.setEstado("EN_PREPARACION");
            // Aquí se guardaría el estado temporal, pero aún no se finaliza la venta (pago)
            guardarDatosPersistentes();
        }
    }

    // 4. Finalizar Atención y Pagar (Garzón entrega boleta)
    public void finalizarYPagart(String metodoPago, int montoEntregado) throws Exception {
        if (pedidoActual == null) throw new Exception("No hay pedido activo.");

        int total = pedidoActual.calcularTotal(); // clase Pedido debe sumar los subtotales

        // Lógica de Pago
        Pago pago = new Pago(generarIdPago(), total, metodoPago, new Date());

        if (metodoPago.equalsIgnoreCase("EFECTIVO")) {
            if (montoEntregado < total) throw new Exception("Dinero insuficiente.");
            // Vuelto en caso de necesitar usarlo.
            int vuelto = montoEntregado - total;
        }

        // Asignar pago al pedido y cerrar
        pedidoActual.setPago(pago);
        pedidoActual.setEstado("PAGADO");
        pedidoActual.getMesa().setEstado("DISPONIBLE"); // Liberar mesa

        // Guardar en historial histórico
        pedidos.add(pedidoActual);

        // Limpiar actual
        pedidoActual = null;
        guardarDatosPersistentes();
    }

    // MÉTODOS AUXILIARES DE BÚSQUEDA (Getters)

    public List<Plato> getCarta() { return carta; }

    public List<Plato> buscarPlatos(String filtro) {
        return carta.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(filtro.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Mesa> getMesasDisponibles() {
        return mesas.stream().filter(m -> m.getEstado().equals("DISPONIBLE")).collect(Collectors.toList());
    }

    public List<Garzon> getGarzones() { return garzones; }

    private Plato buscarPlato(String id) {
        return carta.stream().filter(p -> p.getIdPlato().equals(id)).findFirst().orElse(null);
    }

    private Mesa buscarMesa(int numero) {
        return mesas.stream().filter(m -> m.getNumero() == numero).findFirst().orElse(null);
    }

    private Garzon buscarGarzon(String id) {
        return garzones.stream().filter(g -> g.getIdGarzon().equals(id)).findFirst().orElse(null);
    }

    private Cliente buscarCliente(String run) {
        return clientes.stream().filter(c -> c.getRun().equals(run)).findFirst().orElse(null);
    }

    // Generadores de ID
    private String generarIdPedido() { return "PED-" + (pedidos.size() + 1); }
    private String generarIdPago() { return "PAG-" + System.currentTimeMillis(); }
}