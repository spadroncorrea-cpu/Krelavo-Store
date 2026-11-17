package krelavo.store.servicio;

import java.sql.SQLException;
import krelavo.store.accesodatos.RepositorioAdministracionJDBC;
import krelavo.store.modelo.DatosSeccionAdministrativa;
import krelavo.store.modelo.ResumenAdministrativo;

/** Aplica validaciones del negocio para las operaciones administrativas. */
public final class ServicioAdministracion {
    private final RepositorioAdministracionJDBC repositorio;
    public ServicioAdministracion(RepositorioAdministracionJDBC repositorio) { this.repositorio=repositorio; }
    public ResumenAdministrativo obtenerResumen() throws SQLException { return repositorio.obtenerResumen(); }
    public DatosSeccionAdministrativa listar(String seccion,String busqueda) throws SQLException { return repositorio.listar(seccion,busqueda); }
    public void actualizarProducto(long id,String nombre,String precio) throws Exception {
        String nombreLimpio=nombre==null?"":nombre.trim();
        if(nombreLimpio.isEmpty())throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        long precioNumero;
        try { precioNumero=Long.parseLong(precio.trim()); } catch(Exception e) { throw new IllegalArgumentException("El precio debe ser un número entero en pesos colombianos."); }
        if(precioNumero<0)throw new IllegalArgumentException("El precio no puede ser negativo.");
        repositorio.actualizarProducto(id,nombreLimpio,precioNumero);
    }
    public void crearProducto(String nombre,String marca,String categoria,String precio) throws Exception {
        String nombreLimpio=nombre==null?"":nombre.trim();
        if(nombreLimpio.isEmpty())throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        if(marca==null||marca.trim().isEmpty()||categoria==null||categoria.trim().isEmpty())
            throw new IllegalArgumentException("Selecciona una marca y una categoría.");
        long precioNumero;
        try { precioNumero=Long.parseLong(precio.trim()); } catch(Exception e) { throw new IllegalArgumentException("El precio debe ser un número entero en pesos colombianos."); }
        if(precioNumero<0)throw new IllegalArgumentException("El precio no puede ser negativo.");
        repositorio.crearProducto(nombreLimpio,marca.trim(),categoria.trim(),precioNumero);
    }
    public void ajustarExistencias(long id,String cambio,String nota,long usuarioId) throws Exception {
        int cantidad;
        try { cantidad=Integer.parseInt(cambio.trim()); } catch(Exception e) { throw new IllegalArgumentException("El ajuste debe ser un número entero distinto de cero."); }
        if(cantidad==0)throw new IllegalArgumentException("El ajuste debe ser distinto de cero.");
        repositorio.ajustarExistencias(id,cantidad,nota==null?"Ajuste administrativo":nota.trim(),usuarioId);
    }
}
