// Controlador hecho por: Jesús Lema, Juan Recabal
package controlador;

import modelo.*; // Importar todas las clases del modelo
import vista.VistaPrincipal;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ControladorPicada {
    private static final String ARCHIVO_DATOS = "datos_casino.bin";
    private static ControladorPicada instance; // singleton
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

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    public static final String PROP_DATOS_PERSISTIDOS = "datosPersistidos";

    public ControladorPicada() {
        cargarDatosPersistentes();
        this.pedidoActual = null;
    }

    public static synchronized ControladorPicada getInstance() {
        if (instance == null) {
            instance = new ControladorPicada();
        }
        return instance;
    }

    // Permitir que los paneles se suscriban
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
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

            // Notificar a listeners que los datos cambiaron / fueron persistidos
            pcs.firePropertyChange(PROP_DATOS_PERSISTIDOS, false, true);

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
        // Aceptar tanto DISPONIBLE como RESERVADA como válidas para iniciar atención
        if (!"DISPONIBLE".equalsIgnoreCase(mesa.getEstado()) && !"RESERVADA".equalsIgnoreCase(mesa.getEstado()))
            throw new Exception("La mesa no está disponible.");

        Cliente cliente = null;
        if (rutCliente != null && !rutCliente.trim().isEmpty()) {
            cliente = buscarCliente(rutCliente);
            if (cliente == null) {
                cliente = new Cliente(rutCliente, nombreCliente != null ? nombreCliente : "");
                clientes.add(cliente);
            }
        }

        mesa.setEstado("OCUPADA");
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

    // 3. Confirmar pedido y mover a historial (no descontar stock ni poner EN_PREPARACION)
    public void confirmarPedidoYEnviarCocina() throws Exception {
        if (pedidoActual == null) throw new Exception("No hay pedido activo para confirmar.");
        // Validar stock antes de confirmar
        for (DetallePedido det : pedidoActual.getDetalles()) {
            Plato plato = det.getPlato();
            if (plato == null) continue;
            if (plato.getDisponibilidad() < det.getCantidad()) {
                throw new Exception("Stock insuficiente para: " + plato.getNombre());
            }
        }

        // Descontar stock en el momento de confirmar el pedido (ahora)
        for (DetallePedido det : pedidoActual.getDetalles()) {
            Plato plato = det.getPlato();
            if (plato != null) {
                plato.restarStock(det.getCantidad());
            }
        }

        // Marcar estado ABIERTO y mover a historial
        pedidoActual.setEstado("ABIERTO");
        pedidos.add(pedidoActual);

        // Persistir y limpiar pedidoActual
        guardarDatosPersistentes();
        pedidoActual = null;
    }
    // 4. Finalizar Atención y Pagar (Garzón entrega boleta)
    public void finalizarYPagart(String metodoPago, int montoEntregado) throws Exception {
        if (pedidoActual == null) throw new Exception("No hay pedido activo.");

        int total = pedidoActual.calcularTotal(); // clase Pedido debe sumar los subtotales

        // Lógica de Pago
        Pago pago = new Pago(generarIdPago(), total, metodoPago,0,pedidoActual);

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
    public void agregarInsumos(String id, String nombre, String categoria, String unidadMedida, int stockMinimo, int stockActual, double precioUnitario) {
        Insumo nuevoInsumo = new Insumo(id, nombre, categoria, unidadMedida, stockMinimo, stockActual, precioUnitario);
        insumos.add(nuevoInsumo);
        guardarDatosPersistentes();
    }

    public List<Insumo> getInsumos() {
        return insumos;
    }

    public List<Insumo> getInsumosConBajoStock() {
        return insumos.stream().filter(Insumo::necesitaReposicion).collect(Collectors.toList());
    }

    //Gestion de Proveedores
    public void agregarProveedor(String id, String nombre, String telefono, String email, String tipoProducto) {
        Proveedor nuevoProveedor = new Proveedor(id, nombre, telefono, email, tipoProducto);
        proveedores.add(nuevoProveedor);
        guardarDatosPersistentes();
    }

    public List<Proveedor> getProveedores() {
        return proveedores;
    }

    //Crear orden de compra
    public OrdenCompra crearOrdenCompra(String idProveedor) throws Exception {
        Proveedor proveedor = buscarProveedor(idProveedor);
        if (proveedor == null) throw new Exception("Proveedor no encontrado.");
        String idOrden = "OC-" + (ordenesCompra.size() + 1);
        OrdenCompra nuevaOrden = new OrdenCompra(idOrden, proveedor);
        ordenesCompra.add(nuevaOrden);
        return nuevaOrden;
    }

    public void agregarInsumosAOrden(String idOrden, String idInsumo, int cantidad, double precioUnitario) throws Exception {
        OrdenCompra orden = buscarOrdenCompra(idOrden);
        Insumo insumo = buscarInsumo(idInsumo);

        if (orden == null) throw new Exception("Orden no encontrado.");
        if (insumo == null) throw new Exception("Insumo no encontrado.");
        orden.agregarDetalle(insumo, cantidad, precioUnitario);
        guardarDatosPersistentes();
    }


    public void enviarOrdenCompra(String idOrden) throws Exception {
        OrdenCompra orden = buscarOrdenCompra(idOrden);
        if (orden == null) throw new Exception("Orden no encontrado.");

        if (orden.getDetalles().isEmpty()) throw new Exception("No se puede enviar una orden sin detalles");
        orden.cambiarEstado("ENVIADA");
        guardarDatosPersistentes();
    }

    //Recepcion de mercancia
    public Recepcion registrarRecepcion(String idOrden, String idTransportista, String observaciones) throws Exception {
        OrdenCompra orden = buscarOrdenCompra(idOrden);
        Transportista transportista = buscarTransportista(idTransportista);

        if (orden == null) throw new Exception("Orden no encontrado.");
        if (idTransportista != null && transportista == null) throw new Exception("Transportista no encontrado."); // Transportista puede ser opcional

        // Verificación de estado de la OC
        if (!"ENVIADA".equals(orden.getEstado())) throw new Exception("La orden debe estar ENVIADA para recibirla.");

        String idRecepcion = "REC-" + (recepciones.size() + 1);
        Recepcion nuevaRecepcion = new Recepcion(idRecepcion, orden, transportista);

        nuevaRecepcion.setEstado("PENDIENTE");
        nuevaRecepcion.setObservaciones(observaciones);

        orden.setRecepcion(nuevaRecepcion);
        recepciones.add(nuevaRecepcion);
        guardarDatosPersistentes();

        orden.cambiarEstado("EN_RECEPCION");

        return nuevaRecepcion;
    }

    //Gestion de pagos
    public PagoProveedor registrarPagoProveedor(String idOrden, double monto, String metodoPago) throws Exception {
        OrdenCompra orden = buscarOrdenCompra(idOrden);
        if (orden == null) throw new Exception("Orden no encontrado.");
        if (!"RECIBIDA".equals(orden.getEstado())) throw new Exception("La orden debe estar RECIBIDA para pagarla.");
        String idPago = "PAG-PROV" + (pagosProveedores.size() + 1);
        PagoProveedor nuevoPago = new PagoProveedor(idPago, monto, metodoPago, orden);

        nuevoPago.procesarPago();
        orden.setPago(nuevoPago);
        pagosProveedores.add(nuevoPago);
        guardarDatosPersistentes();
        return nuevoPago;
    }

    //Transportista
    public void agregarTransportista(String id, String nombre, String empresa, String patente, String telefono) {
        Transportista nuevoTransportista = new Transportista(id, nombre, empresa, patente, telefono);
        transportistas.add(nuevoTransportista);
        guardarDatosPersistentes();
    }
    // MÉTODOS AUXILIARES DE BÚSQUEDA (Getters)

    public List<Plato> getCarta() {
        return carta;
    }

    public List<Plato> buscarPlatos(String filtro) {
        return carta.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(filtro.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Mesa> getMesasDisponibles() {
        // Considerar RESERVADA igual que DISPONIBLE para listados de mesas "disponibles"
        return mesas.stream()
                .filter(m -> "DISPONIBLE".equalsIgnoreCase(m.getEstado()) || "RESERVADA".equalsIgnoreCase(m.getEstado()))
                .collect(Collectors.toList());
    }

    public List<Garzon> getGarzones() {
        return garzones;
    }

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
    public List<OrdenCompra> getOrdenesPorEstado(String estado) {
        return ordenesCompra.stream().filter(o -> o.getEstado().equals(estado)).collect(Collectors.toList());
    }

    public List<OrdenCompra> getOrdenesPorProveedor(String idProveedor) {
        return ordenesCompra.stream().filter(o -> o.getProveedor().getIdProveedor().equals(idProveedor)).collect(Collectors.toList());
    }

    public List<Recepcion> getRecepciones() {
        return recepciones;
    }

    public List<PagoProveedor> getPagosProveedor() {
        return pagosProveedores;
    }

    public List<Transportista> getTransportistas() {
        return transportistas;
    }

    // Generadores de ID
    private String generarIdPedido() {
        return "PED-" + (pedidos.size() + 1);
    }

    private String generarIdPago() {
        return "PAG-" + System.currentTimeMillis();
    }

    // NUEVO MÉTODO: Obtener categorías únicas para el JComboBox
    public List<String> obtenerCategoriasUnicas() {
        return carta.stream()
                .map(Plato::getTipo)
                .distinct() // Garantiza que cada categoría aparezca solo una vez
                .collect(Collectors.toList());
    }

    public List<String> obtenerCategoriasInsumos() {
        if (insumos == null) return java.util.Collections.emptyList();
        return insumos.stream()
                .map(Insumo::getCategoria)
                .filter(c -> c != null && !c.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    // NUEVO MÉTODO: Obtener Platos por el nombre de la categoría (String)
    public List<Plato> obtenerPlatosPorCategoria(String nombreCategoria) {
        return carta.stream()
                .filter(p -> p.getTipo().equalsIgnoreCase(nombreCategoria))
                .collect(Collectors.toList());
    }

    public List<DetallePedido> getDetallesPedidoActual() {
        if (pedidoActual == null) return new ArrayList<>();
        return pedidoActual.getDetalles();
    }

    public int getTotalPedidoActual() {
        if (pedidoActual == null) return 0;
        return pedidoActual.calcularTotal();
    }

    public void eliminarDetalleDelPedidoActual(String idPlato) throws Exception {
        if (pedidoActual == null) throw new Exception("No hay una atención iniciada.");
        if (pedidoActual.getDetalles() == null || pedidoActual.getDetalles().isEmpty())
            throw new Exception("Pedido vacío.");

        DetallePedido detalle = pedidoActual.getDetalles().stream()
                .filter(d -> d.getPlato() != null && idPlato.equals(d.getPlato().getIdPlato()))
                .findFirst()
                .orElse(null);

        if (detalle == null) throw new Exception("Detalle no encontrado en el pedido.");
        pedidoActual.getDetalles().remove(detalle);
        guardarDatosPersistentes();
    }
    public void agregarPlato(String idPlato, String nombre, int precio, String tipo, int disponibilidad) throws Exception {
        if (idPlato == null || idPlato.trim().isEmpty()) throw new Exception("ID vacío");
        if (buscarPlato(idPlato) != null) throw new Exception("ID de plato ya existe.");
        Plato nuevo = new Plato(idPlato, nombre, precio, tipo, disponibilidad);
        carta.add(nuevo);
        guardarDatosPersistentes();
    }
    public void actualizarPlato(String idPlato, String nombre, int precio, String tipo, int disponibilidad) throws Exception {
        if (idPlato == null || idPlato.trim().isEmpty()) throw new Exception("ID vacío");
        Plato p = buscarPlato(idPlato);
        if (p == null) throw new Exception("Plato no encontrado.");
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setTipo(tipo);
        p.setDisponibilidad(disponibilidad);
        guardarDatosPersistentes();
    }

    public void eliminarPlato(String id) {
        Plato p = buscarPlato(id);
        if (p != null) {
            carta.remove(p);
            guardarDatosPersistentes();
        }
    }

    // MÉTODOS PARA GESTIÓN DE PAGOS

    public Pago crearPagoParaPedidoActual(String metodoPago, int propina) throws Exception {
        if (pedidoActual == null) {
            throw new Exception("No hay un pedido activo");
        }

        if (pedidoActual.getEstado().equals("PAGADO")) {
            throw new Exception("El pedido ya está pagado");
        }

        Pago pago = pedidoActual.crearPago(metodoPago, propina);
        guardarDatosPersistentes();
        return pago;
    }

     //Procesa el pago del pedido actual

    public boolean procesarPagoPedidoActual(int montoEntregado) throws Exception {
        if (pedidoActual == null) {
            throw new Exception("No hay un pedido activo");
        }

        boolean resultado = pedidoActual.procesarPago(montoEntregado);
        if (resultado) {
            // Agregar a historial
            pedidos.add(pedidoActual);

            // Limpiar pedido actual
            pedidoActual = null;

            guardarDatosPersistentes();
        }

        return resultado;
    }

    //Obtiene información del pago actual

    public String getInfoPagoActual() {
        if (pedidoActual == null || pedidoActual.getPago() == null) {
            return "No hay pago pendiente";
        }

        Pago pago = pedidoActual.getPago();
        return String.format("Total: $%d | Propina: $%d | Método: %s",
                pago.getMonto(), pago.getPropina(), pago.getMetodoPago());
    }

    //Aplica propina porcentual al pago actual

    public void aplicarPropinaPorcentual(int porcentaje) throws Exception {
        if (pedidoActual == null || pedidoActual.getPago() == null) {
            throw new Exception("No hay pago para aplicar propina");
        }

        pedidoActual.getPago().aplicarPropinaPorcentual(porcentaje);
    }


    //Obtiene el vuelto si el pago fue en efectivo

    public int getVueltoPagoActual() {
        if (pedidoActual == null || pedidoActual.getPago() == null) {
            return 0;
        }

        return pedidoActual.getPago().getVuelto();
    }


     //Verifica si el pedido actual está pagado

    public boolean isPedidoActualPagado() {
        return pedidoActual != null && pedidoActual.isPagado();
    }


    //Obtiene el total a pagar del pedido actual

    public int getTotalAPagarActual() {
        if (pedidoActual == null) {
            return 0;
        }

        return pedidoActual.getTotalConPropina();
    }

// MÉTODOS PARA HISTORIAL DE PAGOS

    // obtiene todos los pagos procesados
    public List<Pago> getHistorialPagos() {
        List<Pago> pagos = new ArrayList<>();

        for (Pedido pedido : pedidos) {
            if (pedido.getPago() != null && pedido.getPago().isProcesado()) {
                pagos.add(pedido.getPago());
            }
        }

        return pagos;
    }


     //Obtiene el total recaudado por METODO de pago

    public Map<String, Integer> getTotalPorMetodoPago() {
        Map<String, Integer> totales = new HashMap<>();
        totales.put("EFECTIVO", 0);
        totales.put("TARJETA", 0);
        totales.put("TRANSFERENCIA", 0);

        for (Pago pago : getHistorialPagos()) {
            String metodo = pago.getMetodoPago().toUpperCase();
            int total = totales.getOrDefault(metodo, 0);
            totales.put(metodo, total + pago.getTotalPagado());
        }

        return totales;
    }


    //Obtiene el total de propinas recaudadas

    public int getTotalPropinas() {
        return getHistorialPagos().stream()
                .mapToInt(Pago::getPropina)
                .sum();
    }


    public void actualizarTablaInsumos(JTable tablaInsumos) {
        String[] columnas = {"ID", "Nombre", "Categoría", "Unidad Medida", "Stock Mínimo", "Stock Actual", "Precio Unitario"};
        Object[][] datos = new Object[insumos.size()][7];

        for (int i = 0; i < insumos.size(); i++) {
            Insumo insumo = insumos.get(i);
            datos[i][0] = insumo.getIdInsumo();
            datos[i][1] = insumo.getNombre();
            datos[i][2] = insumo.getCategoria();
            datos[i][3] = insumo.getUnidadMedida();
            datos[i][4] = insumo.getStockMinimo();
            datos[i][5] = insumo.getStockActual();
            datos[i][6] = insumo.getPrecioUnitario();
        }

        tablaInsumos.setModel(new javax.swing.table.DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Todas las celdas no son editables
            }
        });
    }

    public void filtrarInsumos(JTable tablaInsumos, String estado) {
        List<Insumo> insumosFiltrados;
        if (estado.equals("Todos")) {
            insumosFiltrados = insumos;
        } else if (estado.equals("Bajo Stock")) {
            insumosFiltrados = insumos.stream().filter(Insumo::necesitaReposicion).collect(Collectors.toList());
        } else {
            insumosFiltrados = new ArrayList<>();
        }

        String[] columnas = {"ID", "Nombre", "Categoría", "Unidad Medida", "Stock Mínimo", "Stock Actual", "Precio Unitario"};
        Object[][] datos = new Object[insumosFiltrados.size()][7];
        for (int i = 0; i < insumosFiltrados.size(); i++) {
            Insumo insumo = insumosFiltrados.get(i);
            datos[i][0] = insumo.getIdInsumo();
            datos[i][1] = insumo.getNombre();
            datos[i][2] = insumo.getCategoria();
            datos[i][3] = insumo.getUnidadMedida();
            datos[i][4] = insumo.getStockMinimo();
            datos[i][5] = insumo.getStockActual();
            datos[i][6] = insumo.getPrecioUnitario();
        }

        tablaInsumos.setModel(new javax.swing.table.DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Todas las celdas no son editables
            }
        });

    }
    public void actualizarInsumo(String id, String nombre, String categoria, String unidadMedida, int stockMinimo, int stockActual) throws Exception {
        Insumo ins = buscarInsumo(id);
        if (ins == null) throw new Exception("Insumo no encontrado.");
        ins.setNombre(nombre);
        ins.setCategoria(categoria);
        ins.setUnidadMedida(unidadMedida);
        ins.setStockMinimo(stockMinimo);
        ins.setStockActual(stockActual); // el stock actual editado es el que manda
        guardarDatosPersistentes();
    }
    public void eliminarInsumo(String id) {
        Insumo ins = buscarInsumo(id);
        if (ins == null) return; // ya no existe, nada que hacer

        // Eliminar insumo del inventario
        insumos.remove(ins);

        // Quitar cualquier detalle de órdenes de compra que referencien este insumo
        if (ordenesCompra != null) {
            for (OrdenCompra oc : ordenesCompra) {
                if (oc.getDetalles() != null) {
                    oc.getDetalles().removeIf(d -> d.getInsumo() != null && id.equals(d.getInsumo().getIdInsumo()));
                }
            }
        }
    }
    public void procesarRecepcion(String idRecepcion, String estadoFinal) throws Exception {
        if (recepciones == null) throw new Exception("No hay recepciones registradas.");
        Recepcion r = recepciones.stream()
                .filter(x -> x.getIdRecepcion().equals(idRecepcion))
                .findFirst()
                .orElse(null);
        if (r == null) throw new Exception("Recepción no encontrada.");

        if (!"PENDIENTE".equalsIgnoreCase(r.getEstado()) && !"INCOMPLETA".equalsIgnoreCase(r.getEstado())) {
            throw new Exception("Solo se pueden procesar recepciones en estado PENDIENTE o INCOMPLETA.");
        }

        OrdenCompra oc = r.getOrdenCompra();
        if (oc == null) throw new Exception("Orden asociada a la recepción no encontrada.");
        if (oc.getDetalles() != null) {
            for (DetalleOrdenCompra d : oc.getDetalles()) {
                int cantidadRecibida = d.getCantidadRecibida(); // Usar el campo separado
                if (cantidadRecibida <= 0) continue;

                Insumo ref = d.getInsumo();
                Insumo ins = null;
                if (ref != null) ins = buscarInsumo(ref.getIdInsumo());

                if (ins == null) {
                    String id = ref != null && ref.getIdInsumo() != null ? ref.getIdInsumo() : "I-" + System.currentTimeMillis();
                    String nombre = ref != null ? ref.getNombre() : "";
                    String categoria = ref != null ? ref.getCategoria() : "";
                    String unidad = ref != null ? ref.getUnidadMedida() : "";
                    ins = new Insumo(id, nombre, categoria, unidad, 0, 0, d.getPrecioUnitario());
                    insumos.add(ins);
                }

                // Sumar solo la cantidad RECIBIDA al inventario
                ins.agregarStock(cantidadRecibida);
                if (d.getPrecioUnitario() >= 0) ins.setPrecioUnitario(d.getPrecioUnitario());
            }
        }

        r.setEstado(estadoFinal);
        oc.cambiarEstado("RECIBIDA");

        guardarDatosPersistentes();
    }

    public void cargarProveedoresEnTabla(JTable tablaProveedores) {
        if (tablaProveedores == null) return;

        String[] columnas = {"ID", "Nombre", "Teléfono", "Email", "Tipo Producto"};
        List<Proveedor> lista = this.proveedores != null ? this.proveedores : new ArrayList<>();

        Object[][] datos = new Object[lista.size()][columnas.length];
        for (int i = 0; i < lista.size(); i++) {
            Proveedor p = lista.get(i);
            datos[i][0] = p.getIdProveedor();
            datos[i][1] = p.getNombre();
            datos[i][2] = p.getTelefono();
            datos[i][3] = p.getEmail();
            datos[i][4] = p.getTipoProducto();
        }

        tablaProveedores.setModel(new javax.swing.table.DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }
    public void actualizarProveedor(String idProveedor, String nombre, String telefono, String email, String tipoProducto) throws Exception {
        Proveedor p = buscarProveedor(idProveedor);
        if (p == null) throw new Exception("Proveedor no encontrado.");
        p.setNombre(nombre);
        p.setTelefono(telefono);
        p.setEmail(email);
        p.setTipoProducto(tipoProducto);
        guardarDatosPersistentes();
    }

    public void eliminarProveedor(String idProveedor) throws Exception {
        Proveedor p = buscarProveedor(idProveedor);
        if (p == null) throw new Exception("Proveedor no encontrado.");
        proveedores.remove(p);
        guardarDatosPersistentes();
    }

    public void cargarTablaCarta(JTable tablaCarta) {
        if (tablaCarta == null) return;

        String[] columnas = {"ID Plato", "Nombre", "Precio", "Tipo", "Disponibilidad"};
        List<Plato> lista = this.carta != null ? this.carta : new ArrayList<>();

        Object[][] datos = new Object[lista.size()][columnas.length];
        for (int i = 0; i < lista.size(); i++) {
            Plato p = lista.get(i);
            datos[i][0] = p.getIdPlato();
            datos[i][1] = p.getNombre();
            datos[i][2] = p.getPrecio();
            datos[i][3] = p.getTipo();
            datos[i][4] = p.getDisponibilidad();
        }

        tablaCarta.setModel(new javax.swing.table.DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }
    public void cargarTablaPedidoActual(JTable tablaPedidoActual) {
        if (tablaPedidoActual == null) return;

        String[] columnas = {"ID Plato", "Nombre", "Cantidad", "Observaciones", "Subtotal"};
        List<DetallePedido> lista = getDetallesPedidoActual();

        Object[][] datos = new Object[lista.size()][columnas.length];
        for (int i = 0; i < lista.size(); i++) {
            DetallePedido d = lista.get(i);
            Plato p = d.getPlato();
            datos[i][0] = p != null ? p.getIdPlato() : "";
            datos[i][1] = p != null ? p.getNombre() : "";
            datos[i][2] = d.getCantidad();
            datos[i][3] = d.getObservaciones();
            datos[i][4] = d.getSubTotal();
        }

        tablaPedidoActual.setModel(new javax.swing.table.DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    public void cargarTablaPedidosConfirmados(JTable tablaPedidosConfirmados) {
        if (tablaPedidosConfirmados == null) return;

        String[] columnas = {"ID Pedido", "Fecha", "Estado", "Mesa", "Garzón", "Cliente", "Total"};
        List<Pedido> lista = this.pedidos != null ? this.pedidos : new ArrayList<>();

        Object[][] datos = new Object[lista.size()][columnas.length];
        for (int i = 0; i < lista.size(); i++) {
            Pedido ped = lista.get(i);
            datos[i][0] = ped.getIdPedido();
            datos[i][1] = ped.getFechaHora();
            datos[i][2] = ped.getEstado();
            datos[i][3] = ped.getMesa() != null ? ped.getMesa().getNumero() : "";
            datos[i][4] = ped.getGarzon() != null ? ped.getGarzon().getNombre() : "";
            datos[i][5] = ped.getCliente() != null ? ped.getCliente().getNombre() : "";
            datos[i][6] = ped.calcularTotal();
        }

        tablaPedidosConfirmados.setModel(new javax.swing.table.DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    public void cargarTablaDetallePedidoConfirmado(JTable tablaDetallePedidoConfirmado, String idPedido) {
        if (tablaDetallePedidoConfirmado == null) return;

        Pedido pedido = pedidos.stream()
                .filter(p -> p.getIdPedido().equals(idPedido))
                .findFirst()
                .orElse(null);

        if (pedido == null) return;

        String[] columnas = {"ID Plato", "Nombre", "Cantidad", "Observaciones", "Subtotal"};
        List<DetallePedido> lista = pedido.getDetalles();

        Object[][] datos = new Object[lista.size()][columnas.length];
        for (int i = 0; i < lista.size(); i++) {
            DetallePedido d = lista.get(i);
            Plato p = d.getPlato();
            datos[i][0] = p != null ? p.getIdPlato() : "";
            datos[i][1] = p != null ? p.getNombre() : "";
            datos[i][2] = d.getCantidad();
            datos[i][3] = d.getObservaciones();
            datos[i][4] = d.getSubTotal();
        }

        tablaDetallePedidoConfirmado.setModel(new javax.swing.table.DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    // Iniciar preparación de un pedido del historial: ahora valida stock y descuenta disponibilidad
    public void iniciarPreparacionPedido(String idPedido) throws Exception {
        Pedido pedido = pedidos.stream()
                .filter(p -> p.getIdPedido().equals(idPedido))
                .findFirst()
                .orElse(null);

        if (pedido == null) throw new Exception("Pedido no encontrado.");
        if (!"ABIERTO".equalsIgnoreCase(pedido.getEstado())) throw new Exception("Sólo pedidos en estado ABIERTO pueden iniciar preparación.");

        // Ya se descontó stock al confirmar el pedido, por tanto aquí solo cambiar estado
        pedido.setEstado("EN_PREPARACION");
        guardarDatosPersistentes();
    }


    public void procesarPagoPedido(String idPedido, VistaPrincipal mainFrame, CardLayout parentCardLayout, JPanel parentContentPanel) {
        Pedido pedido = pedidos.stream()
                .filter(p -> p.getIdPedido().equals(idPedido))
                .findFirst()
                .orElse(null);

        if (pedido == null) {
            JOptionPane.showMessageDialog(mainFrame, "Pedido no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ("PAGADO".equalsIgnoreCase(pedido.getEstado()) || pedido.isPagado()) {
            JOptionPane.showMessageDialog(mainFrame, "El pedido ya está pagado.", "Atención", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Solo permitir pago si el pedido fue ENTREGADO
        if (!"ENTREGADO".equalsIgnoreCase(pedido.getEstado())) {
            JOptionPane.showMessageDialog(mainFrame, "El pedido debe estar ENTREGADO para procesar el pago.", "Atención", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        final int subtotal = pedido.calcularTotal();
        final int sugerido10 = (int) Math.round(subtotal * 0.10);

        // Componentes del panel
        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));

        panel.add(new JLabel("Subtotal:"));
        JLabel lblSubtotal = new JLabel("$" + subtotal);
        panel.add(lblSubtotal);

        panel.add(new JLabel("Método de pago:"));
        JComboBox<String> cbMetodo = new JComboBox<>(new String[]{"EFECTIVO", "TARJETA", "TRANSFERENCIA"});
        panel.add(cbMetodo);

        panel.add(new JLabel("Monto entregado (solo EFECTIVO):"));
        SpinnerNumberModel modeloMonto = new SpinnerNumberModel(subtotal + sugerido10, 0, 10_000_000, 100);
        JSpinner spMonto = new JSpinner(modeloMonto);
        panel.add(spMonto);

        panel.add(new JLabel("Tipo de documento:"));
        JComboBox<String> cbTipoBoleta = new JComboBox<>(new String[]{"Boleta", "Factura"});
        panel.add(cbTipoBoleta);

        panel.add(new JLabel("Agregar propina (opcional):"));
        JCheckBox chkPropina = new JCheckBox("Activar propina");
        panel.add(chkPropina);

        panel.add(new JLabel("Propina seleccionada:"));
        JSpinner spPropina = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 100));
        spPropina.setEnabled(false);
        panel.add(spPropina);

        panel.add(new JLabel("Total a pagar:"));
        JLabel lblTotal = new JLabel("$" + subtotal);
        panel.add(lblTotal);

        // Inicial estado según método por defecto EFECTIVO -> habilitar monto solo si EFECTIVO
        boolean inicialEfectivo = "EFECTIVO".equalsIgnoreCase(cbMetodo.getSelectedItem().toString());
        spMonto.setEnabled(inicialEfectivo);

        // Listeners para comportamiento dinámico
        cbMetodo.addActionListener(e -> {
            String metodo = cbMetodo.getSelectedItem().toString();
            boolean esEfectivo = "EFECTIVO".equalsIgnoreCase(metodo);
            spMonto.setEnabled(esEfectivo);
        });

        chkPropina.addItemListener(e -> {
            boolean marcado = chkPropina.isSelected();
            spPropina.setEnabled(marcado);
            if (!marcado) {
                spPropina.setValue(0);
            } else {
                // si se activa por primera vez, sugerir 10%
                if (((Integer) spPropina.getValue()) == 0) spPropina.setValue(sugerido10);
            }
            // actualizar total y monto sugerido
            int prop = (Integer) spPropina.getValue();
            lblTotal.setText("$" + (subtotal + prop));
            modeloMonto.setValue(subtotal + prop);
        });

        spPropina.addChangeListener(e -> {
            int prop = (Integer) spPropina.getValue();
            lblTotal.setText("$" + (subtotal + prop));
            modeloMonto.setValue(subtotal + prop);
        });

        int opcion = JOptionPane.showConfirmDialog(mainFrame, panel, "Procesar pago del pedido " + idPedido,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) return;

        String metodo = cbMetodo.getSelectedItem().toString();
        String tipoDocumento = cbTipoBoleta.getSelectedItem().toString();
        int propina = chkPropina.isSelected() ? (Integer) spPropina.getValue() : 0;
        int montoEntregado = (Integer) spMonto.getValue();

        try {
            int totalAPagar = subtotal + propina;

            // Asegurar que exista un Pago asociado al pedido con método, propina y tipoDocumento correctos
            if (pedido.getPago() == null) {
                try {
                    // intentar crear el pago en el pedido (si la clase Pedido define crearPago)
                    pedido.crearPago(metodo, propina);
                } catch (Exception exCrearPago) {
                    // Si no es posible crear con crearPago(), crear manualmente un Pago mínimo
                    try {
                        Pago nuevo = new Pago(generarIdPago(), totalAPagar, metodo, propina, pedido);
                        pedido.setPago(nuevo);
                    } catch (Exception ex2) {
                        JOptionPane.showMessageDialog(mainFrame, "No se pudo crear el pago: " + ex2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            // actualizar propiedades del pago (incluyendo tipo de documento)
            Pago p = pedido.getPago();
            p.setMetodoPago(metodo);
            p.setPropina(propina);
            p.setMonto(totalAPagar);
            p.setTipoDocumento(tipoDocumento); // <-- guardar el tipo pedido por el cliente

            if ("EFECTIVO".equalsIgnoreCase(metodo)) {
                if (montoEntregado < totalAPagar) {
                    JOptionPane.showMessageDialog(mainFrame, "Dinero insuficiente. Se requiere al menos $" + totalAPagar, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                pedido.procesarPago(montoEntregado);
            } else {
                // para tarjeta/transferencia se procesa con el total (sin monto entregado)
                pedido.procesarPago(totalAPagar);
            }

            guardarDatosPersistentes();

            // Construir comprobante
            StringBuilder sb = new StringBuilder();
            sb.append("----- ").append(tipoDocumento).append(" -----\n");
            sb.append("ID Pedido: ").append(pedido.getIdPedido()).append("\n");
            sb.append("Fecha: ").append(pedido.getFechaHora()).append("\n");
            sb.append("Mesa: ").append(pedido.getMesa() != null ? pedido.getMesa().getNumero() : "").append("\n");
            sb.append("Garzón: ").append(pedido.getGarzon() != null ? pedido.getGarzon().getNombre() : "").append("\n\n");
            sb.append("Items:\n");
            for (DetallePedido d : pedido.getDetalles()) {
                String nombre = d.getPlato() != null ? d.getPlato().getNombre() : "";
                sb.append(String.format("%s x%d  = $%d\n", nombre, d.getCantidad(), d.getSubTotal()));
            }
            sb.append("\nSubtotal: $").append(subtotal).append("\n");
            sb.append("Propina: $").append(propina).append("\n");
            sb.append("Total a pagar: $").append(totalAPagar).append("\n");
            sb.append("Método: ").append(metodo).append("\n");
            if ("EFECTIVO".equalsIgnoreCase(metodo)) {
                int vuelto = pedido.getPago() != null ? pedido.getPago().getVuelto() : (montoEntregado - totalAPagar);
                sb.append("Monto entregado: $").append(montoEntregado).append("\n");
                sb.append("Vuelto: $").append(vuelto).append("\n");
            }

            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            ta.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
            JScrollPane scroll = new JScrollPane(ta);
            scroll.setPreferredSize(new java.awt.Dimension(400, 400));
            JOptionPane.showMessageDialog(mainFrame, scroll, "Boleta", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public List<Mesa> getMesas() {
        return mesas;
    }
    public void empezarEdicionPedido(String idPedido) throws Exception {
        Pedido pedido = pedidos.stream()
                .filter(p -> p.getIdPedido().equals(idPedido))
                .findFirst()
                .orElse(null);

        if (pedido == null) throw new Exception("Pedido no encontrado.");
        if ("PAGADO".equalsIgnoreCase(pedido.getEstado())) throw new Exception("No se puede editar un pedido ya pagado.");

        // Restaurar stock si fue descontado previamente.
        // Como ahora el descuento se hace al confirmar, considerar estados donde ya se realizó descuento:
        boolean debeRestaurarStock = "ABIERTO".equalsIgnoreCase(pedido.getEstado())
                || "EN_PREPARACION".equalsIgnoreCase(pedido.getEstado())
                || "LISTO".equalsIgnoreCase(pedido.getEstado());
        if (debeRestaurarStock) {
            for (DetallePedido det : pedido.getDetalles()) {
                Plato plato = det.getPlato();
                if (plato != null) {
                    plato.setDisponibilidad(plato.getDisponibilidad() + det.getCantidad());
                }
            }
        }

        // Quitar del historial y setear como pedido actual para editar
        pedidos.remove(pedido);
        pedido.setEstado("EDITANDO");
        this.pedidoActual = pedido;

        guardarDatosPersistentes();
    }

    public void marcarPedidoListo(String idPedido) throws Exception {
        Pedido pedido = pedidos.stream()
                .filter(p -> p.getIdPedido().equals(idPedido))
                .findFirst()
                .orElse(null);
        if (pedido == null) throw new Exception("Pedido no encontrado.");
        // Solo permitir si está en PREPARACION
        if (!"EN_PREPARACION".equalsIgnoreCase(pedido.getEstado())) {
            throw new Exception("Sólo pedidos en EN_PREPARACION pueden marcarse como LISTO.");
        }
        pedido.setEstado("LISTO");
        guardarDatosPersistentes();
    }

    public void marcarPedidoEntregado(String idPedido) throws Exception {
        Pedido pedido = pedidos.stream()
                .filter(p -> p.getIdPedido().equals(idPedido))
                .findFirst()
                .orElse(null);
        if (pedido == null) throw new Exception("Pedido no encontrado.");
        // Solo permitir si está en LISTO
        if (!"LISTO".equalsIgnoreCase(pedido.getEstado())) {
            throw new Exception("Sólo pedidos en LISTO pueden marcarse como ENTREGADO.");
        }
        pedido.setEstado("ENTREGADO");
        guardarDatosPersistentes();
    }
    public String obtenerComprobante(String idPedido, String tipoDocumento) throws Exception {
        Pedido pedido = pedidos.stream()
                .filter(p -> p.getIdPedido().equals(idPedido))
                .findFirst()
                .orElse(null);

        if (pedido == null) throw new Exception("Pedido no encontrado.");
        // Verificar que tenga pago procesado o esté marcado como pagado
        if (pedido.getPago() == null || !pedido.getPago().isProcesado()) {
            throw new Exception("El pedido no está pagado o no tiene un pago procesado.");
        }

        Pago pago = pedido.getPago();
        int subtotal = pedido.calcularTotal();
        int propina = pago.getPropina();
        int totalAPagar = subtotal + propina;
        int vuelto = pago.getVuelto();

        StringBuilder sb = new StringBuilder();
        sb.append("----- ").append(tipoDocumento != null ? tipoDocumento : "Comprobante").append(" -----\n");
        sb.append("ID Pedido: ").append(pedido.getIdPedido()).append("\n");
        sb.append("Fecha: ").append(pedido.getFechaHora()).append("\n");
        sb.append("Mesa: ").append(pedido.getMesa() != null ? pedido.getMesa().getNumero() : "").append("\n");
        sb.append("Garzón: ").append(pedido.getGarzon() != null ? pedido.getGarzon().getNombre() : "").append("\n\n");
        sb.append("Items:\n");
        for (DetallePedido d : pedido.getDetalles()) {
            String nombre = d.getPlato() != null ? d.getPlato().getNombre() : "";
            sb.append(String.format("%s x%d  = $%d\n", nombre, d.getCantidad(), d.getSubTotal()));
        }
        sb.append("\nSubtotal: $").append(subtotal).append("\n");
        sb.append("Propina: $").append(propina).append("\n");
        sb.append("Total a pagar: $").append(totalAPagar).append("\n");
        sb.append("Método: ").append(pago.getMetodoPago()).append("\n");
        // Mostrar monto entregado / vuelto si están disponibles
        if (pago.isProcesado()) {
            sb.append("Monto entregado: $").append(pago.getMontoProcesado() > 0 ? pago.getMontoProcesado() : pago.getMonto()).append("\n");
            sb.append("Vuelto: $").append(vuelto).append("\n");
        }
        return sb.toString();
    }
    public String obtenerComprobanteAutodetect(String idPedido) throws Exception {
        Pedido pedido = pedidos.stream()
                .filter(p -> p.getIdPedido().equals(idPedido))
                .findFirst()
                .orElse(null);

        if (pedido == null) throw new Exception("Pedido no encontrado.");
        if (pedido.getPago() == null || !pedido.getPago().isProcesado()) {
            throw new Exception("El pedido no está pagado o no tiene un pago procesado.");
        }

        String tipo = pedido.getPago().getTipoDocumento();
        if (tipo == null || tipo.trim().isEmpty()) {
            // si no hay tipo explícito, usar un título genérico
            tipo = "Comprobante";
        }
        return obtenerComprobante(idPedido, tipo);
    }
    public void agregarMesa(int numero, int capacidad, String estado) throws Exception {
        if (mesas == null) mesas = new ArrayList<>();
        if (buscarMesa(numero) != null) throw new Exception("La mesa ya existe.");
        mesas.add(new Mesa(numero, capacidad, estado));
        guardarDatosPersistentes();
    }

    public void agregarGarzon(String idGarzon, String nombre, String turno) throws Exception {
        if (garzones == null) garzones = new ArrayList<>();
        if (buscarGarzon(idGarzon) != null) throw new Exception("El garzón ya existe.");
        garzones.add(new Garzon(idGarzon, nombre, turno));
        guardarDatosPersistentes();
    }


}