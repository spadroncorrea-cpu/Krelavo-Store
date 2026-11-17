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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import krelavo.store.Main;
import krelavo.store.modelo.Producto;
import krelavo.store.modelo.Usuario;
import krelavo.store.servicio.ServicioCliente;
import krelavo.store.vista.ComponentesCliente;

/** Lista de deseos persistente del usuario autenticado. */
public final class ControladorFavoritosCliente {
    private final Main aplicacion;private final Usuario usuario;private final ServicioCliente servicio;
    public ControladorFavoritosCliente(Main app,Usuario usuario,ServicioCliente servicio){this.aplicacion=app;this.usuario=usuario;this.servicio=servicio;}
    public Parent crearVista()throws Exception{
        BorderPane raiz=ComponentesCliente.crearMarco(aplicacion,"Favoritos");VBox contenido=new VBox(17);contenido.setPadding(new Insets(35,62,55,62));contenido.getStyleClass().add("cliente-contenido");
        Label migas=new Label("MI CUENTA   /   LISTA DE DESEOS");migas.getStyleClass().add("cliente-migas");Label titulo=new Label("Tu lista de deseos");titulo.getStyleClass().add("cliente-titulo");Label subtitulo=new Label("Guarda aquí los productos que quieres volver a encontrar.");subtitulo.getStyleClass().add("cliente-subtitulo");contenido.getChildren().addAll(migas,titulo,subtitulo);
        List<Producto> favoritos=servicio.listarFavoritos(usuario.obtenerIdentificador());
        if(favoritos.isEmpty()){VBox vacio=new VBox(12);vacio.setAlignment(Pos.CENTER);vacio.setPadding(new Insets(48));vacio.getStyleClass().add("cliente-tarjeta");Label mensaje=new Label("Todavía no guardaste productos en tu lista de deseos.");mensaje.getStyleClass().add("cliente-subtitulo");Button explorar=new Button("Explorar productos");explorar.getStyleClass().add("primary-button");explorar.setOnAction(e->aplicacion.mostrarTienda());vacio.getChildren().addAll(mensaje,explorar);contenido.getChildren().add(vacio);}
        else for(Producto producto:favoritos)contenido.getChildren().add(fila(producto));
        ScrollPane scroll=new ScrollPane(contenido);scroll.setFitToWidth(true);scroll.getStyleClass().add("cliente-scroll");raiz.setCenter(scroll);return raiz;
    }
    private HBox fila(Producto producto){
        HBox fila=new HBox(18);fila.setAlignment(Pos.CENTER_LEFT);fila.setPadding(new Insets(16));fila.getStyleClass().add("cliente-tarjeta");
        ImageView imagen=new ImageView();imagen.setFitWidth(110);imagen.setFitHeight(110);imagen.setPreserveRatio(true);
        InputStream flujo=producto.obtenerRutaImagen()==null?null:getClass().getResourceAsStream(producto.obtenerRutaImagen());if(flujo!=null){try{imagen.setImage(new Image(flujo));}finally{try{flujo.close();}catch(Exception ignorado){}}}
        VBox datos=new VBox(7);HBox.setHgrow(datos,Priority.ALWAYS);Label nombre=new Label(producto.obtenerNombre());nombre.getStyleClass().add("cliente-producto-nombre");nombre.setWrapText(true);Label marca=new Label(producto.obtenerMarca());marca.getStyleClass().add("cliente-subtitulo");Label precio=new Label(pesos(producto.obtenerPrecioCop()));precio.getStyleClass().add("cliente-precio");datos.getChildren().addAll(nombre,marca,precio);
        Button carrito=new Button(producto.obtenerCantidadExistencias()>0?"Agregar al carrito":"Agotado");carrito.getStyleClass().add("primary-button");carrito.setDisable(producto.obtenerCantidadExistencias()<1);carrito.setOnAction(e->{try{servicio.agregarAlCarrito(usuario.obtenerIdentificador(),producto.obtenerIdentificador(),1);aplicacion.mostrarCarrito();}catch(Exception ex){mensaje(ex.getMessage());}});
        Button quitar=new Button("Quitar de favoritos");quitar.getStyleClass().add("cliente-secundario");quitar.setOnAction(e->{try{servicio.cambiarFavorito(usuario.obtenerIdentificador(),producto.obtenerIdentificador());aplicacion.mostrarFavoritos();}catch(Exception ex){mensaje(ex.getMessage());}});
        VBox acciones=new VBox(9,carrito,quitar);acciones.setAlignment(Pos.CENTER_RIGHT);fila.getChildren().addAll(imagen,datos,acciones);return fila;
    }
    private String pesos(long valor){NumberFormat f=NumberFormat.getCurrencyInstance(new Locale("es","CO"));f.setMaximumFractionDigits(0);return f.format(valor);}
    private void mensaje(String texto){Alert a=new Alert(Alert.AlertType.INFORMATION);a.setTitle("Lista de deseos");a.setHeaderText(null);a.setContentText(texto==null?"La acción no se pudo completar.":texto);a.showAndWait();}
}
