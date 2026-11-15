<<<<<<< HEAD
# Krelavo Store — Login y Catálogo

Aplicación de escritorio con Java 8, JavaFX 8 y proyecto Ant para NetBeans 25.
La interfaz toma de LUNA únicamente la organización editorial y conserva la paleta Krelavo.

## Requisitos locales

- JDK 8u202: `C:\Program Files\Java\jdk1.8.0_202`
- JavaFX 8: `jre\lib\ext\jfxrt.jar` y bibliotecas nativas incluidas con ese JDK.
- NetBeans 25: instalado en `C:\Program Files\NetBeans-25`.
- El `build.xml` compila y ejecuta usando la ruta JDK 8 indicada en `jdk8.home`.

## Abrir en NetBeans 25

1. En **Tools > Java Platforms**, comprueba que exista una plataforma JDK 8. Si no está, pulsa **Add Platform > Java Standard Edition** y selecciona `C:\Program Files\Java\jdk1.8.0_202`.
2. Usa **File > Open Project** y selecciona `C:\Users\ASUS\Documents\KrelavoStore`.
3. En **Project Properties > Libraries**, selecciona la plataforma JDK 8.
4. En **Sources**, conserva Source/Binary Format en **JDK 8**.
5. Ejecuta **Run Project (F6)**. El punto de entrada es `krelavo.store.Main`.

## Credenciales de prueba

- Correo: `sofia@gmail.com`
- Contraseña: `Krelavo2026!`

## Dónde editar

- **Productos y precios:** `src/krelavo/store/model/ProductCatalog.java`. Cada producto se define con nombre, marca, precio en pesos, categoría y nombre/ruta de imagen.
- **Modelo:** `src/krelavo/store/model/Product.java`.
- **Imágenes existentes:** `src/krelavo/store/resources/images/`.
- **Fotos pendientes:** `src/krelavo/store/resources/images/pendientes/README.txt`; el Catálogo reserva ocho espacios y no reutiliza imágenes para llenarlos.
- **Estilos y paleta:** `src/krelavo/store/resources/css/styles.css`.
- **Login:** `src/krelavo/store/controller/LoginController.java`.
- **Catálogo y acciones:** `src/krelavo/store/controller/CatalogController.java`.

## Fotografías pendientes

El proyecto contiene ocho fotos originales de producto: cuatro faciales y cuatro capilares. Para llegar a dieciséis faltan cuatro faciales y cuatro capilares. Las ocho tarjetas pendientes identifican explícitamente la falta de foto/datos. No hay precios, nombres de producto o marcas asignados a esas fichas.

La carpeta de pendientes documenta los nombres sugeridos para incorporar fotos. Cuando agregues una ficha real, completa su marca, nombre y precio en `ProductCatalog.java`, establece la ruta a su PNG/JPG y cambia `placeholder` a `false` al construir el `Product`.

El panel fotográfico del Login se compone con tres imágenes de skincare ya existentes; no se añadió una foto editorial nueva.

## Acciones incluidas

- Login: campos vacíos, formato básico de correo, credenciales de prueba y navegación al catálogo.
- Catálogo: búsqueda por texto, categoría, marca y precio; cuatro marcas destacadas; filtro de favoritos; contador del carrito; botones de registro/login informativos.
- Productos sin foto o datos: no se pueden agregar al carrito.
- No hay pantallas completas de registro, cuenta, carrito ni administración.

## Verificación

`verification/krelavo/store/SmokeCheck.java` comprueba Login, navegación, filtros, favoritos, imágenes y contador del carrito bajo Java 8/JavaFX 8. Desde la raíz del proyecto puede ejecutarse el target Ant `verify` si Ant está disponible y la plataforma JDK 8 está configurada.
=======
# Tienda Krelavo

### Tu belleza, tu esencia.

## Descripción del proyecto

Krelavo Store es un proyecto académico que consiste en el diseño y desarrollo de una tienda virtual especializada en productos de belleza, cuidado facial y cuidado capilar.

El proyecto comenzó con el diseño de un prototipo interactivo en Figma y posteriormente continuó con el desarrollo de una aplicación de escritorio en Java.

Su objetivo es ofrecer una experiencia de navegación sencilla, organizada y agradable para los usuarios.

## Objetivo

Diseñar y desarrollar una aplicación para una tienda de productos de belleza, incorporando funcionalidades básicas de navegación, búsqueda y gestión de productos.

## Etapas del proyecto

- **Prototipo en Figma:** diseño de la interfaz y navegación entre las diferentes pantallas de la tienda.
- **Desarrollo en Java:** construcción de una aplicación de escritorio con inicio de sesión y catálogo de productos.

## Funcionalidades

- Inicio de sesión.
- Catálogo de productos.
- Búsqueda de productos.
- Filtros por categoría, marca y precio.
- Marcas destacadas.
- Productos favoritos.
- Contador de productos agregados al carrito.

## Tecnologías utilizadas

- Java 8.
- JavaFX 8.
- NetBeans 25.
- Apache Ant.
- Figma.

## Equipo de trabajo

- Sofía Padrón
- Daniela Cárdenas
- José Pacheco
- Santiago

## Prototipo interactivo

[Ver prototipo de Krelavo Store en Figma](https://www.figma.com/proto/TFxeXIvMGm6Uprt4WHDnxM/Krelavo-Store---Prototipo?node-id=1-2&t=X1f1Wyk973MwDhkK-0&scaling=min-zoom&content-scaling=fixed&page-id=0%3A1&starting-point-node-id=32%3A131)

## Estado del proyecto

Actualmente, Krelavo Store se encuentra en etapa de desarrollo de su aplicación de escritorio en Java. Se continúa trabajando en la implementación de las funcionalidades y en la integración de los elementos diseñados en el prototipo de Figma.
>>>>>>> c7dcc6a217726b9be6dcd08b4e47384202417fe6
