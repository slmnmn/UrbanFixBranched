# Proyecto: UrbanFix
- Dylan Gutierrez
- Samuel Gonzalez
- Felipe Aguilar
- Simón Rincón
## Descripción General

**UrbanFix** es una aplicación móvil diseñada para reportar daños o incidencias en el espacio público de manera rápida y eficiente. El objetivo del proyecto es facilitar la comunicación entre los ciudadanos y las entidades responsables del mantenimiento urbano, generando una interacción directa entre ellos, para lograr de manera conjunta la solución a algunos problemas que afectan la calidad de vida de los Bogotanos.  
El sistema permite que el usuario capture una imagen, agregue una descripción del problema (Por medio de Inteligencia Artificial) y envíe el reporte con su ubicación geográfica.

La aplicación está desarrollada en **Kotlin** utilizando **Jetpack Compose** para la interfaz de usuario y sigue una arquitectura modular que separa la lógica del negocio de la presentación.  
El backend se conecta a través de servicios REST y el almacenamiento de datos se gestiona mediante Postgre SQL. Cabe resaltar que dicha base de datos esta alojadá alojada en los servidores de AWS.

---

## Tecnologías Principales

- **Lenguaje:** Kotlin  
- **Framework UI:** Jetpack Compose  
- **Gestión de estado:** ViewModel + State Hoisting  
- **Navegación:** Navigation Compose  
- **Servicios en la nube:** PostgreSQL (Storage) alojado en **AWS**  
- **Arquitectura:** MVVM (Model-View-ViewModel)  
- **Otras librerías:** Coil (para carga de imágenes), Retrofit (para API REST)
- **AGP:** 8.11.1 (por temas de compatibilidad entre las versiones del Android studio.)
- **SDK:** 36
- **Integraciones:** Mapbox (Para el mapa), Gemini AI  

---

## Estructura del Proyecto
<img width="600" height="561" alt="image" src="https://github.com/user-attachments/assets/31d83366-6d1d-4a39-9188-15af4934569f" />
<img width="600" height="792" alt="image" src="https://github.com/user-attachments/assets/1b0d612f-0375-4348-b0df-73c110b7c764" />
<img width="597" height="818" alt="image" src="https://github.com/user-attachments/assets/534d572c-1ed3-4546-b0d1-ef5fda070d91" />


- **screens/**
  - Contiene todos los archivos `.kt` correspondientes a las diferentes pantallas de la aplicación.
  - Se reutilizan componentes como la barra superior e inferior, alertas, y fondos con imágenes.
  - Ejemplo: `ProfileScreen.kt` muestra la información del usuario o empresa y gestiona acciones como editar perfil, cerrar sesión y eliminar cuenta.

- **navigation/**
  - Gestiona la navegación entre pantallas mediante `NavHost` y `Composable`.
  - Define todas las rutas dentro de una clase sellada (`sealed class Pantallas`), incluyendo pantallas con argumentos dinámicos como `MisReportes/{userId}` o `Reportar/{reportType}`.
  - Archivo principal: `AppNavigator.kt`, donde se configura el flujo de navegación completo de la app.

- **services/**
  - Contiene la integración con la **IA de Gemini**, utilizando el modelo `gemini-2.0-flash:generateContent`.
  - Se encarga de la conexión con el servicio para generar descripciones automáticas a partir de imágenes o texto.

- **network/**
  - Maneja toda la comunicación con la base de datos.
  - Incluye las *data classes* necesarias para peticiones y respuestas (usuarios, reportes, registros, etc.).
  - Está configurado para conectarse a un servidor local vinculado a **AWS**.

- **data/**
  - Contiene la clase `UserPreferencesManager`, encargada de recordar credenciales (correo y contraseña) cuando se selecciona la opción “Recordarme” en el login.
  - Utiliza almacenamiento local seguro para mantener la sesión iniciada.

- **viewmodel/**
  - Incluye los diferentes `ViewModel` utilizados en la aplicación (login, registro, perfil, etc.).
  - Ejemplo: `OlvidarconViewModel.kt`, que controla el flujo del restablecimiento de contraseña, validaciones y estados (`Loading`, `Error`, `EmailSentSuccess`, etc.).
  - Utiliza corrutinas y `MutableStateFlow` para manejar estados reactivos en Compose.

- **res/**
  - Contiene los recursos visuales y de configuración:
    - Imágenes y logotipos.
    - Archivos `strings.xml` para textos y traducciones.
    - Icono de la aplicación.
    - Configuración de la API de **Mapbox**, que muestra los mapas dentro de la app.
  - Algunas pantallas utilizan imágenes como fondo para mantener un diseño visual consistente.

---




## Front-End con Jetpack Compose

El front-end de UrbanFix fue implementado completamente en **Jetpack Compose**, adoptando un enfoque declarativo y modular. Cada pantalla está compuesta por funciones `@Composable` reutilizables y organizadas en componentes independientes.

### 1. Arquitectura UI

Cada pantalla (por ejemplo, `HomeScreen`, `ReportScreen`) tiene un **ViewModel** asociado que maneja su estado y lógica.  
El estado fluye desde el ViewModel hacia los componentes de Compose, garantizando una UI reactiva y sincronizada.

```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val state = viewModel.uiState.collectAsState()
    HomeContent(
        reports = state.value.reports,
        onReportClick = { viewModel.selectReport(it) }
    )
}
```
El ViewModel usa un `UiState` (data class) con propiedades inmutables y un `MutableStateFlow` para gestionar los cambios.  
Esto permite que Compose detecte automáticamente actualizaciones y recomponga la interfaz sin necesidad de `LiveData` ni callbacks.

El flujo de datos es unidireccional:
1. El usuario interactúa con la UI.  
2. El ViewModel actualiza el estado (`_uiState`).  
3. La interfaz se recompone de forma reactiva.

Este enfoque mantiene la UI sincronizada, separa la lógica de negocio y facilita mantenimiento y escalabilidad.
### 2. Navegación

La navegación entre pantallas se realiza con Navigation Compose, definiendo rutas en un NavGraph central, alojado en la carpetqa de `navigaation/AppNavigator.kt`.

```kotlin
NavHost(
    navController = navController,
    startDestination = Routes.Home
) {
    composable(Routes.Home) { HomeScreen(navController) }
    composable(Routes.Report) { ReportScreen(navController) }
    composable(Routes.Profile) { ProfileScreen(navController) }
}
```
Las rutas están definidas en una clase de constantes para mantener el código limpio y evitar errores tipográficos.

### 3. Componentes Reutilizables
El diseño de UrbanFix se basa en la reutilización de componentes visuales. Algunos ejemplos son:

- `AppButton:` botón personalizado con estilo uniforme.
- `TextInputField:` campo de texto con validación.
- `ReportCard:` tarjeta visual para mostrar reportes en lista.

**Nota:** También se reutilizaron las barras superiores de navegación, donde solo se les cambio el texto, la barra inferior y algunas alertas como las de copiado del código del reporte, ya que se repetían y no cambiaban ningún apartado.

### 4. Integración con IA
La aplicación utiliza un módulo de inteligencia artificial que genera automáticamente una **descripción del reporte** a partir de los parámetros dados por el usuario, como por ejemplo la dirección del evento, el punto de referencia y el tipo de evento (como iluminaria intermitente, etc....).
Este proceso se realiza mediante una petición al modelo de lenguaje de gemini llamado `gemini-2.0-flash:generateContent`, mejorando la experiencia del usuario y reduciendo el tiempo de redacción manual.

### 5. Optimización
- La carga de imágenes se optimizó con Coil, permitiendo previsualización y escalado eficiente.
- Se aplicó manejo de estados de carga `(Loading, Success, Error)` para ofrecer retroalimentación clara al usuario.
---

### Para instalarlo:


....



