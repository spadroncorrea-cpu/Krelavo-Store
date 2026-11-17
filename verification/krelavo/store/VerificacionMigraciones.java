package krelavo.store;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import krelavo.store.basedatos.FabricaConexiones;
import krelavo.store.basedatos.InicializadorBaseDatos;

/** Comprueba migraciones e inventario usando una base temporal de verificación. */
public final class VerificacionMigraciones {
    private VerificacionMigraciones() { }

    public static void main(String[] argumentos) throws Exception {
        Path ruta = Paths.get("build", "verification", "krelavo-refactor.db").toAbsolutePath();
        Files.createDirectories(ruta.getParent());
        Files.deleteIfExists(ruta);
        FabricaConexiones fabrica = new FabricaConexiones(ruta);
        InicializadorBaseDatos inicializador = new InicializadorBaseDatos(fabrica);
        inicializador.inicializar();

        comprobar("versión de esquema aplicada", consultarEntero(fabrica, "PRAGMA user_version") == 5);
        comprobar("16 productos originales", consultarEntero(fabrica, "SELECT count(*) FROM products") == 16);
        comprobar("160 unidades iniciales", consultarEntero(fabrica, "SELECT coalesce(sum(stock_quantity),0) FROM products") == 160);
        comprobar("movimientos iniciales conservados", consultarEntero(fabrica,
                "SELECT count(*) FROM inventory_movements WHERE movement_type='IN'") == 16);
        comprobar("las diez tablas originales existen", consultarEntero(fabrica,
                "SELECT count(*) FROM sqlite_master WHERE type='table' AND name IN "
                + "('users','categories','brands','products','favorites','carts','cart_items','orders','order_items','inventory_movements')") == 10);
        comprobar("claves foráneas activas por conexión", consultarEntero(fabrica, "PRAGMA foreign_keys") == 1);

        inicializador.inicializar();
        comprobar("la segunda inicialización no duplica productos",
                consultarEntero(fabrica, "SELECT count(*) FROM products") == 16);
        comprobar("la segunda inicialización conserva existencias",
                consultarEntero(fabrica, "SELECT coalesce(sum(stock_quantity),0) FROM products") == 160);
        comprobar("la segunda inicialización no duplica movimientos",
                consultarEntero(fabrica, "SELECT count(*) FROM inventory_movements WHERE movement_type='IN'") == 16);
        System.out.println("VERIFICACIÓN CORRECTA: 9 comprobaciones; base temporal " + ruta);
    }

    private static int consultarEntero(FabricaConexiones fabrica, String consulta) throws Exception {
        try (Connection conexion = fabrica.abrirConexion();
                Statement sentencia = conexion.createStatement();
                ResultSet resultado = sentencia.executeQuery(consulta)) {
            if (!resultado.next()) {
                throw new IllegalStateException("La consulta no devolvió resultados: " + consulta);
            }
            return resultado.getInt(1);
        }
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            throw new AssertionError("Falló la comprobación: " + descripcion);
        }
        System.out.println("OK: " + descripcion);
    }
}
