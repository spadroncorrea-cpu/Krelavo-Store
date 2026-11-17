package krelavo.store.controlador;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import krelavo.store.Main;
import krelavo.store.modelo.Producto;
import krelavo.store.modelo.CatalogoProductos;
import krelavo.store.modelo.Rol;
import krelavo.store.servicio.ServicioCliente;
import java.sql.SQLException;

/** Catálogo cosmético con búsqueda, filtros, favoritos y contador local del carrito. */
public class ControladorCatalogo {
    private static final String FACIAL = CatalogoProductos.FACIAL;
    private static final String CAPILAR = CatalogoProductos.CAPILAR;
    private static final String TODAS_LAS_CATEGORIAS = "Todas las categorías";
    private static final String TODAS_LAS_MARCAS = "Todas las marcas";
    private final Main aplicacion;
    private List<Producto> productos = new ArrayList<Producto>();
    private final List<TarjetaProducto> cards = new ArrayList<TarjetaProducto>();
    private ScrollPane desplazamiento;
    private VBox contenido, destacados, capilar;
    private GridPane cuadriculaFacial, cuadriculaCapilar;
    private TextField busqueda;
    private ComboBox<String> filtroCategoria, filtroMarca, filtroPrecio;
    private Button navegacionFavoritos, botonCarrito;
    private Label mensajeFacialVacio, mensajeCapilarVacio;
    private boolean soloFavoritos;
    private int cantidadArticulosCarrito;
    private final ServicioCliente servicio;
    private final long usuarioId;

    public ControladorCatalogo(Main aplicacion) {
        this.aplicacion = aplicacion;
        this.servicio = aplicacion.obtenerServicioCliente();
        this.usuarioId = aplicacion.obtenerSesion().obtenerUsuarioActual().obtenerIdentificador();
    }

    public Parent crearVista() throws SQLException {
        productos = servicio.listarProductos();
        List<Producto> favoritos = servicio.listarFavoritos(usuarioId);
        java.util.Set<Long> idsFavoritos = new java.util.HashSet<Long>();
        for (Producto favorito : favoritos) idsFavoritos.add(favorito.obtenerIdentificador());
        for (Producto producto : productos) producto.establecerFavorito(idsFavoritos.contains(producto.obtenerIdentificador()));
        cantidadArticulosCarrito = servicio.obtenerUnidadesCarrito(usuarioId);
        BorderPane raiz = new BorderPane();
        raiz.getStyleClass().add("catalog-screen");
        raiz.setMinWidth(1024);
        contenido = new VBox(0);
        contenido.getStyleClass().add("catalog-content");
        contenido.setMinWidth(1024);
        contenido.setPrefWidth(1400);
        contenido.setMaxWidth(Double.MAX_VALUE);
        contenido.getChildren().addAll(barraPromocional(), encabezado(), separador(), bannerBienvenida(),
                seccionBusqueda(), seccionCategorias(), seccionMarcas(), seccionDestacados(), seccionCapilar(), piePagina());
        desplazamiento = new ScrollPane(contenido);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setPannable(true);
        desplazamiento.getStyleClass().add("catalog-scroll");
        raiz.setCenter(desplazamiento);
        return raiz;
    }

    private HBox barraPromocional() {
        HBox barra = new HBox();
        barra.setAlignment(Pos.CENTER);
        barra.setMinHeight(32);
        barra.setPrefHeight(32);
        barra.getStyleClass().add("promo-bar");
        Label mensaje = new Label("Descubre tus esenciales para cada ritual de belleza");
        mensaje.getStyleClass().add("promo-message");
        barra.getChildren().add(mensaje);
        return barra;
    }

    private VBox encabezado() {
        VBox encabezado = new VBox();
        encabezado.getStyleClass().add("catalog-header");
        HBox fila = new HBox(24);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(18, 70, 18, 70));

        VBox marca = new VBox(3);
        Label nombre = new Label("KRELAVO STORE");
        nombre.getStyleClass().add("catalog-marca-title");
        Label tagline = new Label("TU BELLEZA, TU ESENCIA");
        tagline.getStyleClass().add("catalog-marca-tagline");
        marca.getChildren().addAll(nombre, tagline);

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);
        HBox navegacion = new HBox(18);
        navegacion.setAlignment(Pos.CENTER_RIGHT);
        Button inicio = botonNavegacion("Inicio");
        Button producto = botonNavegacion("Productos");
        navegacionFavoritos = botonNavegacion("Favoritos");
        botonCarrito = botonNavegacion("Carrito · 0");
        Button cuenta = botonNavegacion("Mi cuenta");
        Button cerrarSesion = botonNavegacion("Cerrar sesión");
        inicio.setOnAction(e -> desplazarA(0));
        producto.setOnAction(e -> desplazarASeccion(destacados));
        navegacionFavoritos.setOnAction(e -> aplicacion.mostrarFavoritos());
        botonCarrito.setOnAction(e -> aplicacion.mostrarCarrito());
        cuenta.setOnAction(e -> aplicacion.mostrarMiCuenta());
        cerrarSesion.setOnAction(e -> aplicacion.cerrarSesion());
        navegacion.getChildren().addAll(inicio, producto, navegacionFavoritos, botonCarrito, cuenta, cerrarSesion);
        fila.getChildren().addAll(marca, espaciador, navegacion);
        encabezado.getChildren().add(fila);
        return encabezado;
    }

    private Line separador() {
        Line line = new Line(0, 0, 1400, 0);
        line.getStyleClass().add("header-divider");
        line.endXProperty().bind(contenido.widthProperty());
        return line;
    }

    private VBox bannerBienvenida() {
        VBox banner = new VBox(7);
        banner.setPadding(new Insets(30, 76, 24, 76));
        banner.getStyleClass().add("welcome-banner");
        Label eyebrow = new Label("KRELAVO · CUIDADO PERSONAL");
        eyebrow.getStyleClass().add("banner-eyebrow");
        Label titulo = new Label("Tu ritual, a tu manera.");
        titulo.getStyleClass().add("banner-title");
        Label body = new Label("Encuentra productos para cuidar tu piel y tu cabello.");
        body.getStyleClass().add("banner-copy");
        banner.getChildren().addAll(eyebrow, titulo, body);
        return banner;
    }

    private VBox seccionBusqueda() {
        VBox seccion = new VBox(12);
        seccion.setPadding(new Insets(24, 76, 0, 76));
        Label titulo = tituloSeccion("Encuentra tus favoritos");
        busqueda = new TextField();
        busqueda.setPromptText("Buscar por producto, marca o precio...");
        busqueda.getStyleClass().add("search-input");
        busqueda.setMaxWidth(Double.MAX_VALUE);
        busqueda.textProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);
        filtroCategoria = crearSelector(TODAS_LAS_CATEGORIAS, Arrays.asList(TODAS_LAS_CATEGORIAS, FACIAL, CAPILAR));
        filtroCategoria.setId("category-filter");
        List<String> marcas = new ArrayList<String>();
        marcas.add(TODAS_LAS_MARCAS);

        for (Producto producto : productos) {
        if (!producto.estaPendiente() && !marcas.contains(producto.obtenerMarca())) {
        marcas.add(producto.obtenerMarca());
    }
}

        filtroMarca = crearSelector(TODAS_LAS_MARCAS, marcas);
        filtroMarca.setId("marca-filter");
        filtroPrecio = crearSelector("Todos los precios", Arrays.asList("Todos los precios", "Hasta $40.000",
                "$40.001 – $55.000", "Más de $55.000"));
        filtroPrecio.setId("precio-filter");
        filtroCategoria.setOnAction(e -> { limpiarModoFavoritos(); aplicarFiltros(); });
        filtroMarca.setOnAction(e -> { limpiarModoFavoritos(); aplicarFiltros(); });
        filtroPrecio.setOnAction(e -> { limpiarModoFavoritos(); aplicarFiltros(); });
        filters.getChildren().addAll(filtroCategoria, filtroMarca, filtroPrecio);
        seccion.getChildren().addAll(titulo, busqueda, filters);
        return seccion;
    }

    private ComboBox<String> crearSelector(String prompt, List<String> values) {
        ComboBox<String> crearSelector = new ComboBox<String>();
        crearSelector.getItems().addAll(values);
        crearSelector.setValue(prompt);
        crearSelector.getStyleClass().add("filter-select");
        crearSelector.setPrefWidth(225);
        crearSelector.setMaxWidth(260);
        return crearSelector;
    }

    private VBox seccionCategorias() {
        VBox seccion = contenedorSeccion();
        Label titulo = tituloSeccion("Compra por categoría");
        HBox fila = new HBox(18);
        fila.getChildren().addAll(tarjetaCategoria("Cuidado facial", "Esenciales para tu rutina facial", FACIAL),
                tarjetaCategoria("Cuidado capilar", "Productos para tu rutina capilar", CAPILAR));
        HBox.setHgrow(fila.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(fila.getChildren().get(1), Priority.ALWAYS);
        seccion.getChildren().addAll(titulo, fila);
        return seccion;
    }

    private VBox tarjetaCategoria(String titulo, String subtitulo, final String valor) {
        VBox tarjeta = new VBox(7);
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.setPadding(new Insets(19, 24, 19, 24));
        tarjeta.setPrefHeight(92);
        tarjeta.setMaxWidth(Double.MAX_VALUE);
        tarjeta.getStyleClass().add("category-tarjeta");
        Label heading = new Label(titulo);
        heading.getStyleClass().add("category-title");
        Label detail = new Label(subtitulo);
        detail.getStyleClass().add("category-subtitle");
        tarjeta.getChildren().addAll(heading, detail);
        tarjeta.setCursor(Cursor.HAND);
        tarjeta.setOnMouseClicked(e -> {
            limpiarModoFavoritos();
            filtroCategoria.setValue(valor);
            filtroMarca.setValue(TODAS_LAS_MARCAS);
            filtroPrecio.setValue("Todos los precios");
            busqueda.clear();
            aplicarFiltros();
            desplazarASeccion(valor.equals(CAPILAR) ? capilar : destacados);
        });
        return tarjeta;
    }

    private VBox seccionMarcas() {
    VBox seccion = contenedorSeccion();

    Label titulo = tituloSeccion("Buscar por marca");

    Label subtitulo = new Label(
        "Descubre tus marcas favoritas de cuidado facial y capilar"
    );
    subtitulo.getStyleClass().add("section-subtitle");

    GridPane cuadricula = new GridPane();
    cuadricula.setHgap(14);
    cuadricula.setVgap(14);
    cuadricula.setMaxWidth(Double.MAX_VALUE);

    int column = 0;
    int fila = 0;

    for (Producto producto : productos) {
        if (producto.estaPendiente()) {
            continue;
        }

        String nombreMarca = producto.obtenerMarca();
        boolean exists = false;

        for (javafx.scene.Node node : cuadricula.getChildren()) {
            VBox existingCard = (VBox) node;
            Label existingTitle = (Label) existingCard.getChildren().get(0);

            if (existingTitle.getText().equals(nombreMarca)) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            VBox tarjeta = tarjetaMarca(nombreMarca, "Explora sus productos");
            cuadricula.add(tarjeta, column, fila);

            column++;

            if (column == 4) {
                column = 0;
                fila++;
            }
        }
    }

    for (int i = 0; i < 4; i++) {
        javafx.scene.layout.ColumnConstraints constraints =
            new javafx.scene.layout.ColumnConstraints();

        constraints.setPercentWidth(25);
        constraints.setHgrow(Priority.ALWAYS);
        cuadricula.getColumnConstraints().add(constraints);
    }

    seccion.getChildren().addAll(titulo, subtitulo, cuadricula);

    return seccion;
}

    private VBox tarjetaMarca(final String nombreMarca, String descriptor) {
        VBox tarjeta = new VBox(6);
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.setPadding(new Insets(17, 18, 17, 18));
        tarjeta.setPrefHeight(78);
        tarjeta.setMaxWidth(Double.MAX_VALUE);
        tarjeta.getStyleClass().add("marca-tarjeta");
        Label titulo = new Label(nombreMarca);
        titulo.getStyleClass().add("marca-tarjeta-title");
        Label detail = new Label(descriptor);
        detail.getStyleClass().add("marca-tarjeta-description");
        tarjeta.getChildren().addAll(titulo, detail);
        tarjeta.setCursor(Cursor.HAND);
        tarjeta.setOnMouseClicked(e -> {
            limpiarModoFavoritos();
            filtroCategoria.setValue(TODAS_LAS_CATEGORIAS);
            filtroPrecio.setValue("Todos los precios");
            filtroMarca.setValue(nombreMarca);
            busqueda.clear();
            aplicarFiltros();
            desplazarASeccion(destacados);
        });
        return tarjeta;
    }

    private VBox seccionDestacados() {
    destacados = contenedorSeccion();

    Label titulo = tituloSeccion("Productos destacados");
    Label subtitulo = new Label("Cuidado facial");
    subtitulo.getStyleClass().add("section-subtitle");

    cuadriculaFacial = cuadriculaProductos(FACIAL);
    mensajeFacialVacio = mensajeVacio();

    destacados.getChildren().addAll(titulo, subtitulo, cuadriculaFacial, mensajeFacialVacio);

    return destacados;
}

    private VBox seccionCapilar() {
        capilar = contenedorSeccion();
        Label titulo = tituloSeccion("Cuidado capilar");
        Label subtitulo = new Label("Esenciales para tu rutina de cabello");
        subtitulo.getStyleClass().add("section-subtitle");
        
        cuadriculaCapilar = cuadriculaProductos(CAPILAR);
        mensajeCapilarVacio = mensajeVacio();
        
        capilar.getChildren().addAll(titulo, subtitulo, cuadriculaCapilar, mensajeCapilarVacio);
        return capilar;
    }

    private GridPane cuadriculaProductos(String tipo) {
        GridPane cuadricula = new GridPane();
        cuadricula.setHgap(18);
        cuadricula.setVgap(18);
        cuadricula.setMaxWidth(Double.MAX_VALUE);
        int column = 0;
        int fila = 0;
        for (Producto producto : productos) {
            if (!tipo.equals(producto.obtenerCategoria())) continue;
            TarjetaProducto tarjeta = new TarjetaProducto(producto);
            cards.add(tarjeta);
            cuadricula.add(tarjeta.raiz, column, fila);
            column++;
            if (column == 4) { column = 0; fila++; }
        }
        for (int i = 0; i < 4; i++) {
            javafx.scene.layout.ColumnConstraints constraints = new javafx.scene.layout.ColumnConstraints();
            constraints.setPercentWidth(25);
            constraints.setHgrow(Priority.ALWAYS);
            cuadricula.getColumnConstraints().add(constraints);
        }
        return cuadricula;
    }

    private VBox contenedorSeccion() {
        VBox seccion = new VBox(12);
        seccion.setPadding(new Insets(34, 76, 0, 76));
        seccion.getStyleClass().add("catalog-section");
        return seccion;
    }

    private Label tituloSeccion(String texto) {
        Label titulo = new Label(texto);
        titulo.getStyleClass().add("section-title");
        return titulo;
    }

    private Label mensajeVacio() {
        Label etiqueta = new Label("No hay productos que coincidan. Las fichas pendientes necesitan datos e imagen.");
        etiqueta.getStyleClass().add("empty-message");
        etiqueta.setVisible(false);
        etiqueta.setManaged(false);
        return etiqueta;
    }

    private HBox piePagina() {
        HBox piePagina = new HBox();
        piePagina.setAlignment(Pos.CENTER_LEFT);
        piePagina.setPadding(new Insets(26, 76, 28, 76));
        piePagina.getStyleClass().add("catalog-footer");
        Label marca = new Label("KRELAVO STORE  ·  Tu belleza, tu esencia");
        marca.getStyleClass().add("footer-marca");
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);
        Label line = new Label("Cosmética y cuidado personal");
        line.getStyleClass().add("footer-copy");
        piePagina.getChildren().addAll(marca, espaciador, line);
        return piePagina;
    }

    private Button botonNavegacion(String texto) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("nav-button");
        return boton;
    }

    private void establecerActivo(Button boton, boolean activo) {
        boton.getStyleClass().remove("nav-active");
        if (activo) boton.getStyleClass().add("nav-active");
    }

    private void limpiarModoFavoritos() {
        soloFavoritos = false;
        if (navegacionFavoritos != null) establecerActivo(navegacionFavoritos, false);
    }

    private void aplicarFiltros() {
        if (busqueda == null) return;
        String query = busqueda.getText() == null ? "" : busqueda.getText().trim().toLowerCase();
        String categoriaSeleccionada = filtroCategoria.getValue();
        String marcaSeleccionada = filtroMarca.getValue();
        String precioSeleccionado = filtroPrecio.getValue();
        int cantidadFacial = 0;
        int cantidadCapilar = 0;
        for (TarjetaProducto tarjeta : cards) {
            Producto producto = tarjeta.producto;
            String textoBuscable = (producto.obtenerNombre() + " " + producto.obtenerMarca() + " "
                    + producto.obtenerCategoria() + " " + producto.obtenerPrecioFormateado()).toLowerCase();
            boolean textMatch = query.isEmpty() || textoBuscable.contains(query);
            boolean coincideCategoria = TODAS_LAS_CATEGORIAS.equals(categoriaSeleccionada) || producto.obtenerCategoria().equals(categoriaSeleccionada);
            boolean coincideMarca = TODAS_LAS_MARCAS.equals(marcaSeleccionada) || producto.obtenerMarca().equals(marcaSeleccionada);
            boolean coincidePrecio = coincidePrecio(producto, precioSeleccionado);
            boolean visible = textMatch && coincideCategoria && coincideMarca && coincidePrecio
                    && (!soloFavoritos || producto.esFavorito());
            tarjeta.raiz.setVisible(visible);
            tarjeta.raiz.setManaged(visible);
            if (visible && FACIAL.equals(producto.obtenerCategoria())) cantidadFacial++;
            if (visible && CAPILAR.equals(producto.obtenerCategoria())) cantidadCapilar++;
        }
        mostrarVacio(mensajeFacialVacio, cantidadFacial == 0);
        mostrarVacio(mensajeCapilarVacio, cantidadCapilar == 0);
        
        reorganizarCuadricula(cuadriculaFacial, FACIAL);
        reorganizarCuadricula(cuadriculaCapilar, CAPILAR);
    }
    
    private void reorganizarCuadricula(GridPane cuadricula, String categoria) {
    cuadricula.getChildren().clear();

    int column = 0;
    int fila = 0;

    for (TarjetaProducto tarjeta : cards) {
        if (categoria.equals(tarjeta.producto.obtenerCategoria())
                && tarjeta.raiz.isVisible()) {

            tarjeta.raiz.setManaged(true);
            cuadricula.add(tarjeta.raiz, column, fila);

            column++;

            if (column == 4) {
                column = 0;
                fila++;
            }
        }
    }
}

    private boolean coincidePrecio(Producto producto, String seleccionado) {
        if (seleccionado == null || "Todos los precios".equals(seleccionado)) return true;
        if (producto.estaPendiente()) return false;
        long precio = producto.obtenerPrecioCop();
        if ("Hasta $40.000".equals(seleccionado)) return precio <= 40000;
        if ("$40.001 – $55.000".equals(seleccionado)) return precio >= 40001 && precio <= 55000;
        if ("Más de $55.000".equals(seleccionado)) return precio > 55000;
        return true;
    }

    private void mostrarVacio(Label etiqueta, boolean mostrar) {
        etiqueta.setVisible(mostrar);
        etiqueta.setManaged(mostrar);
    }

    private void desplazarA(double valor) {
        if (desplazamiento != null) {
            desplazamiento.setVvalue(valor);
            desplazamiento.setHvalue(0);
        }
    }

    private void desplazarASeccion(final VBox seccion) {
        if (seccion == null || desplazamiento == null) return;
        Platform.runLater(() -> {
            double total = contenido.getBoundsInLocal().getHeight();
            double vistaImagen = desplazamiento.getViewportBounds().getHeight();
            if (total > vistaImagen) {
                double y = seccion.getBoundsInParent().getMinY();
                desplazamiento.setVvalue(Math.max(0, Math.min(1, y / (total - vistaImagen))));
            }
        });
    }

    private void mostrarAviso(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private final class TarjetaProducto {
        private final Producto producto;
        private final VBox raiz = new VBox(0);

        TarjetaProducto(Producto producto) {
            this.producto = producto;
            raiz.setPrefSize(282, 398);
            raiz.setMinSize(0, 398);
            raiz.setMaxWidth(Double.MAX_VALUE);
            raiz.getStyleClass().add("product-card");

            StackPane fotografia = new StackPane();
            fotografia.setPrefHeight(228);
            fotografia.setMinHeight(228);
            fotografia.setMaxHeight(228);
            fotografia.getStyleClass().add("product-photo");
            if (producto.estaPendiente()) {
                VBox pendiente = new VBox(7);
                pendiente.setAlignment(Pos.CENTER);
                Label marca = new Label("＋");
                marca.getStyleClass().add("pending-mark");
                Label tituloPendiente = new Label("Fotografía pendiente");
                tituloPendiente.getStyleClass().add("pending-title");
                Label detallePendiente = new Label("Agregar archivo PNG o JPG");
                detallePendiente.getStyleClass().add("pending-detail");
                pendiente.getChildren().addAll(marca, tituloPendiente, detallePendiente);
                fotografia.getChildren().add(pendiente);
                raiz.getStyleClass().add("product-pending");
            } else if (producto.obtenerRutaImagen() != null) {
                Image imagen = cargarImagen(producto.obtenerRutaImagen());
                ImageView vistaImagen = new ImageView(imagen);
                vistaImagen.setPreserveRatio(true);
                vistaImagen.setFitWidth(242);
                vistaImagen.setFitHeight(208);
                vistaImagen.getStyleClass().add("product-image");
                fotografia.getChildren().add(vistaImagen);
            } else {
                Label pendiente = new Label("Imagen pendiente");
                pendiente.getStyleClass().add("pending-detail");
                fotografia.getChildren().add(pendiente);
            }

            Button corazon = new Button(producto.esFavorito() ? "♥" : "♡");
            corazon.getStyleClass().add("favorite-button");
            StackPane.setAlignment(corazon, Pos.TOP_RIGHT);
            StackPane.setMargin(corazon, new Insets(10, 10, 0, 0));
            corazon.setTooltip(new Tooltip("Agregar a favoritos"));
            corazon.setOnAction(e -> {
                try {
                    boolean favorito = servicio.cambiarFavorito(usuarioId, producto.obtenerIdentificador());
                    producto.establecerFavorito(favorito);
                } catch (SQLException ex) {
                    mostrarAviso("Favoritos", "No se pudo actualizar la lista de deseos: " + ex.getMessage());
                    return;
                }
                corazon.setText(producto.esFavorito() ? "♥" : "♡");
                corazon.getStyleClass().remove("favorite-selected");
                if (producto.esFavorito()) corazon.getStyleClass().add("favorite-selected");
                corazon.setTooltip(new Tooltip(producto.esFavorito() ? "Quitar de favoritos" : "Agregar a favoritos"));
                aplicarFiltros();
            });
            fotografia.getChildren().add(corazon);

            VBox detalles = new VBox(5);
            detalles.setPadding(new Insets(13, 16, 16, 16));
            Label nombre = new Label(producto.obtenerNombre());
            nombre.getStyleClass().add("product-name");
            nombre.setWrapText(true);
            nombre.setMinHeight(36);
            Label marca = new Label(producto.obtenerMarca());
            marca.getStyleClass().add("product-brand");
            Label precio = new Label(producto.obtenerPrecioFormateado());
            precio.getStyleClass().add("product-price");
            Label existencias = new Label(producto.obtenerCantidadExistencias() > 0
                    ? "Disponibles: " + producto.obtenerCantidadExistencias() : "Agotado");
            existencias.getStyleClass().add("product-brand");
            Button agregar = new Button(producto.estaPendiente() ? "Foto y datos pendientes"
                    : producto.obtenerCantidadExistencias() > 0 ? "Agregar al carrito" : "Agotado");
            agregar.getStyleClass().add("product-add-button");
            agregar.setMaxWidth(Double.MAX_VALUE);
            agregar.setPrefHeight(36);
            agregar.setDisable(producto.estaPendiente() || producto.obtenerCantidadExistencias() < 1);
            if (!producto.estaPendiente() && producto.obtenerCantidadExistencias() > 0) {
                agregar.setOnAction(e -> {
                    try {
                        servicio.agregarAlCarrito(usuarioId, producto.obtenerIdentificador(), 1);
                        cantidadArticulosCarrito = servicio.obtenerUnidadesCarrito(usuarioId);
                        botonCarrito.setText("Carrito · " + cantidadArticulosCarrito);
                        agregar.setText("Agregado ✓");
                        agregar.setTooltip(new Tooltip(producto.obtenerNombre() + " agregado. Artículos en el carrito: " + cantidadArticulosCarrito));
                        PauseTransition restablecer = new PauseTransition(Duration.millis(1800));
                        restablecer.setOnFinished(terminado -> agregar.setText("Agregar al carrito"));
                        restablecer.play();
                    } catch (Exception ex) {
                        mostrarAviso("Carrito", ex.getMessage() == null ? "No se pudo agregar el producto." : ex.getMessage());
                    }
                });
            }
            detalles.getChildren().addAll(nombre, marca, precio, existencias, agregar);
            raiz.getChildren().addAll(fotografia, detalles);
        }

        private Image cargarImagen(String ruta) {
            InputStream flujo = getClass().getResourceAsStream(ruta);
            if (flujo == null) throw new IllegalStateException("Falta la imagen original: " + ruta);
            try {
                Image imagen = new Image(flujo);
                if (imagen.isError() || imagen.getWidth() <= 0 || imagen.getHeight() <= 0) {
                    throw new IllegalStateException("Imagen inválida: " + ruta);
                }
                return imagen;
            } finally {
                try { flujo.close(); } catch (Exception ignored) { }
            }
        }
    }
}
