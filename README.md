
# Control Remoto UDP de 18 Botones (Android KitKat + Python Receiver)

![](https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEglYlhhdXa-S3l9HXqmF2j9kXr_B7eXfjdUQRSjyZ9fa0eH2QQw70PenY6cfpEez21kxwiPpW6m-H8Eyf7_csgXhS0ifc6SAQ7DcIFRlmioZZP4uV0aGapPSLYAdxUISJVabQVxwjWiM6H9U8_ZWMTvdpW0RIVFe-PBsIqjugWRxNwYtlNR8_zIzr_RzXJS/w640-h338/Captura%20de%20pantalla%202026-11-02%20103229.png)

Este proyecto consiste en un ecosistema de control remoto compuesto por una aplicación móvil para **Android 4.4 (KitKat)** desarrollada bajo el entorno **Eclipse ADT** y un script receptor en **Python 3** para Windows. Permite el lanzamiento remoto de aplicaciones del sistema con argumentos, así como la activación de ordenadores apagados mediante el protocolo **Wake-on-LAN (WoL)**.

## 📱 Características de la Aplicación Android

* **Diseño optimizado (3x6):** Cuadrícula de 18 botones (`ImageButton`) adaptada nativamente mediante `LinearLayout` anidados para garantizar su correcta visualización y compatibilidad en dispositivos antiguos sin depender de librerías externas propensas a fallos de renderizado.
* **Configuración Dinámica:** Menú de ajustes integrado (`Settings`) que permite modificar la dirección IP y el puerto UDP de destino en caliente. Los datos se guardan de forma persistente a través de `SharedPreferences`.
* **Transmisión Asíncrona:** El envío de tramas de red se gestiona mediante un hilo secundario (`AsyncTask`), evitando la congelación de la interfaz de usuario (errores ANR).
* **Soporte Wake-on-LAN:** El último botón de la cuadrícula (Fila 2, Columna 5) genera e interactúa con un paquete binario puro (**Magic Packet**) enviado por *broadcast* (`255.255.255.255:9`) para encender equipos de la red local de forma remota.

## 🐍 Características del Receptor Python (Windows)

* **Escucha Continua:** Socket UDP asíncrono configurado para recibir y decodificar datos en bucle.
* **Mapeo Automático de Comandos:** Diccionario interno parametrizado para asociar cada uno los 18 botones de la cuadrícula a un programa o comando de la consola de Windows con sus respectivos argumentos de ejecución.

---

## 🛠️ Requisitos e Instalación

### 1. Entorno Android (Dispositivo / Eclipse)
* **IDE:** Eclipse con el plugin ADT (Android Development Tools).
* **Versión Mínima de SDK:** API 19 (Android 4.4 KitKat).
* **Nivel del Compilador Java:** Configurar el proyecto en `Java Compliance Level 1.6` o superior desde las propiedades del proyecto en Eclipse para el correcto soporte de anotaciones en interfaces.
* **Permisos:** La app requiere el permiso de red en el archivo `AndroidManifest.xml`:
  ```xml
  <uses-permission android:name="android.permission.INTERNET" />
  ```

### 2. Entorno Python (Servidor de comandos)
* **Versión:** Python 3.x instalado en el sistema operativo Windows.
* **Dependencias:** Ninguna (utiliza exclusivamente librerías estándar nativas como `socket` y `subprocess`).

---

## 🚀 Guía de Configuración y Uso

### Paso 1: Configurar el Receptor en la PC
1. Abre el script `launcher.py` y edita el diccionario `COMANDOS_CONFIG`.
2. Modifica o añade las rutas de los programas que desees abrir. *Recuerda escapar las barras invertidas utilizando doble barra (`\\`) para las rutas de Windows.*
3. Abre una terminal de Windows (`cmd`), navega hasta la carpeta del script y ejecútalo:
   ```bash
   python launcher.py
   ```
4. El terminal se quedará a la escucha mostrando su dirección y puerto actual (`0.0.0.0:5005`).

### Paso 2: Configurar la Aplicación Android
1. Conecta tu dispositivo Android a la **misma red Wi-Fi** que el ordenador.
2. Compila e instala el archivo `.apk` generado por Eclipse en el dispositivo.
3. Abre la aplicación, presiona el botón físico de menú (o los tres puntos superiores de la pantalla) y selecciona **Ajustes**.
4. Introduce la **IP local de tu ordenador** (por ejemplo, `192.168.1.45`) y el puerto correspondiente (`5005`). Guarda los cambios.

### Paso 3: Configurar Wake-on-LAN (Opcional)
Para utilizar el botón de encendido remoto (Fila 2, Columna 5):
1. Abre `MainActivity.java` y coloca la dirección MAC de la tarjeta de red de tu PC en la variable `macTarget`.
2. Asegúrate de tener activada la opción **Wake-on-LAN** en la BIOS/UEFI de tu ordenador.
3. En el Administrador de dispositivos de Windows, ve a las propiedades de tu adaptador de red y permite que el dispositivo reactive el equipo mediante el uso de *Magic Packets*.

---

## 📐 Estructura de Mensajes UDP (Tramas)

Los botones convencionales envían una cadena de texto codificada en UTF-8 con la estructura de coordenadas del botón presionado:

| Botón | Coordenadas | Trama de texto enviada |
| :--- | :--- | :--- |
| Botón 1 | Fila 0, Columna 0 | `"BOTON_F0_C0"` |
| Botón 6 | Fila 0, Columna 5 | `"BOTON_F0_C5"` |
| Botón 13 | Fila 2, Columna 0 | `"BOTON_F2_C0"` |
| Botón 18 (WoL) | Fila 2, Columna 5 | *Envía Magic Packet binario (102 bytes de payload)* |

---

## 🛑 Resolución de Problemas

* **La app se instala pero la pantalla sale vacía:** Asegúrate de realizar un **Project -> Clean** en Eclipse para forzar la sincronización del archivo de recursos `R.java`. Comprueba también que la imagen `ic_launcher` exista en las carpetas `res/drawable`.
* **El script de Python se abre y se cierra al instante:** No lo ejecutes haciendo doble clic. Abre una consola de comandos `cmd` manualmente y lánzalo desde allí para poder diagnosticar la excepción (generalmente provocada por un puerto ocupado o por errores de sintaxis al escribir rutas con barras simples en el diccionario).
* **Los paquetes no llegan al receptor:** Verifica que el Firewall de Windows no esté bloqueando las conexiones entrantes de Python o del puerto UDP `5005`.
