package krelavo.store.controlador;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import krelavo.store.Main;
import krelavo.store.modelo.DireccionCliente;
import krelavo.store.modelo.LineaCarrito;
import krelavo.store.modelo.PedidoCliente;
import krelavo.store.modelo.Usuario;
import krelavo.store.servicio.ServicioCliente;
import krelavo.store.servicio.EjecutorAcceso;
import krelavo.store.vista.ComponentesCliente;

/** Cuenta del cliente: perfil, contraseñas, direcciones e historial de pedidos. */
public final class ControladorMiCuenta {
    private final Main aplicacion;private Usuario usuario;private final ServicioCliente servicio;
    private final VBox panel=new VBox(17);private VBox opcionesCuenta;private String seccion="Información personal";
    public ControladorMiCuenta(Main app,Usuario usuario,ServicioCliente servicio){this.aplicacion=app;this.usuario=usuario;this.servicio=servicio;}

    public Parent crearVista()throws Exception{return crearVista("Información personal");}
    public Parent crearVistaPedidos()throws Exception{return crearVista("Mis pedidos");}
    public Parent crearVistaDirecciones()throws Exception{return crearVista("Mis direcciones");}
    private Parent crearVista(String inicial)throws Exception{
        seccion=inicial;BorderPane raiz=ComponentesCliente.crearMarco(aplicacion,"Cuenta");
        VBox contenido=new VBox(20);contenido.setPadding(new Insets(32,58,46,58));contenido.getStyleClass().add("cliente-contenido");
        Label migas=new Label("KRELAVO STORE   /   MI CUENTA");migas.getStyleClass().add("cliente-migas");Label titulo=new Label("Mi cuenta");titulo.getStyleClass().add("cliente-titulo");
        HBox cuerpo=new HBox(22);VBox menu=crearMenu();menu.setPrefWidth(250);menu.setMinWidth(230);panel.getStyleClass().add("cliente-tarjeta");panel.setPadding(new Insets(24));HBox.setHgrow(panel,Priority.ALWAYS);cuerpo.getChildren().addAll(menu,panel);
        contenido.getChildren().addAll(migas,titulo,cuerpo);ScrollPane scroll=new ScrollPane(contenido);scroll.setFitToWidth(true);scroll.getStyleClass().add("cliente-scroll");raiz.setCenter(scroll);mostrarSeccion(seccion);return raiz;
    }

    private VBox crearMenu(){
        VBox menu=new VBox(6);opcionesCuenta=menu;menu.setPadding(new Insets(18));menu.getStyleClass().add("cliente-tarjeta");
        Label encabezado=new Label("Opciones de cuenta");encabezado.getStyleClass().add("cliente-subtitulo-fuerte");menu.getChildren().add(encabezado);
        String[] opciones={"Información personal","Cambiar contraseña","Mis direcciones","Mis pedidos","Lista de deseos"};
        for(String opcion:opciones){Button b=new Button(opcion);b.setMaxWidth(Double.MAX_VALUE);b.setAlignment(Pos.CENTER_LEFT);b.getStyleClass().add("cuenta-opcion");b.setOnAction(e->{if("Lista de deseos".equals(opcion)){aplicacion.mostrarFavoritos();return;}mostrarSeccion(opcion);});menu.getChildren().add(b);}
        Button salir=new Button("Cerrar sesión");salir.setMaxWidth(Double.MAX_VALUE);salir.setAlignment(Pos.CENTER_LEFT);salir.getStyleClass().add("cuenta-opcion-salir");salir.setOnAction(e->aplicacion.cerrarSesion());menu.getChildren().add(salir);return menu;
    }

    private void mostrarSeccion(String opcion){
        seccion=opcion;panel.getChildren().clear();
        if(opcionesCuenta!=null)for(javafx.scene.Node nodo:opcionesCuenta.getChildren())if(nodo instanceof Button){
            Button boton=(Button)nodo;boton.getStyleClass().remove("cuenta-opcion-activa");
            if(opcion.equals(boton.getText()))boton.getStyleClass().add("cuenta-opcion-activa");
        }
        try{
            if("Información personal".equals(opcion))mostrarPerfil();
            else if("Cambiar contraseña".equals(opcion))mostrarCambioContrasena();
            else if("Mis direcciones".equals(opcion))mostrarDirecciones();
            else mostrarPedidos();
        }catch(Exception ex){aviso("No se pudo cargar la sección: "+mensajeError(ex),Alert.AlertType.ERROR);}
    }

    private void mostrarPerfil()throws Exception{
        Label encabezado=tituloSeccion("Información personal");
        GridPane detalles=new GridPane();detalles.setHgap(15);detalles.setVgap(12);
        agregarDetalle(detalles,0,"Nombre completo",usuario.obtenerNombre());agregarDetalle(detalles,1,"Correo electrónico",usuario.obtenerCorreo());
        agregarDetalle(detalles,2,"Teléfono",usuario.obtenerTelefono().isEmpty()?"Sin registrar":usuario.obtenerTelefono());
        List<DireccionCliente> direcciones=servicio.listarDirecciones(usuario.obtenerIdentificador());String principal="Sin registrar";
        for(DireccionCliente d:direcciones)if(d.esPrincipal()){principal=d.obtenerLinea()+", "+d.obtenerCiudad()+", "+d.obtenerDepartamento();break;}
        agregarDetalle(detalles,3,"Dirección principal",principal);agregarDetalle(detalles,4,"Fecha de registro",usuario.obtenerFechaRegistro().isEmpty()?"—":usuario.obtenerFechaRegistro());
        TextField nombre=new TextField(usuario.obtenerNombre());TextField correo=new TextField(usuario.obtenerCorreo());TextField telefono=new TextField(usuario.obtenerTelefono());
        nombre.getStyleClass().add("login-input");correo.getStyleClass().add("login-input");telefono.getStyleClass().add("login-input");
        GridPane formulario=new GridPane();formulario.setHgap(12);formulario.setVgap(10);
        formulario.add(etiqueta("Nombre completo"),0,0);formulario.add(nombre,1,0);formulario.add(etiqueta("Correo electrónico"),0,1);formulario.add(correo,1,1);formulario.add(etiqueta("Teléfono"),0,2);formulario.add(telefono,1,2);
        GridPane.setHgrow(nombre,Priority.ALWAYS);GridPane.setHgrow(correo,Priority.ALWAYS);GridPane.setHgrow(telefono,Priority.ALWAYS);
        Button guardar=boton("Guardar cambios");Label mensaje=new Label();mensaje.getStyleClass().add("cliente-aviso");guardar.setOnAction(e->{try{usuario=servicio.actualizarPerfil(usuario.obtenerIdentificador(),nombre.getText(),correo.getText(),telefono.getText());aplicacion.actualizarSesion(usuario);mostrarSeccion("Información personal");exito("Tu información se actualizó correctamente.");}catch(Exception ex){mensaje.setText(mensajeError(ex));}});
        panel.getChildren().addAll(encabezado,detalles,new Label("Editar perfil"),formulario,guardar,mensaje);
        Label pedidosTitulo=tituloSeccion("Pedidos recientes");panel.getChildren().add(pedidosTitulo);
        List<PedidoCliente> pedidos=servicio.listarPedidos(usuario.obtenerIdentificador());
        if(pedidos.isEmpty()){Label vacio=new Label("Todavía no tienes pedidos.");vacio.getStyleClass().add("cliente-subtitulo");panel.getChildren().add(vacio);}
        else for(int i=0;i<Math.min(3,pedidos.size());i++)panel.getChildren().add(filaPedido(pedidos.get(i)));
    }
    private void agregarDetalle(GridPane grid,int fila,String titulo,String valor){Label a=etiqueta(titulo);Label b=new Label(valor);b.getStyleClass().add("cliente-valor");b.setWrapText(true);grid.add(a,0,fila);grid.add(b,1,fila);}

    private void mostrarCambioContrasena(){
        panel.getChildren().add(tituloSeccion("Cambiar contraseña"));
        PasswordField actual=campoContrasena("Contraseña actual"),nueva=campoContrasena("Nueva contraseña · mínimo 12 caracteres"),confirmar=campoContrasena("Confirmar nueva contraseña");
        Button guardar=boton("Actualizar contraseña");Label mensaje=new Label();mensaje.getStyleClass().add("cliente-aviso");
        guardar.setOnAction(e->{
            if(guardar.isDisabled())return;
            String claveActual=actual.getText(),claveNueva=nueva.getText(),claveConfirmada=confirmar.getText();
            guardar.setDisable(true);mensaje.setText("Protegiendo la nueva contraseña…");
            Task<Void> tarea=EjecutorAcceso.crearTarea(()->{servicio.cambiarContrasena(usuario.obtenerIdentificador(),claveActual,claveNueva,claveConfirmada);return null;});
            tarea.setOnSucceeded(evento->{guardar.setDisable(false);actual.clear();nueva.clear();confirmar.clear();mensaje.setText("");exito("La contraseña se actualizó correctamente.");});
            tarea.setOnFailed(evento->{guardar.setDisable(false);mensaje.setText(mensajeError(tarea.getException()));});
            EjecutorAcceso.ejecutar(tarea);
        });
        panel.getChildren().addAll(etiqueta("Contraseña actual"),actual,etiqueta("Nueva contraseña"),nueva,etiqueta("Confirmación"),confirmar,guardar,mensaje);
    }

    private void mostrarDirecciones()throws Exception{
        HBox cabecera=new HBox();cabecera.setAlignment(Pos.CENTER_LEFT);Label titulo=tituloSeccion("Mis direcciones");HBox.setHgrow(titulo,Priority.ALWAYS);Button agregar=boton("Agregar dirección");agregar.setOnAction(e->dialogoDireccion(null));cabecera.getChildren().addAll(titulo,agregar);panel.getChildren().add(cabecera);
        List<DireccionCliente> direcciones=servicio.listarDirecciones(usuario.obtenerIdentificador());
        if(direcciones.isEmpty()){Label vacio=new Label("Aún no registraste direcciones de entrega.");vacio.getStyleClass().add("cliente-subtitulo");panel.getChildren().add(vacio);}
        for(DireccionCliente d:direcciones){VBox tarjeta=new VBox(8);tarjeta.setPadding(new Insets(15));tarjeta.getStyleClass().add("cliente-tarjeta");Label nombre=new Label(d.obtenerDestinatario()+(d.esPrincipal()?"  ·  Principal":""));nombre.getStyleClass().add("cliente-subtitulo-fuerte");Label texto=new Label(d.obtenerLinea()+"\n"+d.obtenerCiudad()+", "+d.obtenerDepartamento()+"\n"+d.obtenerTelefono()+(d.obtenerIndicaciones().isEmpty()?"":"\n"+d.obtenerIndicaciones()));texto.getStyleClass().add("cliente-valor");texto.setWrapText(true);
            HBox acciones=new HBox(8);Button editar=botonSecundario("Editar");editar.setOnAction(e->dialogoDireccion(d));Button principal=botonSecundario("Hacer principal");principal.setDisable(d.esPrincipal());principal.setOnAction(e->{try{servicio.marcarDireccionPrincipal(usuario.obtenerIdentificador(),d.obtenerIdentificador());mostrarSeccion("Mis direcciones");}catch(Exception ex){aviso(mensajeError(ex),Alert.AlertType.ERROR);}});Button eliminar=botonTexto("Eliminar");eliminar.setOnAction(e->eliminarDireccion(d));acciones.getChildren().addAll(editar,principal,eliminar);tarjeta.getChildren().addAll(nombre,texto,acciones);panel.getChildren().add(tarjeta);}
    }

    private void dialogoDireccion(DireccionCliente d){
        Dialog<ButtonType> dialogo=new Dialog<ButtonType>();dialogo.setTitle(d==null?"Agregar dirección":"Editar dirección");dialogo.setHeaderText("Datos para la entrega");
        TextField nombre=new TextField(d==null?usuario.obtenerNombre():d.obtenerDestinatario());TextField telefono=new TextField(d==null?usuario.obtenerTelefono():d.obtenerTelefono());TextField linea=new TextField(d==null?"":d.obtenerLinea());TextField ciudad=new TextField(d==null?"":d.obtenerCiudad());TextField departamento=new TextField(d==null?"":d.obtenerDepartamento());TextField indicaciones=new TextField(d==null?"":d.obtenerIndicaciones());CheckBox principal=new CheckBox("Usar como dirección principal");principal.setSelected(d==null||d.esPrincipal());
        GridPane campos=new GridPane();campos.setHgap(10);campos.setVgap(8);campos.setPadding(new Insets(12));String[] etiquetas={"Destinatario","Teléfono","Dirección","Ciudad","Departamento","Indicaciones"};TextField[] valores={nombre,telefono,linea,ciudad,departamento,indicaciones};for(int i=0;i<etiquetas.length;i++){campos.add(new Label(etiquetas[i]),0,i);campos.add(valores[i],1,i);GridPane.setHgrow(valores[i],Priority.ALWAYS);}campos.add(principal,1,6);dialogo.getDialogPane().setContent(campos);dialogo.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL,ButtonType.OK);
        dialogo.showAndWait().ifPresent(respuesta->{if(respuesta==ButtonType.OK)try{servicio.guardarDireccion(usuario.obtenerIdentificador(),d==null?0:d.obtenerIdentificador(),nombre.getText(),telefono.getText(),linea.getText(),ciudad.getText(),departamento.getText(),indicaciones.getText(),principal.isSelected());mostrarSeccion("Mis direcciones");}catch(Exception ex){aviso(mensajeError(ex),Alert.AlertType.ERROR);}});
    }
    private void eliminarDireccion(DireccionCliente d){Alert confirmar=new Alert(Alert.AlertType.CONFIRMATION,"¿Eliminar esta dirección? Los pedidos anteriores conservarán su copia.",ButtonType.CANCEL,ButtonType.OK);confirmar.setTitle("Eliminar dirección");confirmar.showAndWait().ifPresent(r->{if(r==ButtonType.OK)try{servicio.eliminarDireccion(usuario.obtenerIdentificador(),d.obtenerIdentificador());mostrarSeccion("Mis direcciones");}catch(Exception ex){aviso(mensajeError(ex),Alert.AlertType.ERROR);}});}

    private void mostrarPedidos()throws Exception{
        panel.getChildren().add(tituloSeccion("Mis pedidos"));List<PedidoCliente> pedidos=servicio.listarPedidos(usuario.obtenerIdentificador());
        if(pedidos.isEmpty()){Label vacio=new Label("Cuando confirmes una compra, aparecerá aquí su seguimiento y detalle.");vacio.getStyleClass().add("cliente-subtitulo");vacio.setWrapText(true);panel.getChildren().add(vacio);return;}
        for(PedidoCliente pedido:pedidos)panel.getChildren().add(filaPedido(pedido));
    }
    private HBox filaPedido(PedidoCliente pedido){
        HBox fila=new HBox(14);fila.setAlignment(Pos.CENTER_LEFT);fila.setPadding(new Insets(13));fila.getStyleClass().add("cliente-tarjeta");VBox datos=new VBox(5);HBox.setHgrow(datos,Priority.ALWAYS);Label codigo=new Label(pedido.obtenerCodigo());codigo.getStyleClass().add("cliente-subtitulo-fuerte");Label detalle=new Label(pedido.obtenerFecha()+"  ·  "+pedido.obtenerCantidadProductos()+" unidades  ·  "+pedido.obtenerEstado());detalle.getStyleClass().add("cliente-migas");Label total=new Label(pesos(pedido.obtenerTotal()));total.getStyleClass().add("cliente-precio");datos.getChildren().addAll(codigo,detalle,total);Button ver=botonSecundario("Ver detalle");ver.setOnAction(e->mostrarDetalle(pedido));fila.getChildren().addAll(datos,ver);return fila;
    }
    private void mostrarDetalle(PedidoCliente pedido){StringBuilder texto=new StringBuilder();for(LineaCarrito l:pedido.obtenerLineas())texto.append(l.obtenerCantidad()).append(" × ").append(l.obtenerProducto().obtenerNombre()).append(" · ").append(pesos(l.obtenerProducto().obtenerPrecioCop())).append("\n");texto.append("\nEntrega: ").append(pedido.obtenerDestinatario()).append("\n").append(pedido.obtenerLinea()).append("\n").append(pedido.obtenerCiudad()).append(", ").append(pedido.obtenerDepartamento()).append("\nTeléfono: ").append(pedido.obtenerTelefono()).append("\n\nSubtotal: ").append(pesos(pedido.obtenerSubtotal())).append("\nEnvío: ").append(pedido.obtenerEnvio()==0?"Gratis":pesos(pedido.obtenerEnvio())).append("\nTotal: ").append(pesos(pedido.obtenerTotal())).append("\nEstado: ").append(pedido.obtenerEstado());Alert a=new Alert(Alert.AlertType.INFORMATION);a.setTitle("Detalle del pedido");a.setHeaderText(pedido.obtenerCodigo());a.setContentText(texto.toString());a.getDialogPane().setPrefWidth(540);a.showAndWait();}

    private Label tituloSeccion(String texto){Label l=new Label(texto);l.getStyleClass().add("cliente-subtitulo-fuerte");return l;}
    private Label etiqueta(String texto){Label l=new Label(texto);l.getStyleClass().add("cliente-etiqueta");return l;}
    private PasswordField campoContrasena(String prompt){PasswordField f=new PasswordField();f.setPromptText(prompt);f.getStyleClass().add("login-input");return f;}
    private Button boton(String texto){Button b=new Button(texto);b.getStyleClass().add("primary-button");return b;}
    private Button botonSecundario(String texto){Button b=new Button(texto);b.getStyleClass().add("cliente-secundario");return b;}
    private Button botonTexto(String texto){Button b=new Button(texto);b.getStyleClass().add("cliente-enlace");return b;}
    private String pesos(long valor){NumberFormat f=NumberFormat.getCurrencyInstance(new Locale("es","CO"));f.setMaximumFractionDigits(0);return f.format(valor);}
    private String mensajeError(Throwable e){return e.getMessage()==null?"No se pudo completar la operación.":e.getMessage();}
    private void aviso(String texto,Alert.AlertType tipo){Alert a=new Alert(tipo);a.setTitle("Mi cuenta");a.setHeaderText(null);a.setContentText(texto);a.showAndWait();}
    private void exito(String texto){aviso(texto,Alert.AlertType.INFORMATION);}
}
