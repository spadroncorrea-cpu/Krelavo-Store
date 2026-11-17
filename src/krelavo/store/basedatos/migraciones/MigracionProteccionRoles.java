package krelavo.store.basedatos.migraciones;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import krelavo.store.basedatos.MigracionEsquema;

/** Limita la base de datos a una cuenta administradora y protege los roles asignados. */
public final class MigracionProteccionRoles implements MigracionEsquema {
    @Override
    public int obtenerVersion() {
        return 4;
    }

    @Override
    public void aplicar(Connection conexion) throws SQLException {
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_users_admin_unico "
                    + "ON users(role) WHERE role = 'ADMIN'");
            sentencia.execute("CREATE TRIGGER IF NOT EXISTS trg_users_rol_inmutable "
                    + "BEFORE UPDATE OF role ON users WHEN NEW.role <> OLD.role "
                    + "BEGIN SELECT RAISE(ABORT, 'No se permite modificar el rol de una cuenta.'); END");
        }
    }
}
