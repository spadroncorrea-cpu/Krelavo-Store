package krelavo.store;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import krelavo.store.accesodatos.RepositorioClienteJDBC;
import krelavo.store.accesodatos.RepositorioUsuarioJDBC;
import krelavo.store.basedatos.FabricaConexiones;
import krelavo.store.modelo.DireccionCliente;
import krelavo.store.modelo.LineaCarrito;
import krelavo.store.modelo.PedidoCliente;
import krelavo.store.modelo.Producto;
import krelavo.store.modelo.Rol;
import krelavo.store.modelo.Usuario;
import krelavo.store.seguridad.HashContrasenas;
import krelavo.store.seguridad.SesionUsuario;
import krelavo.store.servicio.ServicioAutenticacion;
import krelavo.store.servicio.ServicioCliente;
import javax.imageio.ImageIO;

/** Pruebas de integración y navegación sobre una base SQLite temporal. */
public final class VerificacionIntegracion extends Application {
    private static int comprobaciones;
    private static Path carpetaTemporal;
    private static Path rutaBaseDatos;
    private static Throwable fallo;

    public static void main(String[] argumentos) throws Exception {
        carpetaTemporal = Files.createTempDirectory(Paths.get("build", "verification"), "integracion-");
        rutaBaseDatos = carpetaTemporal.resolve("krelavo-integracion.db");
        System.setProperty("krelavo.db.path", rutaBaseDatos.toString());
        System.setProperty("krelavo.envio.cop", "0");
        launch(argumentos);
        if (fallo != null) {
            throw new AssertionError("Fallaron las pruebas de integración", fallo);
        }
        Files.deleteIfExists(rutaBaseDatos);
        Files.deleteIfExists(carpetaTemporal);
        System.out.println("INTEGRACIÓN CORRECTA: " + comprobaciones + " comprobaciones JavaFX/JDBC.");
    }

    @Override
    public void start(Stage ventana) {
        try {
            ejecutarPruebas(ventana);
        } catch (Throwable error) {
            fallo = error;
            error.printStackTrace(System.err);
        } finally {
            ventana.close();
            Platform.exit();
        }
    }

    private void ejecutarPruebas(Stage ventana) throws Exception {
        Main aplicacion = new Main();
        aplicacion.start(ventana);
        capturar(ventana, "configuracion-inicial");
        FabricaConexiones fabrica = new FabricaConexiones(rutaBaseDatos);
        RepositorioUsuarioJDBC repositorioUsuarios = new RepositorioUsuarioJDBC(fabrica);
        RepositorioClienteJDBC repositorioClientes = new RepositorioClienteJDBC(fabrica);
        ServicioAutenticacion autenticacion = aplicacion.obtenerServicioAutenticacion();
        ServicioCliente clientes = aplicacion.obtenerServicioCliente();
        HashContrasenas hasher = new HashContrasenas();

        comprobar("el primer inicio presenta configuración inicial", tieneClase(ventana, "login-screen")
                && contar(ventana.getScene().getRoot(), PasswordField.class) == 2
                && imagenesCargadas(ventana.getScene().getRoot()) == 3);
        comprobar("la base temporal comienza sin administrador", !autenticacion.hayAdministrador());

        Usuario administrador = autenticacion.crearAdministradorInicial("Admin Pruebas",
                "ADMIN@krelavo.test", "ClaveAdmin-2026!", "ClaveAdmin-2026!");
        comprobar("se crea el primer administrador", administrador.obtenerRol() == Rol.ADMIN
                && autenticacion.hayAdministrador());
        comprobar("la contraseña del administrador se guarda como hash con sal",
                esHash(repositorioUsuarios.buscarCredenciales("admin@krelavo.test").obtenerHashContrasena()));
        esperarFallo("la configuración inicial rechaza un segundo administrador",
                IllegalStateException.class, () -> autenticacion.crearAdministradorInicial(
                        "Admin Dos", "admin2@krelavo.test", "ClaveAdmin-2026!", "ClaveAdmin-2026!"));

        Usuario cliente = autenticacion.registrarCliente("Cliente Uno", "cliente@krelavo.test",
                "ClaveCliente-2026!", "ClaveCliente-2026!");
        Usuario otroCliente = autenticacion.registrarCliente("Cliente Dos", "otro@krelavo.test",
                "OtraClaveCliente-2026!", "OtraClaveCliente-2026!");
        comprobar("el registro público crea exclusivamente CLIENTE", cliente.obtenerRol() == Rol.CLIENTE
                && otroCliente.obtenerRol() == Rol.CLIENTE);
        comprobar("el registro público no ofrece selector de rol",
                !tieneTexto(ventana.getScene().getRoot(), "ADMIN"));
        esperarFallo("se rechaza el correo duplicado", IllegalArgumentException.class,
                () -> autenticacion.registrarCliente("Duplicado", "CLIENTE@KRELAVO.TEST",
                        "ClaveCliente-2026!", "ClaveCliente-2026!"));
        comprobar("el hash del cliente también está protegido", esHash(
                repositorioUsuarios.buscarCredenciales("cliente@krelavo.test").obtenerHashContrasena()));
        comprobar("ADMIN también puede autenticarse por el Login único",
                autenticacion.autenticar("admin@krelavo.test", "ClaveAdmin-2026!").obtenerRol() == Rol.ADMIN);
        comprobar("cliente puede autenticarse sin distinguir mayúsculas del correo",
                autenticacion.autenticar("CLIENTE@KRELAVO.TEST", "ClaveCliente-2026!").obtenerRol() == Rol.CLIENTE);
        esperarFallo("se rechaza una contraseña incorrecta", IllegalArgumentException.class,
                () -> autenticacion.autenticar("cliente@krelavo.test", "Incorrecta-2026!"));

        SesionUsuario sesion = aplicacion.obtenerSesion();
        aplicacion.mostrarInicioSesion();
        capturar(ventana, "inicio-sesion");
        comprobar("tras la configuración inicial se muestra el Login único", tieneClase(ventana, "login-screen")
                && contar(ventana.getScene().getRoot(), PasswordField.class) == 1
                && imagenesCargadas(ventana.getScene().getRoot()) == 3);
        aplicacion.mostrarConfiguracionInicial();
        comprobar("la configuración inicial queda bloqueada después del primer administrador",
                tieneClase(ventana, "login-screen") && contar(ventana.getScene().getRoot(), PasswordField.class) == 1);
        aplicacion.mostrarRegistroCliente();
        capturar(ventana, "registro-cliente");
        comprobar("la pantalla de registro es accesible sin selector ADMIN",
                tieneClase(ventana, "login-screen") && !tieneTexto(ventana.getScene().getRoot(), "ADMIN"));

        aplicacion.iniciarSesion(administrador);
        capturar(ventana, "panel-administrador");
        comprobar("ADMIN llega al panel administrativo", tieneClase(ventana, "admin-root")
                && sesion.tieneRol(Rol.ADMIN));
        aplicacion.mostrarTienda();
        comprobar("ADMIN no entra al catálogo cliente", tieneClase(ventana, "admin-root"));
        aplicacion.mostrarMiCuenta();
        comprobar("ADMIN no accede a Mi cuenta", tieneClase(ventana, "admin-root"));
        pulsarBoton(ventana.getScene().getRoot(), "Productos");
        comprobar("la sección de productos del panel abre su tabla", tieneClase(ventana, "admin-root")
                && contar(ventana.getScene().getRoot(), javafx.scene.control.TableView.class) > 0);
        capturar(ventana, "productos-administracion");
        pulsarBoton(ventana.getScene().getRoot(), "Inventario");
        comprobar("la sección de inventario del panel abre su tabla", tieneClase(ventana, "admin-root")
                && contar(ventana.getScene().getRoot(), javafx.scene.control.TableView.class) > 0);
        capturar(ventana, "inventario-administracion");
        aplicacion.cerrarSesion();
        comprobar("cerrar sesión ADMIN elimina la sesión y vuelve al Login", !sesion.estaAutenticada()
                && tieneClase(ventana, "login-screen"));

        aplicacion.iniciarSesion(autenticacion.autenticar("cliente@krelavo.test", "ClaveCliente-2026!"));
        comprobar("CLIENTE llega a la tienda y no al panel administrativo",
                tieneClase(ventana, "catalog-screen") && sesion.tieneRol(Rol.CLIENTE));

        List<Producto> productos = clientes.listarProductos();
        comprobar("el catálogo conectado contiene los 16 productos", productos.size() == 16);
        Producto primero = productos.get(0);
        Producto segundo = productos.get(1);
        long idCliente = cliente.obtenerIdentificador();
        long idOtro = otroCliente.obtenerIdentificador();

        clientes.agregarAlCarrito(idCliente, primero.obtenerIdentificador(), 2);
        comprobar("el carrito conserva cantidades al abrir otro repositorio",
                new RepositorioClienteJDBC(new FabricaConexiones(rutaBaseDatos))
                        .obtenerCarrito(idCliente).get(0).obtenerCantidad() == 2);
        clientes.actualizarCantidadCarrito(idCliente, primero.obtenerIdentificador(), 3);
        comprobar("la cantidad del carrito se actualiza y el subtotal se recalcula",
                clientes.obtenerCarrito(idCliente).get(0).obtenerSubtotal() == primero.obtenerPrecioCop() * 3);
        esperarFallo("el carrito impide superar existencias", java.sql.SQLException.class,
                () -> clientes.actualizarCantidadCarrito(idCliente, primero.obtenerIdentificador(), 11));
        clientes.quitarDelCarrito(idCliente, primero.obtenerIdentificador());
        comprobar("eliminar una línea vacía el carrito", clientes.obtenerUnidadesCarrito(idCliente) == 0);
        clientes.agregarAlCarrito(idCliente, primero.obtenerIdentificador(), 2);

        comprobar("el favorito persiste en SQLite", clientes.cambiarFavorito(idCliente, primero.obtenerIdentificador())
                && new RepositorioClienteJDBC(new FabricaConexiones(rutaBaseDatos))
                        .listarFavoritos(idCliente).size() == 1);
        comprobar("los favoritos quedan aislados por cliente", clientes.listarFavoritos(idOtro).isEmpty());
        comprobar("el costo de envío inicial está configurado en cero pesos", clientes.obtenerCostoEnvio() == 0);
        aplicacion.cerrarSesion();
        aplicacion.iniciarSesion(autenticacion.autenticar("cliente@krelavo.test", "ClaveCliente-2026!"));
        comprobar("carrito y favoritos sobreviven al cierre y nuevo inicio de sesión",
                clientes.obtenerUnidadesCarrito(idCliente) == 2
                && clientes.listarFavoritos(idCliente).size() == 1);
        esperarFallo("el rol CLIENTE no puede cambiarse directamente en SQLite", java.sql.SQLException.class,
                () -> ejecutarSql(fabrica, "UPDATE users SET role='ADMIN' WHERE id=" + idCliente));
        comprobar("la cuenta conserva el rol CLIENTE después del intento de elevación",
                repositorioUsuarios.buscarCredenciales("cliente@krelavo.test").obtenerUsuario().obtenerRol() == Rol.CLIENTE);

        Usuario actualizado = clientes.actualizarPerfil(idCliente, "Cliente Uno Actualizado",
                "cliente.nuevo@krelavo.test", "+57 300 123 4567");
        comprobar("el perfil actualiza nombre, correo y teléfono", actualizado.obtenerNombre().contains("Actualizado")
                && actualizado.obtenerCorreo().equals("cliente.nuevo@krelavo.test")
                && actualizado.obtenerTelefono().contains("300"));
        esperarFallo("el perfil rechaza correo de otra cuenta", IllegalArgumentException.class,
                () -> clientes.actualizarPerfil(idCliente, "Nombre", "otro@krelavo.test", "3001234567"));

        DireccionCliente direccion = clientes.guardarDireccion(idCliente, 0, "Cliente Uno", "3001234567",
                "Calle 10 # 20-30", "Bogotá", "Cundinamarca", "Torre A", true);
        DireccionCliente direccionAlterna = clientes.guardarDireccion(idCliente, 0, "Cliente Uno", "3001234567",
                "Carrera 20 # 10-50", "Bogotá", "Cundinamarca", "", false);
        direccionAlterna = clientes.guardarDireccion(idCliente, direccionAlterna.obtenerIdentificador(),
                "Cliente Uno", "3001234567", "Carrera 20 # 10-51", "Bogotá", "Cundinamarca", "Editar", true);
        comprobar("se guardan, editan y marcan direcciones principales",
                clientes.listarDirecciones(idCliente).size() == 2 && direccionAlterna.esPrincipal()
                && "Carrera 20 # 10-51".equals(direccionAlterna.obtenerLinea()));
        esperarFallo("un usuario no puede borrar una dirección ajena", java.sql.SQLException.class,
                () -> clientes.eliminarDireccion(idOtro, direccion.obtenerIdentificador()));
        esperarFallo("un usuario no puede modificar una dirección ajena", java.sql.SQLException.class,
                () -> clientes.guardarDireccion(idOtro, direccion.obtenerIdentificador(), "Otro", "3001234567",
                        "Calle falsa", "Bogotá", "Cundinamarca", "", true));
        comprobar("la dirección ajena sigue en poder de su propietario",
                clientes.listarDirecciones(idCliente).size() == 2);

        clientes.cambiarContrasena(idCliente, "ClaveCliente-2026!", "NuevaClaveCliente-2026!",
                "NuevaClaveCliente-2026!");
        esperarFallo("la contraseña anterior deja de autenticar", IllegalArgumentException.class,
                () -> autenticacion.autenticar("cliente.nuevo@krelavo.test", "ClaveCliente-2026!"));
        comprobar("la nueva contraseña autentica", autenticacion.autenticar(
                "cliente.nuevo@krelavo.test", "NuevaClaveCliente-2026!").obtenerIdentificador() == idCliente);

        String codigoPedido = clientes.confirmarPedido(idCliente, direccion.obtenerIdentificador());
        PedidoCliente pedido = clientes.buscarPedido(idCliente, codigoPedido);
        comprobar("el checkout crea pedido, detalle y copia histórica de dirección",
                pedido != null && pedido.obtenerLineas().size() == 1 && pedido.obtenerLineas().get(0).obtenerCantidad() == 2
                && pedido.obtenerTotal() == primero.obtenerPrecioCop() * 2
                && "Calle 10 # 20-30".equals(pedido.obtenerLinea()));
        comprobar("el pedido se conserva en el historial y solo lo ve su propietario",
                clientes.listarPedidos(idCliente).size() == 1 && clientes.buscarPedido(idOtro, codigoPedido) == null);
        clientes.guardarDireccion(idCliente, direccion.obtenerIdentificador(), "Cliente Uno", "3001234567",
                "Dirección actualizada después de la compra", "Bogotá", "Cundinamarca", "", true);
        comprobar("editar una dirección no altera su copia histórica en el pedido",
                "Calle 10 # 20-30".equals(clientes.buscarPedido(idCliente, codigoPedido).obtenerLinea()));
        comprobar("checkout descuenta inventario y registra salida",
                consultarEntero(fabrica, "SELECT stock_quantity FROM products WHERE id=" + primero.obtenerIdentificador()) == 8
                && consultarEntero(fabrica, "SELECT count(*) FROM inventory_movements WHERE order_id="
                        + pedido.obtenerIdentificador() + " AND movement_type='OUT' AND quantity_change=-2") == 1);
        comprobar("checkout vacía el carrito después de registrar el pedido", clientes.obtenerUnidadesCarrito(idCliente) == 0);

        clientes.agregarAlCarrito(idCliente, segundo.obtenerIdentificador(), 1);
        int pedidosPrevios = clientes.listarPedidos(idCliente).size();
        int movimientosPrevios = consultarEntero(fabrica, "SELECT count(*) FROM inventory_movements");
        int existenciasPrevias = consultarEntero(fabrica, "SELECT stock_quantity FROM products WHERE id="
                + segundo.obtenerIdentificador());
        ejecutarSql(fabrica, "CREATE TRIGGER fallo_prueba_movimiento BEFORE INSERT ON inventory_movements "
                + "WHEN NEW.movement_type='OUT' BEGIN SELECT RAISE(ABORT,'fallo controlado'); END");
        esperarFallo("checkout comunica error al fallar un movimiento de inventario", java.sql.SQLException.class,
                () -> clientes.confirmarPedido(idCliente, direccion.obtenerIdentificador()));
        ejecutarSql(fabrica, "DROP TRIGGER fallo_prueba_movimiento");
        comprobar("la compra fallida revierte pedido, existencias y movimientos",
                clientes.listarPedidos(idCliente).size() == pedidosPrevios
                && consultarEntero(fabrica, "SELECT stock_quantity FROM products WHERE id="
                        + segundo.obtenerIdentificador()) == existenciasPrevias
                && consultarEntero(fabrica, "SELECT count(*) FROM inventory_movements") == movimientosPrevios);
        comprobar("la compra fallida conserva el carrito", clientes.obtenerUnidadesCarrito(idCliente) == 1);

        clientes.agregarAlCarrito(idOtro, segundo.obtenerIdentificador(), 1);
        int existenciasAntesAjena = consultarEntero(fabrica, "SELECT stock_quantity FROM products WHERE id="
                + segundo.obtenerIdentificador());
        esperarFallo("checkout rechaza dirección de otra cuenta", java.sql.SQLException.class,
                () -> clientes.confirmarPedido(idOtro, direccion.obtenerIdentificador()));
        comprobar("el rechazo por dirección ajena no descuenta inventario",
                consultarEntero(fabrica, "SELECT stock_quantity FROM products WHERE id="
                        + segundo.obtenerIdentificador()) == existenciasAntesAjena);

        verificarPantallasCliente(ventana, aplicacion, clientes,
                autenticacion.autenticar("cliente.nuevo@krelavo.test", "NuevaClaveCliente-2026!"), codigoPedido);

        aplicacion.iniciarSesion(administrador);
        comprobar("volver a ADMIN lleva de nuevo al panel", tieneClase(ventana, "admin-root"));
        aplicacion.cerrarSesion();
        comprobar("el cierre de sesión limpia sesión y vuelve al Login", !sesion.estaAutenticada()
                && tieneClase(ventana, "login-screen"));
    }

    private void verificarPantallasCliente(Stage ventana, Main aplicacion, ServicioCliente clientes,
            Usuario cliente, String codigoPedido) throws Exception {
        aplicacion.iniciarSesion(cliente);
        Parent raiz = ventana.getScene().getRoot();
        comprobar("la pantalla principal del cliente es el catálogo real de 16 productos",
                tieneClase(ventana, "catalog-screen") && contarClase(raiz, "product-card") == 16
                && imagenesCargadas(raiz) == 16);
        capturar(ventana, "catalogo");
        javafx.scene.control.ScrollPane desplazamientoCatalogo =
                (javafx.scene.control.ScrollPane) ((javafx.scene.layout.BorderPane) raiz).getCenter();
        desplazamientoCatalogo.setVvalue(0.56);
        capturar(ventana, "catalogo-productos");
        desplazamientoCatalogo.setVvalue(0);
        pulsarBoton(raiz, "Favoritos");
        comprobar("la navegación del catálogo abre la lista de deseos", tieneClase(ventana, "cliente-pantalla")
                && tieneTexto(ventana.getScene().getRoot(), "Tu lista de deseos"));
        capturar(ventana, "lista-deseos");
        pulsarBoton(ventana.getScene().getRoot(), "Carrito");
        comprobar("la navegación abre el carrito como pantalla completa", tieneClase(ventana, "cliente-pantalla")
                && tieneTexto(ventana.getScene().getRoot(), "Tu carrito"));
        capturar(ventana, "carrito");
        pulsarBoton(ventana.getScene().getRoot(), "Mi cuenta");
        comprobar("Mi cuenta muestra datos de perfil y no ofrece administración",
                tieneClase(ventana, "cliente-pantalla") && tieneTexto(ventana.getScene().getRoot(), "Mi cuenta")
                && !tieneTexto(ventana.getScene().getRoot(), "Administración"));
        capturar(ventana, "mi-cuenta");
        pulsarBoton(ventana.getScene().getRoot(), "Mis direcciones");
        comprobar("Mi cuenta abre la sección de direcciones", tieneTexto(ventana.getScene().getRoot(), "Mis direcciones"));
        capturar(ventana, "direcciones");
        pulsarBoton(ventana.getScene().getRoot(), "Mis pedidos");
        comprobar("Mi cuenta abre pedidos y su detalle", tieneTexto(ventana.getScene().getRoot(), codigoPedido));
        capturar(ventana, "pedidos");
        pulsarBoton(ventana.getScene().getRoot(), "Cerrar sesión");
        comprobar("cerrar sesión desde Mi cuenta elimina la sesión", !aplicacion.obtenerSesion().estaAutenticada()
                && tieneClase(ventana, "login-screen"));
    }

    private static boolean esHash(String hash) {
        return hash != null && hash.startsWith("pbkdf2_sha256$") && !hash.contains("Clave");
    }

    private static void comprobar(String nombre, boolean correcto) {
        if (!correcto) throw new AssertionError("Falló: " + nombre);
        comprobaciones++;
        System.out.println("OK: " + nombre);
    }

    private static void esperarFallo(String nombre, Class<? extends Throwable> tipo, Accion accion) throws Exception {
        try {
            accion.ejecutar();
        } catch (Throwable error) {
            if (tipo.isInstance(error)) {
                comprobar(nombre, true);
                return;
            }
            throw new AssertionError(nombre + " (tipo inesperado: " + error + ")", error);
        }
        comprobar(nombre, false);
    }

    private static int consultarEntero(FabricaConexiones fabrica, String consulta) throws Exception {
        try (Connection conexion = fabrica.abrirConexion(); Statement sentencia = conexion.createStatement();
                ResultSet resultado = sentencia.executeQuery(consulta)) {
            if (!resultado.next()) throw new AssertionError("Consulta sin resultado: " + consulta);
            return resultado.getInt(1);
        }
    }

    private static void capturar(Stage ventana, String nombre) throws Exception {
        Parent raiz = ventana.getScene().getRoot();
        raiz.applyCss();
        raiz.layout();
        int ancho = Math.max(1, (int) ventana.getScene().getWidth());
        int alto = Math.max(1, (int) ventana.getScene().getHeight());
        WritableImage captura = ventana.getScene().snapshot(new WritableImage(ancho, alto));
        Path carpeta = Paths.get("build", "verification", "capturas");
        Files.createDirectories(carpeta);
        ImageIO.write(SwingFXUtils.fromFXImage(captura, null), "png",
                new File(carpeta.resolve(nombre + ".png").toString()));
    }

    private static void ejecutarSql(FabricaConexiones fabrica, String consulta) throws Exception {
        try (Connection conexion = fabrica.abrirConexion(); Statement sentencia = conexion.createStatement()) {
            sentencia.execute(consulta);
        }
    }

    private static boolean tieneClase(Stage ventana, String nombre) {
        return ventana.getScene() != null && ventana.getScene().getRoot().getStyleClass().contains(nombre);
    }

    private static int contar(Parent raiz, Class<?> tipo) {
        int cantidad = tipo.isInstance(raiz) ? 1 : 0;
        for (Node nodo : raiz.getChildrenUnmodifiable()) {
            if (nodo instanceof Parent) {
                cantidad += contar((Parent) nodo, tipo);
            } else if (tipo.isInstance(nodo)) {
                cantidad++;
            }
            if (nodo instanceof javafx.scene.control.ScrollPane) {
                Node contenido = ((javafx.scene.control.ScrollPane) nodo).getContent();
                if (contenido instanceof Parent) cantidad += contar((Parent) contenido, tipo);
            }
        }
        return cantidad;
    }

    private static int contarClase(Parent raiz, String nombreClase) {
        int cantidad = raiz.getStyleClass().contains(nombreClase) ? 1 : 0;
        for (Node nodo : raiz.getChildrenUnmodifiable()) {
            if (nodo instanceof javafx.scene.control.ScrollPane) {
                Node contenido = ((javafx.scene.control.ScrollPane) nodo).getContent();
                if (contenido instanceof Parent) cantidad += contarClase((Parent) contenido, nombreClase);
            } else if (nodo instanceof Parent) {
                cantidad += contarClase((Parent) nodo, nombreClase);
            } else if (nodo.getStyleClass().contains(nombreClase)) {
                cantidad++;
            }
        }
        return cantidad;
    }

    private static int imagenesCargadas(Parent raiz) {
        int cantidad = 0;
        for (Node nodo : raiz.getChildrenUnmodifiable()) {
            if (nodo instanceof javafx.scene.image.ImageView) {
                javafx.scene.image.Image imagen = ((javafx.scene.image.ImageView) nodo).getImage();
                if (imagen != null && !imagen.isError() && imagen.getWidth() > 0 && imagen.getHeight() > 0) cantidad++;
            }
            if (nodo instanceof Parent) cantidad += imagenesCargadas((Parent) nodo);
            if (nodo instanceof javafx.scene.control.ScrollPane) {
                Node contenido = ((javafx.scene.control.ScrollPane) nodo).getContent();
                if (contenido instanceof Parent) cantidad += imagenesCargadas((Parent) contenido);
            }
        }
        return cantidad;
    }

    private static boolean tieneTexto(Parent raiz, String texto) {
        if (raiz instanceof Label && texto.equals(((Label) raiz).getText())) return true;
        if (raiz instanceof Button && texto.equals(((Button) raiz).getText())) return true;
        if (raiz instanceof TextField && texto.equals(((TextField) raiz).getPromptText())) return true;
        for (Node nodo : raiz.getChildrenUnmodifiable()) {
            if (nodo instanceof Parent && tieneTexto((Parent) nodo, texto)) return true;
            if (nodo instanceof Button && texto.equals(((Button) nodo).getText())) return true;
            if (nodo instanceof Label && texto.equals(((Label) nodo).getText())) return true;
            if (nodo instanceof javafx.scene.control.ScrollPane) {
                Node contenido = ((javafx.scene.control.ScrollPane) nodo).getContent();
                if (contenido instanceof Parent && tieneTexto((Parent) contenido, texto)) return true;
            }
        }
        return false;
    }

    private static void pulsarBoton(Parent raiz, String texto) {
        Button boton = buscarBoton(raiz, texto);
        if (boton == null) throw new AssertionError("No se encontró el botón: " + texto);
        boton.fire();
    }

    private static Button buscarBoton(Parent raiz, String texto) {
        for (Node nodo : raiz.getChildrenUnmodifiable()) {
            if (nodo instanceof Button && texto.equals(((Button) nodo).getText().trim())) return (Button) nodo;
            if (nodo instanceof Parent) {
                Button encontrado = buscarBoton((Parent) nodo, texto);
                if (encontrado != null) return encontrado;
            }
            if (nodo instanceof javafx.scene.control.ScrollPane) {
                Node contenido = ((javafx.scene.control.ScrollPane) nodo).getContent();
                if (contenido instanceof Parent) {
                    Button encontrado = buscarBoton((Parent) contenido, texto);
                    if (encontrado != null) return encontrado;
                }
            }
        }
        return null;
    }

    private interface Accion {
        void ejecutar() throws Exception;
    }
}
