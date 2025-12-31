#  AppMovilNotas

Aplicación móvil para la **gestión de estudiantes, tareas y notas** desarrollada en **Android** con **Kotlin, Jetpack Compose, Room y ViewModel**.

---

## Características

- Gestión de estudiantes: crear, editar y visualizar promedio.
- Gestión de tareas: crear, editar, eliminar y buscar.
- Asignación de notas: edición directa en la UI.
- Cálculo automático del promedio con **Flow/StateFlow**.
- Persistencia local con **Room**.
- Navegación entre pantallas con **Navigation Compose**.
- UI moderna con **Material3** y Jetpack Compose.
- Muestra en pdf la lista de estudiantes,

---

## Tecnologías usadas

- **Kotlin**
- **Jetpack Compose**
- **Room**
- **ViewModel + StateFlow**
- **Navigation Compose**
- **Material3**

---

##  Instrucciones de compilación

1. Clonar el repositorio:
   ```bash
   git clone https:https://github.com/evelynLizeth/App-movil-Notas

2. Abrir el proyecto en Android Studio (versión recomendada: Arctic Fox o superior).
3. Asegurarse de tener instalado el SDK de Android 24+.
4. Sincronizar dependencias con Gradle:
5. File → Sync Project with Gradle Files. 6. Ejecutar en un emulador o dispositivo físico:
6. Run → Run 'app'.
## Screenshots
  1.  Lista de estudiantes
   Lista de estudiantes con nombre y promedio general.
    Podemos crear, editar y seleccionar registros para eliminar.
    Podemos imprimir en PDF la lista de estudiantes y sus promedios.
   Al hacer clic en el promedio se abre la pantalla detalle del estudiante.
      (docs/screenshots/main_screen.png)


  2.  Detalle de notas
   Muestra el nombre, tareas y notas editables.
   El promedio se recalcula automáticamente.
      (docs/screenshots/main_screen.png)

   3. Gestión de tareas
   Gestión de tareas globales: crear, editar y eliminar.
   tareas
      (docs/screenshots/main_screen.png)

## Decisiones de diseño
- En la primera pantalla decidi poner una vista general de los nombres de los alumnos y su promedio genera,
desde la cual podemos  buscar los nombres de los alumnos, en la siguiente linea podemos acceder al panel de herramientas en el cual
podemos  crear nuevos registros, editarlos y seleccionar varios para eliminar 
con un solo clic, seguido tenemos el boton de imprimir, todos estos botones los cree con imagenes para que sea facil de usar y 
unicamente el boton de crear tareas tiene un diseño de boton tradicional dandole un diseño llamativo  a la app.
- He usado la paleta de colores: azul para titulos y botones tradicionales, las letras en color negro, lascabeceras de tablas en 
color plomo tono acentado, las lineas de las tablas son intercaladas tono plomo suave y sin color para mejor manejo de la información,
las letras de las cabeceras de las tablas en negro con negrita, todos los campos editables tiene una linea inferior para que facilmente el usuario
sepa que puede escribir en esas partes, tiene mensajes de confirmacon de cada accion.
- Reactividad con Flow/StateFlow: el promedio se recalcula automáticamente al editar notas, evitando lógica manual en la UI.
- Persistencia en Room: todas las entidades (Student, Task, Grade) se almacenan en base de datos local.
- Tablas con filas alternadas para mejor legibilidad.
- Encabezados en negrita.
- Botones alineados según diseño.
- Navegación declarativa: cada pantalla se define como un composable en el NavGraph.
- Material3: estilos modernos.



