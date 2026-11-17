# Convenciones de desarrollo de Krelavo Store

## Idioma

- Usa español para el código nuevo, nombres de clases e interfaces, métodos, variables, atributos, parámetros, constantes, comentarios, JavaDoc, mensajes de error, registros, interfaz y documentación.
- Prefiere nombres descriptivos en español, por ejemplo `Producto`, `ControladorCatalogo`, `ServicioInventario`, `registrarProducto` y `cantidadExistencias`.
- Conserva los identificadores ingleses exigidos por Java, JavaFX, JDBC, bibliotecas, herramientas y contratos existentes que no puedan cambiarse de forma segura.
- Mantén en español los textos visibles para el usuario.

## Compatibilidad y cambios

- Mantén compatibilidad con Java 8, JavaFX 8, Apache Ant y el proyecto NetBeans existente.
- No hagas traducciones masivas ni renombres archivos, clases, métodos, propiedades, tablas o columnas sin revisar primero todas sus referencias y presentar el impacto.
- Trata los nombres actuales del esquema SQLite como contrato persistente. Cualquier cambio de tabla o columna debe realizarse con una migración versionada y compatible con bases ya creadas.
- Antes de renombrar código existente, revisa importaciones, referencias entre clases, manifiesto, configuración Ant/NetBeans, recursos, migraciones, base persistente y verificaciones; compila con JDK 8 después del cambio.
- No modifiques el comportamiento actual de pantallas o flujos al realizar cambios de infraestructura, salvo que la tarea lo solicite.

## Arquitectura y datos

- Conserva la separación entre interfaz/controladores, servicios, DAO, modelos y acceso a datos.
- No mezcles SQL con controladores JavaFX. Usa consultas parametrizadas y transacciones para operaciones que actualicen varias tablas.
- Activa las claves foráneas SQLite en cada conexión.
- Usa migraciones incrementales con `PRAGMA user_version`; no edites migraciones ya aplicadas para cambiar bases existentes.
- No guardes contraseñas en texto plano ni rutas absolutas de recursos dependientes del equipo.
- Las cargas iniciales deben ser idempotentes y preservar datos que hayan sido modificados posteriormente.
