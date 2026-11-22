# Krelavo Store

### Tu belleza, tu esencia.

## Descripción

Krelavo Store es un proyecto académico de una tienda virtual enfocada en productos de belleza, cuidado facial y cuidado capilar.

El proyecto inició con el diseño de un prototipo interactivo en Figma y continuó con el desarrollo de una aplicación de escritorio utilizando Java y JavaFX.

## Objetivo

Desarrollar una aplicación que permita explorar un catálogo de productos de belleza, gestionar compras y administrar la información de la tienda mediante una interfaz sencilla y organizada.

## Funcionalidades

### Módulo de clientes
- Registro de usuarios e inicio de sesión.
- Visualización del catálogo de productos.
- Búsqueda de productos.
- Filtros por categoría, marca y precio.
- Selección y gestión de productos favoritos.
- Carrito de compras con modificación de cantidades.
- Proceso de compra y registro de pedidos.
- Consulta del historial de compras.
- Edición de información personal.
- Gestión de direcciones.
- Cambio de contraseña.

### Módulo de administración
- Panel principal con información general de la tienda.
- Gestión de productos.
- Consulta de categorías y marcas.
- Control de inventario.
- Registro de entradas y ajustes de existencias.
- Consulta de movimientos de inventario.
- Consulta de pedidos y clientes.
- Visualización de reportes.

## Tecnologías utilizadas

- **Java 8:** lenguaje de programación.
- **JavaFX 8:** desarrollo de la interfaz gráfica.
- **SQLite:** gestión de la base de datos.
- **Apache Ant:** automatización de la compilación.
- **NetBeans:** entorno de desarrollo.
- **Figma:** diseño del prototipo.
- **Git y GitHub:** control de versiones.

## Requisitos para ejecutar el proyecto

Antes de abrir Krelavo Store, es necesario contar con:

- Windows.
- JDK 8 con soporte para JavaFX 8.
- Apache Ant (se puede utilizar el incluido en NetBeans).
- NetBeans.
- Las librerías incluidas en la carpeta `lib` del proyecto.

## Instalación y ejecución

### 1. Descargar el proyecto

1. Ingresar al repositorio de GitHub:
   https://github.com/spadroncorrea-cpu/Krelavo-Store
2. Seleccionar la rama `entrega-final`.
3. Hacer clic en el botón **Code**.
4. Seleccionar **Download ZIP**.
5. Extraer el archivo descargado en una ubicación del equipo.

### 2. Abrir el proyecto en NetBeans

1. Iniciar NetBeans.
2. Seleccionar la opción **File (Archivo)**.
3. Elegir **Open Project (Abrir proyecto)**.
4. Buscar la carpeta donde se extrajo Krelavo Store.
5. Seleccionar la carpeta principal del proyecto, donde se encuentran `build.xml` y `nbproject`.
6. Hacer clic en **Open Project**.

### 3. Configurar Java

1. Verificar que NetBeans tenga configurado JDK 8.
2. Comprobar que la configuración permita utilizar JavaFX 8.
3. Verificar que las librerías necesarias se encuentren disponibles en la carpeta `lib`.

### 4. Ejecutar la aplicación

1. Seleccionar el proyecto Krelavo Store en NetBeans.
2. Esperar a que finalice la carga del proyecto.
3. Seleccionar **Run Project (Ejecutar proyecto)** o pulsar F6.
4. Esperar a que aparezca la pantalla de inicio de sesión.

### 5. Compilar y verificar el proyecto

También es posible utilizar Apache Ant desde la terminal, ubicándose en la carpeta principal del proyecto y ejecutando:

```bash
ant clean compile verify jar
```

Este comando limpia los archivos generados anteriormente, compila el código, ejecuta las verificaciones y genera el archivo JAR.

## Base de datos

Krelavo Store utiliza SQLite para almacenar la información del sistema, incluyendo usuarios, productos, favoritos, pedidos e inventario.

La inicialización y las migraciones de la base de datos se gestionan desde la aplicación.

## Manual de usuario

El repositorio incluye el Manual de Usuario de Krelavo Store, ubicado en:

`documentos/Manual_Usuario_Krelavo.pdf`

Este documento explica cómo utilizar las funcionalidades disponibles para los perfiles de cliente y administrador.

## Equipo de trabajo

- Sofía Padrón.
- Daniela Cárdenas.
- José Pacheco.
- Santiago Morales.
## Prototipo en Figma

[Ver prototipo de Krelavo Store](https://www.figma.com/proto/TFxeXIvMGm6Uprt4WHDnxM/Krelavo-Store---Prototipo?node-id=1-2&t=X1f1Wyk973MwDhkK-0&scaling=min-zoom&content-scaling=fixed&page-id=0%3A1&starting-point-node-id=32%3A131)

## Estado del proyecto

Proyecto académico desarrollado con JavaFX y SQLite. Incluye módulos de clientes y administración, gestión de compras, favoritos e inventario.

---

**KRELAVO STORE**

*Tu belleza, tu esencia.*