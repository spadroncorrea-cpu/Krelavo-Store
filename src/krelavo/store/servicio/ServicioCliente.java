package krelavo.store.servicio;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import krelavo.store.accesodatos.RepositorioClienteJDBC;
import krelavo.store.modelo.DireccionCliente;
import krelavo.store.modelo.LineaCarrito;
import krelavo.store.modelo.PedidoCliente;
import krelavo.store.modelo.Producto;
import krelavo.store.modelo.Usuario;
import krelavo.store.seguridad.HashContrasenas;

/** Reglas de perfil, carrito, favoritos, direcciones y compras del cliente. */
public final class ServicioCliente {
    private static final String PROPIEDAD_ENVIO="krelavo.envio.cop";
    private static final Pattern CORREO=Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final RepositorioClienteJDBC repositorio;
    private final HashContrasenas hasher;
    private final long costoEnvio;

    public ServicioCliente(RepositorioClienteJDBC repositorio,HashContrasenas hasher){
        if(repositorio==null||hasher==null)throw new IllegalArgumentException("Se requieren el repositorio de cliente y el servicio de contraseñas.");
        this.repositorio=repositorio;this.hasher=hasher;
        String configuracion=System.getProperty(PROPIEDAD_ENVIO,"0").trim();
        try{costoEnvio=Long.parseLong(configuracion);}catch(NumberFormatException ex){throw new IllegalStateException("La propiedad "+PROPIEDAD_ENVIO+" debe ser un número entero de pesos colombianos.",ex);}
        if(costoEnvio<0)throw new IllegalStateException("El costo de envío no puede ser negativo.");
    }

    public long obtenerCostoEnvio(){return costoEnvio;}
    public List<Producto> listarProductos()throws SQLException{return repositorio.listarProductos();}
    public List<Producto> listarFavoritos(long usuario)throws SQLException{return repositorio.listarFavoritos(usuario);}
    public boolean cambiarFavorito(long usuario,long producto)throws SQLException{return repositorio.cambiarFavorito(usuario,producto);}
    public List<LineaCarrito> obtenerCarrito(long usuario)throws SQLException{return repositorio.obtenerCarrito(usuario);}
    public int obtenerUnidadesCarrito(long usuario)throws SQLException{return repositorio.obtenerUnidadesCarrito(usuario);}
    public void agregarAlCarrito(long usuario,long producto,int cantidad)throws SQLException{repositorio.agregarAlCarrito(usuario,producto,cantidad);}
    public void actualizarCantidadCarrito(long usuario,long producto,int cantidad)throws SQLException{repositorio.actualizarCantidadCarrito(usuario,producto,cantidad);}
    public void quitarDelCarrito(long usuario,long producto)throws SQLException{repositorio.quitarDelCarrito(usuario,producto);}
    public String confirmarPedido(long usuario,long direccion)throws SQLException{return repositorio.confirmarPedido(usuario,direccion,costoEnvio);}
    public List<DireccionCliente> listarDirecciones(long usuario)throws SQLException{return repositorio.listarDirecciones(usuario);}
    public List<PedidoCliente> listarPedidos(long usuario)throws SQLException{return repositorio.listarPedidos(usuario);}
    public PedidoCliente buscarPedido(long usuario,String codigo)throws SQLException{return repositorio.buscarPedido(usuario,codigo);}
    public Usuario buscarUsuarioCliente(long usuario)throws SQLException{return repositorio.buscarUsuarioCliente(usuario);}

    public Usuario actualizarPerfil(long usuario,String nombre,String correo,String telefono)throws SQLException{
        String nombreLimpio=limpiar(nombre);
        String correoLimpio=limpiar(correo).toLowerCase(Locale.ROOT);
        String telefonoLimpio=limpiar(telefono);
        if(nombreLimpio.isEmpty()||nombreLimpio.length()>120)throw new IllegalArgumentException("El nombre es obligatorio y no puede superar 120 caracteres.");
        if(correoLimpio.length()>254||!CORREO.matcher(correoLimpio).matches())throw new IllegalArgumentException("Ingresa un correo electrónico válido.");
        if(!telefonoLimpio.isEmpty()&&!telefonoLimpio.matches("[+0-9() .-]{7,25}"))throw new IllegalArgumentException("Revisa el teléfono ingresado.");
        return repositorio.actualizarPerfil(usuario,nombreLimpio,correoLimpio,telefonoLimpio);
    }

    public void cambiarContrasena(long usuario,String actual,String nueva,String confirmacion)throws SQLException{
        if(actual==null||actual.isEmpty())throw new IllegalArgumentException("Ingresa tu contraseña actual.");
        if(nueva==null||nueva.length()<12||nueva.length()>128)throw new IllegalArgumentException("La nueva contraseña debe tener entre 12 y 128 caracteres.");
        if(!nueva.equals(confirmacion))throw new IllegalArgumentException("La confirmación no coincide.");
        char[] claveActual=actual.toCharArray(),claveNueva=nueva.toCharArray();
        try{
            if(!hasher.verificar(claveActual,repositorio.obtenerHashContrasena(usuario)))throw new IllegalArgumentException("La contraseña actual no es correcta.");
            repositorio.actualizarHashContrasena(usuario,hasher.generarHash(claveNueva));
        }finally{HashContrasenas.limpiar(claveActual);HashContrasenas.limpiar(claveNueva);}
    }

    public DireccionCliente guardarDireccion(long usuario,long identificador,String destinatario,String telefono,
            String linea,String ciudad,String departamento,String indicaciones,boolean principal)throws SQLException{
        String nombre=limpiar(destinatario),fono=limpiar(telefono),calle=limpiar(linea),municipio=limpiar(ciudad),region=limpiar(departamento);
        if(nombre.isEmpty()||calle.isEmpty()||municipio.isEmpty()||region.isEmpty())throw new IllegalArgumentException("Completa destinatario, dirección, ciudad y departamento.");
        if(nombre.length()>120||calle.length()>240||municipio.length()>100||region.length()>100)throw new IllegalArgumentException("Uno de los datos de dirección supera la longitud permitida.");
        if(!fono.matches("[+0-9() .-]{7,25}"))throw new IllegalArgumentException("Ingresa un teléfono válido para la entrega.");
        List<DireccionCliente> anteriores=repositorio.listarDirecciones(usuario);
        boolean principalFinal=principal||anteriores.isEmpty();
        String nota=limpiar(indicaciones);
        if(nota.length()>300)throw new IllegalArgumentException("Las indicaciones no pueden superar 300 caracteres.");
        return repositorio.guardarDireccion(usuario,new DireccionCliente(identificador,nombre,fono,calle,municipio,region,nota,principalFinal));
    }
    public void eliminarDireccion(long usuario,long direccion)throws SQLException{
        List<DireccionCliente> anteriores=repositorio.listarDirecciones(usuario);
        boolean principal=false;for(DireccionCliente d:anteriores)if(d.obtenerIdentificador()==direccion)principal=d.esPrincipal();
        repositorio.eliminarDireccion(usuario,direccion);
        if(principal){List<DireccionCliente> restantes=repositorio.listarDirecciones(usuario);if(!restantes.isEmpty())repositorio.marcarDireccionPrincipal(usuario,restantes.get(0).obtenerIdentificador());}
    }
    public void marcarDireccionPrincipal(long usuario,long direccion)throws SQLException{repositorio.marcarDireccionPrincipal(usuario,direccion);}
    private String limpiar(String valor){return valor==null?"":valor.trim();}
}
