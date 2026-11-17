package krelavo.store.controlador;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import krelavo.store.Main;
import krelavo.store.accesodatos.RepositorioAdministracionJDBC;
import krelavo.store.modelo.DatosSeccionAdministrativa;
import krelavo.store.modelo.ResumenAdministrativo;
import krelavo.store.modelo.Rol;
import krelavo.store.modelo.Usuario;
import krelavo.store.servicio.ServicioAdministracion;

/** Panel administrativo con navegación fija y datos persistentes. */
public final class ControladorPanelAdministracion {
    private static final List<String> SECCIONES = Arrays.asList(
            "Resumen general", "Productos", "Categorías y marcas", "Inventario",
            "Pedidos", "Clientes", "Reportes");
    private final Main aplicacion;
    private final Usuario administrador;
    private final ServicioAdministracion servicio;
    private final BorderPane raiz = new BorderPane();
    private final VBox contenido = new VBox(18);
    private final Label titulo = new Label();
    private final TextField busqueda = new TextField();
    private String seccionActual = "Resumen general";
    private TableView<List<String>> tabla;

    public ControladorPanelAdministracion(Main aplicacion, Usuario administrador) {
        this.aplicacion = aplicacion;
        this.administrador = administrador;
        this.servicio = new ServicioAdministracion(
                new RepositorioAdministracionJDBC(aplicacion.obtenerFabricaConexiones()));
    }

    public Parent crearVista() {
        if (!aplicacion.obtenerSesion().tieneRol(Rol.ADMIN)) {
            aplicacion.mostrarInicioSesion();
            return new VBox();
        }
        raiz.getStyleClass().add("admin-root");
        raiz.setLeft(crearMenu());
        raiz.setTop(crearCabecera());
        contenido.getStyleClass().add("admin-content");
        contenido.setPadding(new Insets(28, 34, 34, 34));
        ScrollPane desplazamiento = new ScrollPane(contenido);
        desplazamiento.setFitToWidth(true);
        desplazamiento.getStyleClass().add("admin-scroll");
        raiz.setCenter(desplazamiento);
        mostrarSeccion("Resumen general");
        return raiz;
    }

    private VBox crearMenu() {
        VBox menu = new VBox(8);
        menu.setPadding(new Insets(26, 15, 20, 15));
        menu.setPrefWidth(245);
        menu.getStyleClass().add("admin-sidebar");
        Label marca = new Label("KRELAVO"); marca.getStyleClass().add("admin-brand");
        Label subtitulo = new Label("STORE  ·  ADMINISTRACIÓN"); subtitulo.getStyleClass().add("admin-brand-subtitle");
        VBox encabezado = new VBox(3, marca, subtitulo); encabezado.setPadding(new Insets(0, 9, 24, 9));
        menu.getChildren().add(encabezado);
        for (String seccion : SECCIONES) {
            Button boton = new Button("  " + seccion);
            boton.setMaxWidth(Double.MAX_VALUE);
            boton.setAlignment(Pos.CENTER_LEFT);
            boton.getStyleClass().add("admin-nav-button");
            boton.setOnAction(evento -> mostrarSeccion(seccion));
            menu.getChildren().add(boton);
        }
        javafx.scene.layout.Region espacio = new javafx.scene.layout.Region();
        VBox.setVgrow(espacio, Priority.ALWAYS);
        menu.getChildren().add(espacio);
        Button salir = new Button("  Cerrar sesión");
        salir.setMaxWidth(Double.MAX_VALUE); salir.setAlignment(Pos.CENTER_LEFT);
        salir.getStyleClass().add("admin-logout-button");
        salir.setOnAction(evento -> aplicacion.cerrarSesion());
        menu.getChildren().add(salir);
        return menu;
    }

    private HBox crearCabecera() {
        HBox cabecera = new HBox(15);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        cabecera.setPadding(new Insets(18, 34, 18, 34));
        cabecera.getStyleClass().add("admin-header");
        VBox titulos = new VBox(3);
        titulo.getStyleClass().add("admin-page-title");
        Label saludo = new Label("Administración de Krelavo Store"); saludo.getStyleClass().add("admin-page-subtitle");
        titulos.getChildren().addAll(titulo, saludo);
        HBox.setHgrow(titulos, Priority.ALWAYS);
        Label identidad = new Label(administrador.obtenerNombre()); identidad.getStyleClass().add("admin-user-name");
        cabecera.getChildren().addAll(titulos, identidad);
        return cabecera;
    }

    private void mostrarSeccion(String seccion) {
        if (!aplicacion.obtenerSesion().tieneRol(Rol.ADMIN)) { aplicacion.cerrarSesion(); return; }
        seccionActual = seccion;
        titulo.setText(seccion);
        contenido.getChildren().clear();
        tabla = null;
        busqueda.clear();
        if ("Resumen general".equals(seccion)) { mostrarResumen(); return; }
        Label descripcion = new Label(descripcionSeccion(seccion)); descripcion.getStyleClass().add("admin-section-copy");
        contenido.getChildren().add(descripcion);
        HBox herramientas = new HBox(10); herramientas.setAlignment(Pos.CENTER_LEFT);
        if ("Productos".equals(seccion) || "Inventario".equals(seccion)
                || "Clientes".equals(seccion) || "Pedidos".equals(seccion)) {
            busqueda.setPromptText("Buscar en " + seccion.toLowerCase(Locale.ROOT) + "…");
            busqueda.getStyleClass().add("search-input");
            HBox.setHgrow(busqueda, Priority.ALWAYS);
            busqueda.textProperty().addListener((obs, anterior, actual) -> cargarTabla(actual));
            Button actualizar = botonSecundario("Actualizar"); actualizar.setOnAction(e -> cargarTabla(busqueda.getText()));
            herramientas.getChildren().addAll(busqueda, actualizar);
        }
        if ("Productos".equals(seccion) || "Inventario".equals(seccion)) {
            if ("Productos".equals(seccion)) {
                Button nuevo = botonPrincipal("Agregar producto");
                nuevo.setOnAction(e -> crearProducto());
                herramientas.getChildren().add(nuevo);
            }
            Button accion = botonPrincipal("Productos".equals(seccion) ? "Editar producto" : "Ajustar existencias");
            accion.setOnAction(e -> {
                if ("Productos".equals(seccion)) editarSeleccionado(); else ajustarSeleccionado();
            });
            herramientas.getChildren().add(accion);
        }
        if (!herramientas.getChildren().isEmpty()) contenido.getChildren().add(herramientas);
        tabla = new TableView<List<String>>();
        tabla.getStyleClass().add("admin-table");
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        contenido.getChildren().add(tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        cargarTabla("");
    }

    private String descripcionSeccion(String seccion) {
        if ("Productos".equals(seccion)) return "Consulta el catálogo y actualiza nombre o precio. Los productos no se eliminan para conservar pedidos e historial.";
        if ("Inventario".equals(seccion)) return "Registra entradas y ajustes con trazabilidad. Cada cambio queda asociado a un movimiento de inventario.";
        if ("Categorías y marcas".equals(seccion)) return "Catálogo actual de categorías y marcas. La edición de estos registros se habilitará en una siguiente etapa.";
        if ("Pedidos".equals(seccion)) return "Pedidos almacenados en SQLite. La gestión de estados se habilitará al implementar el flujo de compras.";
        if ("Clientes".equals(seccion)) return "Cuentas de cliente registradas. El historial y la edición de cuentas se desarrollarán en su etapa correspondiente.";
        return "Indicadores calculados desde los datos persistidos. Los importes solo incluyen pedidos confirmados o completados.";
    }

    private void mostrarResumen() {
        try {
            ResumenAdministrativo r = servicio.obtenerResumen();
            HBox tarjetas = new HBox(14);
            tarjetas.getChildren().addAll(tarjeta("PRODUCTOS", String.valueOf(r.obtenerProductos()), "Productos activos"),
                    tarjeta("EXISTENCIAS", String.valueOf(r.obtenerExistencias()), "Unidades disponibles"),
                    tarjeta("PEDIDOS PENDIENTES", String.valueOf(r.obtenerPedidosPendientes()), "Por atender"),
                    tarjeta("CLIENTES", String.valueOf(r.obtenerClientes()), "Cuentas activas"),
                    tarjeta("VENTAS", formatearPesos(r.obtenerVentasCop()), "Confirmadas y completadas"));
            for (javafx.scene.Node nodo : tarjetas.getChildren()) HBox.setHgrow(nodo, Priority.ALWAYS);
            contenido.getChildren().addAll(tarjetas);
            Label tituloTabla = new Label("Inventario que requiere atención"); tituloTabla.getStyleClass().add("admin-widget-title");
            contenido.getChildren().add(tituloTabla);
            DatosSeccionAdministrativa datos = servicio.listar("Inventario", "");
            TableView<List<String>> bajo = crearTabla(datos);
            bajo.setItems(FXCollections.observableArrayList());
            for (List<String> fila : datos.obtenerFilas()) {
                try { if (Integer.parseInt(fila.get(3)) <= 5) bajo.getItems().add(fila); } catch (NumberFormatException ignorado) { }
            }
            bajo.setPrefHeight(260);
            contenido.getChildren().add(bajo);
            if (bajo.getItems().isEmpty()) {
                Label sinAlertas = new Label("No hay productos con existencias bajas según los datos actuales.");
                sinAlertas.getStyleClass().add("admin-section-copy"); contenido.getChildren().add(sinAlertas);
            }
        } catch (Exception ex) { mostrarError("No se pudo cargar el resumen: " + mensajeError(ex)); }
    }

    private VBox tarjeta(String etiqueta, String valor, String detalle) {
        VBox tarjeta = new VBox(10); tarjeta.setPadding(new Insets(18)); tarjeta.setMinWidth(145);
        tarjeta.getStyleClass().add("admin-metric-card");
        Label nombre = new Label(etiqueta); nombre.getStyleClass().add("admin-metric-label");
        Label numero = new Label(valor); numero.getStyleClass().add("admin-metric-value");
        Label apoyo = new Label(detalle); apoyo.getStyleClass().add("admin-metric-detail"); apoyo.setWrapText(true);
        tarjeta.getChildren().addAll(nombre, numero, apoyo); return tarjeta;
    }

    private void cargarTabla(String filtro) {
        if (tabla == null) return;
        try {
            DatosSeccionAdministrativa datos = servicio.listar(seccionActual, filtro);
            TableView<List<String>> nueva = crearTabla(datos);
            tabla.getColumns().setAll(nueva.getColumns());
            tabla.setItems(nueva.getItems());
        } catch (Exception ex) { mostrarError("No se pudieron cargar los datos: " + mensajeError(ex)); }
    }

    private TableView<List<String>> crearTabla(DatosSeccionAdministrativa datos) {
        TableView<List<String>> resultado = new TableView<List<String>>();
        resultado.getStyleClass().add("admin-table");
        resultado.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        for (int i=0;i<datos.obtenerEncabezados().size();i++) {
            final int indice=i;
            TableColumn<List<String>,String> columna=new TableColumn<List<String>,String>(datos.obtenerEncabezados().get(i));
            columna.setCellValueFactory(celda -> new SimpleStringProperty(celda.getValue().get(indice)));
            resultado.getColumns().add(columna);
        }
        resultado.setItems(FXCollections.observableArrayList(datos.obtenerFilas()));
        return resultado;
    }

    private void editarSeleccionado() {
        List<String> fila=seleccionada(); if(fila==null)return;
        Dialog<ButtonType> dialogo=new Dialog<ButtonType>(); dialogo.setTitle("Editar producto"); dialogo.setHeaderText("Actualiza la información del catálogo");
        TextField nombre=new TextField(fila.get(1)); TextField precio=new TextField(fila.get(4));
        VBox campos=new VBox(8,new Label("Nombre"),nombre,new Label("Precio en pesos colombianos"),precio); campos.setPadding(new Insets(12)); dialogo.getDialogPane().setContent(campos);
        dialogo.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL,ButtonType.OK);
        dialogo.showAndWait().ifPresent(respuesta->{if(respuesta==ButtonType.OK)try{servicio.actualizarProducto(Long.parseLong(fila.get(0)),nombre.getText(),precio.getText());cargarTabla(busqueda.getText());}catch(Exception ex){mostrarError(mensajeError(ex));}});
    }

    private void crearProducto() {
        Dialog<ButtonType> dialogo=new Dialog<ButtonType>();
        dialogo.setTitle("Agregar producto");
        dialogo.setHeaderText("El nuevo producto inicia con cero existencias");
        TextField nombre=new TextField();nombre.setPromptText("Nombre del producto");
        TextField precio=new TextField();precio.setPromptText("Precio en pesos colombianos");
        ComboBox<String> marca=new ComboBox<String>();
        ComboBox<String> categoria=new ComboBox<String>();
        try {
            DatosSeccionAdministrativa opciones=servicio.listar("Categorías y marcas","");
            for(List<String> opcion:opciones.obtenerFilas()) {
                if("Marca".equals(opcion.get(0))&&"Activa".equals(opcion.get(2)))marca.getItems().add(opcion.get(1));
                if("Categoría".equals(opcion.get(0))&&"Activa".equals(opcion.get(2)))categoria.getItems().add(opcion.get(1));
            }
        } catch(Exception ex) { mostrarError("No se pudieron cargar marcas y categorías: "+mensajeError(ex));return; }
        marca.setPromptText("Selecciona una marca");categoria.setPromptText("Selecciona una categoría");
        VBox campos=new VBox(8,new Label("Nombre"),nombre,new Label("Marca"),marca,
                new Label("Categoría"),categoria,new Label("Precio en pesos colombianos"),precio);
        campos.setPadding(new Insets(12));dialogo.getDialogPane().setContent(campos);
        dialogo.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL,ButtonType.OK);
        dialogo.showAndWait().ifPresent(respuesta->{if(respuesta==ButtonType.OK)try{
            servicio.crearProducto(nombre.getText(),marca.getValue(),categoria.getValue(),precio.getText());
            cargarTabla(busqueda.getText());
        }catch(Exception ex){mostrarError(mensajeError(ex));}});
    }

    private void ajustarSeleccionado() {
        List<String> fila=seleccionada(); if(fila==null)return;
        Dialog<ButtonType> dialogo=new Dialog<ButtonType>(); dialogo.setTitle("Ajustar existencias"); dialogo.setHeaderText(fila.get(1));
        TextField cantidad=new TextField();cantidad.setPromptText("Ejemplo: 10 para entrada, -2 para ajuste");
        TextField nota=new TextField();nota.setPromptText("Motivo del movimiento");
        VBox campos=new VBox(8,new Label("Cambio de unidades"),cantidad,new Label("Nota"),nota);campos.setPadding(new Insets(12));dialogo.getDialogPane().setContent(campos);
        dialogo.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL,ButtonType.OK);
        dialogo.showAndWait().ifPresent(respuesta->{if(respuesta==ButtonType.OK)try{servicio.ajustarExistencias(Long.parseLong(fila.get(0)),cantidad.getText(),nota.getText(),administrador.obtenerIdentificador());cargarTabla(busqueda.getText());}catch(Exception ex){mostrarError(mensajeError(ex));}});
    }

    private List<String> seleccionada() {
        if(tabla==null||tabla.getSelectionModel().getSelectedItem()==null){mostrarError("Selecciona primero un registro de la tabla.");return null;}
        return tabla.getSelectionModel().getSelectedItem();
    }
    private Button botonPrincipal(String texto){Button b=new Button(texto);b.getStyleClass().add("admin-action-button");return b;}
    private Button botonSecundario(String texto){Button b=new Button(texto);b.getStyleClass().add("admin-refresh-button");return b;}
    private String formatearPesos(long valor){NumberFormat f=NumberFormat.getCurrencyInstance(new Locale("es","CO"));f.setMaximumFractionDigits(0);return f.format(valor);}
    private String mensajeError(Throwable ex){return ex.getMessage()==null?"Error inesperado.":ex.getMessage();}
    private void mostrarError(String texto){Alert a=new Alert(Alert.AlertType.ERROR);a.setTitle("Panel administrativo");a.setHeaderText(null);a.setContentText(texto);a.showAndWait();}
}
