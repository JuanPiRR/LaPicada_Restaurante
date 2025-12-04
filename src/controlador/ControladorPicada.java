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
    private List<Insumo> insumos;
    private List<Proveedor> proveedores;
    private List<OrdenCompra> ordenesCompra;
    private List<Transportista> transportistas;
    private List<Recepcion> recepciones;
    private List<PagoProveedor> pagosProveedores;
    private List<Plato> carta;          // Reemplaza a 'productos'
    private List<Mesa> mesas;
    private List<Garzon> garzones;
    private List<Cliente> clientes;
    private List<Pedido> pedidos;       // Historial de pedidos
    private List<Valoracion> valoracion;

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

                //Nueva lista (inventario)
                insumos = (List<Insumo>) ois.readObject();
                proveedores = (List<Proveedor>) ois.readObject();
                ordenesCompra = (List<OrdenCompra>) ois.readObject();
                transportistas = (List<Transportista>) ois.readObject();
                recepciones = (List<Recepcion>) ois.readObject();
                pagosProveedores = (List<PagoProveedor>) ois.readObject();

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

            //Nueva lista (inventario)
            oos.writeObject(insumos);
            oos.writeObject(proveedores);
            oos.writeObject(ordenesCompra);
            oos.writeObject(transportistas);
            oos.writeObject(recepciones);
            oos.writeObject(pagosProveedores);

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

        //a partir de aqui son relacionado a inventario:
        insumos = new ArrayList<>();
        proveedores = new ArrayList<>();
        ordenesCompra = new ArrayList<>();
        transportistas = new ArrayList<>();
        recepciones = new ArrayList<>();
        pagosProveedores = new ArrayList<>();

        insumos.add(new Insumo("I001", "Carne de Res", "Carnes", "kg", 10, 5, 8500));
        insumos.add(new Insumo("I002", "Pollo", "Carnes", "kg", 8, 3, 4500));
        insumos.add(new Insumo("I003", "Arroz", "Granos", "kg", 20, 10, 1200));
        insumos.add(new Insumo("I004", "Tomate", "Verduras", "kg", 5, 2, 1500));

        proveedores.add(new Proveedor("P001", "Carnes Don Lema", "912345678", "carnes@donlema.cl", "Carnes"));
        proveedores.add(new Proveedor("P002", "Verduras Frescas S.A.", "922334455", "ventas@verdurasfrescas.cl", "Verduras"));

        transportistas.add(new Transportista("T001", "Jose Recabal", "Transportes Rapido", "AB123CD", "933445566"));
        guardarDatosPersistentes();

    }

    // LÓGICA DEL NEGOCIO

    // 1. El Garzón asigna una mesa al Cliente.
    public void iniciarAtencion(int numeroMesa, String idGarzon, String rutCliente, String nombreCliente) throws Exception {
        Mesa mesa = buscarMesa(numeroMesa);
        Garzon garzon = buscarGarzon(idGarzon);

        if (mesa == null || garzon == null) throw new Exception("Mesa o Garzón no encontrados.");
        if (!mesa.getEstado().equals("DISPONIBLE")) throw new Exception("La mesa está ocupada.");

        // Buscar o crear cliente
        Cliente cliente = buscarCliente(rutCliente);
        if (cliente == null) {
            cliente = new Cliente(rutCliente, nombreCliente);
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
        Pago pago = new Pago(generarIdPago(), total, metodoPago, 0);

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
    //Metodos para gestion de Insumos
    //Gestion de Insumos
    public void agregarInsumos(String id, String nombre, String categoria, String unidadMedida, int stockMinimo, int stockActual, double precioUnitario){
        Insumo nuevoInsumo = new Insumo(id,nombre,categoria,unidadMedida,stockMinimo,stockActual,precioUnitario);
        insumos.add(nuevoInsumo);
        guardarDatosPersistentes();
    }

    public List<Insumo> getInsumos(){return insumos;}

    public List<Insumo> getInsumosConBajoStock(){
        return insumos.stream().filter(Insumo :: necesitaReposicion).collect(Collectors.toList());
    }

    //Gestion de Proveedores
    public void agregarProveedor(String id, String nombre, String telefono, String email, String tipoProducto){
        Proveedor nuevoProveedor = new Proveedor(id,nombre,telefono,email,tipoProducto);
        proveedores.add(nuevoProveedor);
        guardarDatosPersistentes();
    }

    public List<Proveedor> getProveedores(){return proveedores;}

    //Crear orden de compra
    public OrdenCompra crearOrdenCompra(String idProveedor)throws Exception{
        Proveedor proveedor = buscarProveedor(idProveedor);
        if (proveedor == null) throw new Exception("Proveedor no encontrado.");
        String idOrden = "OC-" + (ordenesCompra.size() + 1);
        OrdenCompra nuevaOrden = new OrdenCompra(idOrden, proveedor);
        ordenesCompra.add(nuevaOrden);
        return nuevaOrden;
    }

    public void agregarInsumosAOrden(String idOrden, String idInsumo, int cantidad, double precioUnitario)throws Exception{
        OrdenCompra orden = buscarOrdenCompra(idOrden);
        Insumo insumo = buscarInsumo(idInsumo);

        if (orden == null) throw new Exception("Orden no encontrado.");
        if (insumo == null) throw new Exception("Insumo no encontrado.");
        orden.agregarDetalle(insumo, cantidad, precioUnitario);
        guardarDatosPersistentes();
    }

    public void enviarOrdenCompra(String idOrden)throws Exception{
        OrdenCompra orden = buscarOrdenCompra(idOrden);
        if (orden == null) throw new Exception("Orden no encontrado.");

        if (orden.getDetalles().isEmpty()) throw new Exception("No se puede enviar una orden sin detalles");
        orden.cambiarEstado("ENVIADA");
        guardarDatosPersistentes();
    }

    //Recepcion de mercancia
    public Recepcion registrarRecepcion(String idOrden, String idTransportista, String estado, String observaciones)throws Exception{
        OrdenCompra orden = buscarOrdenCompra(idOrden);
        Transportista transportista = buscarTransportista(idTransportista);

        if(orden == null) throw new Exception("Orden no encontrado.");
        if (transportista == null) throw new Exception("Transportista no encontrado.");

        if(!"ENVIADA".equals(orden.getEstado())) throw new Exception("La orden debe estar ENVIADA para recibirla.");

        String idRecepcion = "REC-" + (recepciones.size() + 1);
        Recepcion nuevaRecepcion = new Recepcion(idRecepcion,orden,transportista);
        nuevaRecepcion.setEstado(estado);
        nuevaRecepcion.setObservaciones(observaciones);

        //actualizar Stock
        nuevaRecepcion.procesarRecepcion();
        orden.setRecepcion(nuevaRecepcion);
        recepciones.add(nuevaRecepcion);
        guardarDatosPersistentes();
        return nuevaRecepcion;

    }
   //Gestion de pagos
    public PagoProveedor registrarPagoProveedor(String idOrden, double monto, String metodoPago) throws Exception{
        OrdenCompra orden = buscarOrdenCompra(idOrden);
        if(orden == null) throw new Exception("Orden no encontrado.");
        if(!"RECIBIDA".equals(orden.getEstado())) throw new Exception("La orden debe estar RECIBIDA para pagarla.");
        String idPago = "PAG-PROV" + (pagosProveedores.size() + 1);
        PagoProveedor nuevoPago = new PagoProveedor(idPago,monto,metodoPago, orden);

        nuevoPago.procesarPago();
        orden.setPago(nuevoPago);
        pagosProveedores.add(nuevoPago);
        guardarDatosPersistentes();
        return nuevoPago;
    }

    //Transportista
    public void agregarTransportista(String id, String nombre, String empresa, String patente, String telefono){
        Transportista nuevoTransportista = new Transportista(id,nombre,empresa,patente,telefono);
        transportistas.add(nuevoTransportista);
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

    private Cliente buscarCliente(String rut) {
        return clientes.stream().filter(c -> c.getRut().equals(rut)).findFirst().orElse(null);
    }

    //Auxiliares del inventario
    private Insumo buscarInsumo(String id) {
        return insumos.stream().filter(i -> i.getIdInsumo().equals(id)).findFirst().orElse(null);
    }

    private Proveedor buscarProveedor(String id) {
        return proveedores.stream().filter(p -> p.getIdProveedor().equals(id)).findFirst().orElse(null);
    }

    private OrdenCompra buscarOrdenCompra(String id) {
        return ordenesCompra.stream().filter(o -> o.getIdOrden().equals(id)).findFirst().orElse(null);
    }

    private Transportista buscarTransportista(String id) {
        return transportistas.stream().filter(o -> o.getIdTransportista().equals(id)).findFirst().orElse(null);
    }

    //Metodos de consulta (inventario)
    public List<OrdenCompra> getOrdenesPorEstado(String estado){
        return  ordenesCompra.stream().filter(o -> o.getEstado().equals(estado)).collect(Collectors.toList());
    }
    public List<OrdenCompra> getOrdenesPorProveedor(String idProveedor){
        return ordenesCompra.stream().filter(o->o.getProveedor().getIdProveedor().equals(idProveedor)).collect(Collectors.toList());
    }
    public List<Recepcion> getRecepciones(){
        return recepciones;
    }
    public List<PagoProveedor> getPagosProveedor() {
        return pagosProveedores;
    }
    public List<Transportista> getTransportistas() {
        return transportistas;
    }
    // Generadores de ID
    private String generarIdPedido() { return "PED-" + (pedidos.size() + 1); }
    private String generarIdPago() { return "PAG-" + System.currentTimeMillis(); }
}