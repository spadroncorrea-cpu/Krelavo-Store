package krelavo.store.vista;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import krelavo.store.Main;

/** Piezas compartidas para la navegación de las pantallas del cliente. */
public final class ComponentesCliente {
    private ComponentesCliente() { }
    public static BorderPane crearMarco(Main aplicacion,String seccion) {
        BorderPane raiz=new BorderPane();raiz.getStyleClass().add("cliente-pantalla");raiz.setTop(crearEncabezado(aplicacion,seccion));return raiz;
    }
    private static Parent crearEncabezado(Main app,String seccion){
        HBox encabezado=new HBox(20);encabezado.setAlignment(Pos.CENTER_LEFT);encabezado.setPadding(new Insets(18,58,18,58));encabezado.getStyleClass().add("cliente-encabezado");
        javafx.scene.layout.VBox marca=new javafx.scene.layout.VBox(2);Label nombre=new Label("KRELAVO STORE");nombre.getStyleClass().add("catalog-brand-title");Label lema=new Label("TU BELLEZA, TU ESENCIA");lema.getStyleClass().add("catalog-brand-tagline");marca.getChildren().addAll(nombre,lema);
        Region espacio=new Region();HBox.setHgrow(espacio,Priority.ALWAYS);
        Button tienda=boton("Tienda",seccion.equals("Tienda"));tienda.setOnAction(e->app.mostrarTienda());
        Button favoritos=boton("Lista de deseos",seccion.equals("Favoritos"));favoritos.setOnAction(e->app.mostrarFavoritos());
        Button carrito=boton("Carrito",seccion.equals("Carrito"));carrito.setOnAction(e->app.mostrarCarrito());
        Button cuenta=boton("Mi cuenta",seccion.equals("Cuenta"));cuenta.setOnAction(e->app.mostrarMiCuenta());
        Button cerrar=boton("Cerrar sesión",false);cerrar.setOnAction(e->app.cerrarSesion());
        encabezado.getChildren().addAll(marca,espacio,tienda,favoritos,carrito,cuenta,cerrar);return encabezado;
    }
    private static Button boton(String texto,boolean activo){Button b=new Button(texto);b.getStyleClass().add("cliente-nav");if(activo)b.getStyleClass().add("cliente-nav-activo");return b;}
}
