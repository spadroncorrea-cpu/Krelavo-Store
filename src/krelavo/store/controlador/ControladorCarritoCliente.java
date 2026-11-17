package krelavo.store.controlador;

import java.io.InputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import krelavo.store.Main;
import krelavo.store.modelo.DireccionCliente;
import krelavo.store.modelo.LineaCarrito;
import krelavo.store.modelo.Usuario;
import krelavo.store.servicio.ServicioCliente;
import krelavo.store.vista.ComponentesCliente;

/** Pantalla independiente del carrito y confirmación de compra del cliente. */
public final class ControladorCarritoCliente {
    private final Main aplicacion;private final Usuario usuario;private final ServicioCliente servicio;
    private List<LineaCarrito> lineas;private List<DireccionCliente> direcciones;
    public ControladorCarritoCliente(Main app,Usuario usuario,ServicioCliente servicio){this.aplicacion=app;this.usuario=usuario;this.servicio=servicio;}

    public Parent crearVista() throws Exception {
        lineas=servicio.obtenerCarrito(usuario.obtenerIdentificador());direcciones=servicio.listarDirecciones(usuario.obtenerIdentificador());
        BorderPane raiz=ComponentesCliente.crearMarco(aplicacion,"Carrito");
        VBox contenido=new VBox(21);contenido.setPadding(new Insets(34,58,54,58));contenido.getStyleClass().add("cliente-contenido");
        Label migas=new Label("TIENDA   /   CARRITO DE COMPRAS");migas.getStyleClass().add("cliente-migas");
        Label titulo=new Label("Tu carrito");titulo.getStyleClass().add("cliente-titulo");
        Label subtitulo=new Label(lineas.isEmpty()?"Tus productos seleccionados aparecerán aquí.":"Revisa tus productos antes de continuar.");subtitulo.getStyleClass().add("cliente-subtitulo");
        HBox columnas=new HBox(22);VBox lista=new VBox(12);lista.getStyleClass().add("cliente-lista-carrito");
        if(lineas.isEmpty()){
            VBox vacio=new VBox(12);vacio.setAlignment(Pos.CENTER);vacio.setPadding(new Insets(45));vacio.getStyleClass().add("cliente-tarjeta");
            Label mensaje=new Label("Tu carrito está esperando tus favoritos.");mensaje.getStyleClass().add("cliente-subtitulo");Button comprar=boton("Explorar productos");comprar.setOnAction(e->aplicacion.mostrarTienda());vacio.getChildren().addAll(mensaje,comprar);lista.getChildren().add(vacio);
        }else for(LineaCarrito linea:lineas)lista.getChildren().add(crearFila(linea));
        HBox.setHgrow(lista,Priority.ALWAYS);lista.setMaxWidth(Double.MAX_VALUE);
        VBox resumen=crearResumen();resumen.setPrefWidth(355);resumen.setMinWidth(320);columnas.getChildren().addAll(lista,resumen);
        contenido.getChildren().addAll(migas,titulo,subtitulo,columnas);
        ScrollPane scroll=new ScrollPane(contenido);scroll.setFitToWidth(true);scroll.getStyleClass().add("cliente-scroll");raiz.setCenter(scroll);return raiz;
    }

    private HBox crearFila(LineaCarrito linea){
        HBox fila=new HBox(18);fila.setAlignment(Pos.CENTER_LEFT);fila.setPadding(new Insets(16));fila.getStyleClass().add("cliente-tarjeta");
        ImageView imagen=imagen(linea.obtenerProducto().obtenerRutaImagen());
        VBox datos=new VBox(7);HBox.setHgrow(datos,Priority.ALWAYS);
        Label nombre=new Label(linea.obtenerProducto().obtenerNombre());nombre.getStyleClass().add("cliente-producto-nombre");nombre.setWrapText(true);
        Label marca=new Label(linea.obtenerProducto().obtenerMarca());marca.getStyleClass().add("cliente-subtitulo");
        Label precio=new Label(pesos(linea.obtenerProducto().obtenerPrecioCop())+" por unidad");precio.getStyleClass().add("cliente-precio");
        Label existencia=new Label("Disponibles: "+linea.obtenerProducto().obtenerCantidadExistencias());existencia.getStyleClass().add("cliente-migas");
        datos.getChildren().addAll(nombre,marca,precio,existencia);
        HBox cantidad=new HBox(8);cantidad.setAlignment(Pos.CENTER);
        Button menos=botonSecundario("−");menos.setDisable(linea.obtenerCantidad()<=1);menos.setOnAction(e->cambiarCantidad(linea,-1));
        Label numero=new Label(String.valueOf(linea.obtenerCantidad()));numero.getStyleClass().add("cliente-cantidad");
        Button mas=botonSecundario("+");mas.setDisable(linea.obtenerCantidad()>=linea.obtenerProducto().obtenerCantidadExistencias());mas.setOnAction(e->cambiarCantidad(linea,1));
        cantidad.getChildren().addAll(menos,numero,mas);
        VBox valores=new VBox(6);valores.setAlignment(Pos.CENTER_RIGHT);Label total=new Label(pesos(linea.obtenerSubtotal()));total.getStyleClass().add("cliente-precio");Button eliminar=botonTexto("Quitar");eliminar.setOnAction(e->quitar(linea));valores.getChildren().addAll(total,eliminar);
        fila.getChildren().addAll(imagen,datos,cantidad,valores);return fila;
    }

    private VBox crearResumen(){
        VBox caja=new VBox(15);caja.setPadding(new Insets(23));caja.getStyleClass().add("cliente-tarjeta");
        Label titulo=new Label("Resumen de compra");titulo.getStyleClass().add("cliente-subtitulo-fuerte");
        long subtotal=0;for(LineaCarrito l:lineas)subtotal=Math.addExact(subtotal,Math.multiplyExact(l.obtenerProducto().obtenerPrecioCop(),l.obtenerCantidad()));
        Label cuenta=new Label("Subtotal   "+pesos(subtotal));cuenta.getStyleClass().add("cliente-resumen-linea");
        Label envio=new Label("Envío   "+(servicio.obtenerCostoEnvio()==0?"Gratis":pesos(servicio.obtenerCostoEnvio())));envio.getStyleClass().add("cliente-resumen-linea");
        Region separador=new Region();separador.getStyleClass().add("cliente-separador");
        Label total=new Label("Total   "+pesos(subtotal+servicio.obtenerCostoEnvio()));total.getStyleClass().add("cliente-total");total.setWrapText(true);
        Label etiquetaDireccion=new Label("Dirección de entrega");etiquetaDireccion.getStyleClass().add("cliente-etiqueta");
        ComboBox<DireccionCliente> selector=new ComboBox<DireccionCliente>();selector.setMaxWidth(Double.MAX_VALUE);selector.getItems().addAll(direcciones);selector.setPromptText("Selecciona una dirección");
        for(DireccionCliente d:direcciones)if(d.esPrincipal()){selector.setValue(d);break;}
        if(selector.getValue()==null&&!direcciones.isEmpty())selector.setValue(direcciones.get(0));
        Button administrar=botonTexto(direcciones.isEmpty()?"Registrar una dirección":"Administrar mis direcciones");administrar.setOnAction(e->aplicacion.mostrarDireccionesCliente());
        Button confirmar=boton("Confirmar pedido");confirmar.setMaxWidth(Double.MAX_VALUE);confirmar.setDisable(lineas.isEmpty()||direcciones.isEmpty());confirmar.setOnAction(e->confirmar(selector.getValue()));
        caja.getChildren().addAll(titulo,cuenta,envio,separador,total,etiquetaDireccion,selector,administrar,confirmar);
        if(direcciones.isEmpty()){Label aviso=new Label("Agrega una dirección en Mi cuenta para continuar.");aviso.getStyleClass().add("cliente-aviso");aviso.setWrapText(true);caja.getChildren().add(caja.getChildren().size()-1,aviso);}
        return caja;
    }

    private void confirmar(DireccionCliente direccion){
        if(direccion==null){aviso("Selecciona una dirección de entrega.");return;}
        Alert confirmacion=new Alert(Alert.AlertType.CONFIRMATION);confirmacion.setTitle("Confirmar pedido");confirmacion.setHeaderText("¿Confirmas tu compra?");confirmacion.setContentText("El pedido quedará registrado sin realizar un cobro en línea.");
        if(confirmacion.showAndWait().orElse(ButtonType.CANCEL)!=ButtonType.OK)return;
        try{String codigo=servicio.confirmarPedido(usuario.obtenerIdentificador(),direccion.obtenerIdentificador());Alert resultado=new Alert(Alert.AlertType.INFORMATION);resultado.setTitle("Pedido registrado");resultado.setHeaderText("Número de pedido: "+codigo);resultado.setContentText("Tu pedido se registró y el inventario se actualizó correctamente.");resultado.showAndWait();aplicacion.mostrarPedidosCliente();}
        catch(Exception ex){aviso(ex.getMessage()==null?"No se pudo registrar el pedido. El carrito se conservó.":ex.getMessage());}
    }
    private void cambiarCantidad(LineaCarrito linea,int cambio){try{int nueva=linea.obtenerCantidad()+cambio;if(nueva<1)return;servicio.actualizarCantidadCarrito(usuario.obtenerIdentificador(),linea.obtenerProducto().obtenerIdentificador(),nueva);aplicacion.mostrarCarrito();}catch(Exception ex){aviso(ex.getMessage());}}
    private void quitar(LineaCarrito linea){try{servicio.quitarDelCarrito(usuario.obtenerIdentificador(),linea.obtenerProducto().obtenerIdentificador());aplicacion.mostrarCarrito();}catch(Exception ex){aviso(ex.getMessage());}}
    private ImageView imagen(String ruta){ImageView vista=new ImageView();vista.setFitWidth(112);vista.setFitHeight(112);vista.setPreserveRatio(true);if(ruta!=null){InputStream flujo=getClass().getResourceAsStream(ruta);if(flujo!=null){try{vista.setImage(new Image(flujo));}finally{try{flujo.close();}catch(Exception ignorado){}}}}vista.getStyleClass().add("cliente-imagen-producto");return vista;}
    private Button boton(String texto){Button b=new Button(texto);b.getStyleClass().add("primary-button");return b;}
    private Button botonSecundario(String texto){Button b=new Button(texto);b.getStyleClass().add("cliente-cantidad-boton");return b;}
    private Button botonTexto(String texto){Button b=new Button(texto);b.getStyleClass().add("cliente-enlace");return b;}
    private String pesos(long valor){NumberFormat f=NumberFormat.getCurrencyInstance(new Locale("es","CO"));f.setMaximumFractionDigits(0);return f.format(valor);}
    private void aviso(String texto){Alert a=new Alert(Alert.AlertType.WARNING);a.setTitle("Carrito de compras");a.setHeaderText(null);a.setContentText(texto);a.showAndWait();}
}
