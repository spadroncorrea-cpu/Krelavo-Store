package krelavo.store;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import krelavo.store.controlador.ControladorCatalogo;
import krelavo.store.controlador.ControladorConfiguracionInicial;
import krelavo.store.controlador.ControladorMiCuenta;
import krelavo.store.controlador.ControladorPanelAdministracion;
import krelavo.store.controlador.ControladorRegistroCliente;
import krelavo.store.controlador.ControladorInicioSesion;
import krelavo.store.accesodatos.RepositorioUsuarioJDBC;
import krelavo.store.accesodatos.RepositorioClienteJDBC;
import krelavo.store.basedatos.FabricaConexiones;
import krelavo.store.basedatos.InicializadorBaseDatos;
import krelavo.store.modelo.Rol;
import krelavo.store.modelo.Usuario;
import krelavo.store.seguridad.HashContrasenas;
import krelavo.store.seguridad.SesionUsuario;
import krelavo.store.servicio.ServicioAutenticacion;
import krelavo.store.servicio.ServicioCliente;

/** Punto de composición de pantallas, sesión, permisos y servicios. */
public class Main extends Application {
    private Stage ventana;
    private Scene escena;
    private FabricaConexiones fabricaConexiones;
    private ServicioAutenticacion servicioAutenticacion;
    private ServicioCliente servicioCliente;
    private final SesionUsuario sesion = new SesionUsuario();
    private Parent vistaTienda;
    private long identificadorClienteTienda = -1;

    @Override
    public void start(Stage escenario) {
        ventana = escenario;
        ventana.setTitle("Krelavo Store");
        ventana.setMinWidth(960);
        ventana.setMinHeight(820);
        try {
            fabricaConexiones = new FabricaConexiones();
            new InicializadorBaseDatos(fabricaConexiones).inicializar();
            HashContrasenas hasher = new HashContrasenas();
            servicioAutenticacion = new ServicioAutenticacion(
                    new RepositorioUsuarioJDBC(fabricaConexiones), hasher);
            servicioCliente = new ServicioCliente(new RepositorioClienteJDBC(fabricaConexiones), hasher);
            if (servicioAutenticacion.hayAdministrador()) {
                mostrarInicioSesion();
            } else {
                mostrarConfiguracionInicial();
            }
            ventana.show();
        } catch (Exception ex) {
            mostrarErrorBaseDatos(ex);
            Platform.exit();
        }
    }

    /** Conserva el punto de entrada anterior mientras usa la navegación protegida. */
    public void mostrarInicioSesionCompatibilidad() {
        mostrarInicioSesion();
    }

    /** Conserva el punto de entrada anterior mientras usa la navegación protegida. */
    public void mostrarTiendaCompatibilidad() {
        mostrarTienda();
    }

    public void mostrarInicioSesion() {
        if (sesion.estaAutenticada()) {
            redirigirSegunRol();
            return;
        }
        try {
            if (servicioAutenticacion != null && !servicioAutenticacion.hayAdministrador()) {
                mostrarConfiguracionInicial();
                return;
            }
        } catch (Exception ex) {
            mostrarErrorBaseDatos(ex);
            return;
        }
        establecerEscena(new ControladorInicioSesion(this, servicioAutenticacion).crearVista());
    }

    public void mostrarConfiguracionInicial() {
        if (sesion.estaAutenticada()) {
            redirigirSegunRol();
            return;
        }
        if (servicioAutenticacion == null) {
            mostrarInicioSesion();
            return;
        }
        try {
            if (servicioAutenticacion.hayAdministrador()) {
                establecerEscena(new ControladorInicioSesion(this, servicioAutenticacion).crearVista());
                return;
            }
        } catch (Exception ex) {
            mostrarErrorBaseDatos(ex);
            return;
        }
        establecerEscena(new ControladorConfiguracionInicial(this, servicioAutenticacion).crearVista());
    }

    public void mostrarRegistroCliente() {
        if (sesion.estaAutenticada()) {
            redirigirSegunRol();
            return;
        }
        try {
            if (servicioAutenticacion == null || !servicioAutenticacion.hayAdministrador()) {
                mostrarConfiguracionInicial();
                return;
            }
        } catch (Exception ex) {
            mostrarErrorBaseDatos(ex);
            return;
        }
        establecerEscena(new ControladorRegistroCliente(this, servicioAutenticacion).crearVista());
    }

    public void iniciarSesion(Usuario usuario) {
        sesion.iniciar(usuario);
        redirigirSegunRol();
    }

    public void mostrarTienda() {
        if (!sesion.estaAutenticada()) {
            mostrarInicioSesion();
            return;
        }
        if (sesion.tieneRol(Rol.ADMIN)) {
            mostrarPanelAdministracion();
            return;
        }
        if (!sesion.tieneRol(Rol.CLIENTE)) {
            cerrarSesion();
            return;
        }
        long identificadorActual = sesion.obtenerUsuarioActual().obtenerIdentificador();
        if (vistaTienda == null || identificadorActual != identificadorClienteTienda) {
            ControladorCatalogo controlador = new ControladorCatalogo(this);
            try {
                vistaTienda = controlador.crearVista();
                identificadorClienteTienda = identificadorActual;
            } catch (Exception ex) { mostrarErrorBaseDatos(ex); return; }
        }
        establecerEscena(vistaTienda);
    }

    public void mostrarMiCuenta() {
        if (!sesion.tieneRol(Rol.CLIENTE)) {
            redirigirSegunRol();
            return;
        }
        vistaTienda = null;
        identificadorClienteTienda = -1;
        try {
            establecerEscena(new ControladorMiCuenta(this, sesion.obtenerUsuarioActual(), servicioCliente).crearVista());
        } catch (Exception ex) { mostrarErrorBaseDatos(ex); }
    }

    public void mostrarCarrito() {
        if (!sesion.tieneRol(Rol.CLIENTE)) { redirigirSegunRol(); return; }
        vistaTienda = null; identificadorClienteTienda = -1;
        try { establecerEscena(new krelavo.store.controlador.ControladorCarritoCliente(this,
                sesion.obtenerUsuarioActual(), servicioCliente).crearVista()); }
        catch (Exception ex) { mostrarErrorBaseDatos(ex); }
    }

    public void mostrarFavoritos() {
        if (!sesion.tieneRol(Rol.CLIENTE)) { redirigirSegunRol(); return; }
        vistaTienda = null; identificadorClienteTienda = -1;
        try { establecerEscena(new krelavo.store.controlador.ControladorFavoritosCliente(this,
                sesion.obtenerUsuarioActual(), servicioCliente).crearVista()); }
        catch (Exception ex) { mostrarErrorBaseDatos(ex); }
    }

    public void mostrarPedidosCliente() {
        if (!sesion.tieneRol(Rol.CLIENTE)) { redirigirSegunRol(); return; }
        vistaTienda = null; identificadorClienteTienda = -1;
        try { establecerEscena(new ControladorMiCuenta(this, sesion.obtenerUsuarioActual(), servicioCliente)
                .crearVistaPedidos()); }
        catch (Exception ex) { mostrarErrorBaseDatos(ex); }
    }

    public void mostrarDireccionesCliente() {
        if (!sesion.tieneRol(Rol.CLIENTE)) { redirigirSegunRol(); return; }
        vistaTienda = null; identificadorClienteTienda = -1;
        try { establecerEscena(new ControladorMiCuenta(this, sesion.obtenerUsuarioActual(), servicioCliente)
                .crearVistaDirecciones()); }
        catch (Exception ex) { mostrarErrorBaseDatos(ex); }
    }

    public void mostrarPanelAdministracion() {
        if (!sesion.tieneRol(Rol.ADMIN)) {
            redirigirSegunRol();
            return;
        }
        establecerEscena(new ControladorPanelAdministracion(
                this, sesion.obtenerUsuarioActual()).crearVista());
    }

    public void cerrarSesion() {
        sesion.cerrar();
        vistaTienda = null;
        identificadorClienteTienda = -1;
        mostrarInicioSesion();
    }

    public ServicioAutenticacion obtenerServicioAutenticacion() {
        return servicioAutenticacion;
    }

    public FabricaConexiones obtenerFabricaConexiones() {
        return fabricaConexiones;
    }

    public ServicioCliente obtenerServicioCliente() { return servicioCliente; }

    public SesionUsuario obtenerSesion() {
        return sesion;
    }

    public void actualizarSesion(Usuario usuario) {
        if (!sesion.tieneRol(Rol.CLIENTE) || usuario == null || usuario.obtenerRol() != Rol.CLIENTE
                || usuario.obtenerIdentificador() != sesion.obtenerUsuarioActual().obtenerIdentificador()) {
            throw new SecurityException("Solo se puede actualizar el perfil del cliente autenticado.");
        }
        sesion.iniciar(usuario);
    }

    private void redirigirSegunRol() {
        if (sesion.tieneRol(Rol.ADMIN)) {
            mostrarPanelAdministracion();
        } else if (sesion.tieneRol(Rol.CLIENTE)) {
            mostrarTienda();
        } else {
            mostrarInicioSesion();
        }
    }

    private void establecerEscena(Parent raiz) {
        if (escena == null) {
            escena = new Scene(raiz, 1440, 900);
            escena.getStylesheets().add(getClass().getResource(
                    "/krelavo/store/resources/css/styles.css").toExternalForm());
        } else {
            escena.setRoot(raiz);
        }
        ventana.setScene(escena);
        ventana.setWidth(1440);
        ventana.setHeight(940);
        ventana.centerOnScreen();
    }

    private void mostrarErrorBaseDatos(Exception error) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Base de datos no disponible");
        alerta.setHeaderText("No se pudo preparar Krelavo Store.");
        alerta.setContentText(error.getMessage() == null
                ? "Revisa la base de datos e inténtalo de nuevo." : error.getMessage());
        alerta.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
