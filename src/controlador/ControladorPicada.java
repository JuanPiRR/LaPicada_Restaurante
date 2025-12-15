// Controlador hecho por: Jesús Lema, Juan Recabal
package controlador;

import modelo.*; // Importar todas las clases del modelo

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
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
    public void confirmarPedidoYEnviarCocina() throws Exception {
        if (pedidoActual == null) throw new Exception("No hay pedido activo para confirmar.");

        // Descontar stock
        for (DetallePedido det : pedidoActual.getDetalles()) {
            if (det.getPlato() != null) {
                det.getPlato().restarStock(det.getCantidad());
            }
        }

        // Marcar estado y mover a historial para preparación/pago posterior
        pedidoActual.setEstado("EN_PREPARACION");
        pedidos.add(pedidoActual);

        // Persistir y limpiar pedidoActual (liberar UI para nuevo pedido)
        guardarDatosPersistentes();
        pedidoActual = null;
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
        return mesas.stream().filter(m -> m.getEstado().equals("DISPONIBLE")).collect(Collectors.toList());
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
                int cantidadRecibida = d.getCantidad(); // en panelRecepciones ya se actualizó d.setCantidad(...)
                if (cantidadRecibida <= 0) continue;

                Insumo ref = d.getInsumo();
                Insumo ins = null;
                if (ref != null) ins = buscarInsumo(ref.getIdInsumo());

                if (ins == null) {
                    // Crear insumo mínimo si no existe (atributos básicos desde el detalle si están)
                    String id = ref != null && ref.getIdInsumo() != null ? ref.getIdInsumo() : "I-" + System.currentTimeMillis();
                    String nombre = ref != null ? ref.getNombre() : "";
                    String categoria = ref != null ? ref.getCategoria() : "";
                    String unidad = ref != null ? ref.getUnidadMedida() : "";
                    ins = new Insumo(id, nombre, categoria, unidad, 0, 0, d.getPrecioUnitario());
                    insumos.add(ins);
                }

                // Sumar stock y actualizar precio unitario con el precio recibido
                ins.agregarStock(cantidadRecibida);
                if (d.getPrecioUnitario() >= 0) ins.setPrecioUnitario(d.getPrecioUnitario());
            }
        }

        // Actualizar estados
        r.setEstado(estadoFinal);
        oc.cambiarEstado("RECIBIDA");

        // Persistir y notificar listeners
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
}