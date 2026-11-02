import socket
import subprocess
import os
import time
# Configuración del servidor/escucha UDP
LISTEN_IP = "0.0.0.0" 
LISTEN_PORT = 5005  # Debe coincidir con el puerto de tu app de Android

# CONFIGURACIÓN DE LOS 18 BOTONES (CUADRÍCULA 3x6)
# Estructura: "TRAMA_RECIBIDA": ["Ruta_Aplicacion", "Argumento_1", "Argumento_2", ...]
COMANDOS_CONFIG = {
    # --- FILA 0 ---
    "BOTON_F0_C0": ["D:\\Games\\psxfin\\psxfin.exe", "-f", "D:\\Games\\psxfin\\cdimages\\(PS1) Crash Bandicoot 2 [PAL] [ SCES-00967].bin"], 
    "BOTON_F0_C1": ["D:\\Games\\psxfin\\psxfin.exe", "-f", "D:\\Games\\psxfin\\cdimages\\CTR - Crash Team Racing (USA).bin"], 
    "BOTON_F0_C2": ["D:\\Games\\psxfin\\psxfin.exe", "-f", "D:\\Games\\psxfin\\cdimages\\Disney's Hercules [U] [SLUS-00529].bin"],
    "BOTON_F0_C3": ["D:\\Games\\psxfin\\psxfin.exe", "-f", "D:\\Games\\psxfin\\cdimages\\Spyro the Dragon (USA).bin"],
    #"BOTON_F0_C4": ["D:\\Games\\psxfin\\psxfin.exe", "-f", ""],
    "BOTON_F0_C5": ["D:\\Games\\psxfin\\psxfin.exe", "-f", "D:\\Games\\psxfin\\cdimages\\\Overboard! (Europe)\\Overboard! (Europe) (Track 01).bin"],

    # --- FILA 1 ---
    "BOTON_F1_C0": ["C:\\Project64-3.0.1\\Project64.exe", "-f", "C:\\Project64-3.0.1\\roms\\mario64.zip"],
    "BOTON_F1_C1": ["C:\\Project64-3.0.1\\Project64.exe", "-f", "C:\\Project64-3.0.1\\roms\\marioParty.zip"],
    "BOTON_F1_C2": ["C:\\Project64-3.0.1\\Project64.exe", "-f", "C:\\Project64-3.0.1\\roms\\smash.zip"],
    #"BOTON_F1_C3": [""],
    #"BOTON_F1_C4": [""],
    "BOTON_F1_C5": ["stopall"],

    # --- FILA 2 ---
    #"BOTON_F2_C0": [""],
    #"BOTON_F2_C1": [""],
    #"BOTON_F2_C2": [""],
    #"BOTON_F2_C3": [""],
    "BOTON_F2_C4": ["shutdown", "-f", "/s", "/t", "10"],
    #"BOTON_F2_C5": [""],
}

def ejecutar_comando_windows(trama):
    """Busca la trama en la configuración y ejecuta el comando con sus argumentos."""
    if trama in COMANDOS_CONFIG:
        comando_completo = COMANDOS_CONFIG[trama]
        
        print(f"[➔] Cerrando en Windows: psxfin")
        os.system("taskkill /f /im psxfin.exe")
        print(f"[➔] Cerrando en Windows: project64")
        os.system("taskkill /f /im project64.exe")
        time.sleep(3) 
        print(f"[➔] Ejecutando en Windows: {' '.join(comando_completo)}")
        
        try:
            # Popen lanza la aplicación de forma asíncrona (no bloquea el script de Python)
            subprocess.Popen(comando_completo)
        except FileNotFoundError:
            print(f"[!] Error: No se encontró el ejecutable especificado en: {comando_completo}")
        except Exception as e:
            print(f"[!] Error al intentar abrir la aplicación: {e}")
    else:
        print(f"[?] Trama '{trama}' recibida, pero no tiene ningún comando configurado.")

def iniciar_cliente_udp():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    
    try:
        sock.bind((LISTEN_IP, LISTEN_PORT))
        print(f"[*] Servidor UDP escuchando en {LISTEN_IP}:{LISTEN_PORT}")
        print("[*] Esperando comandos de la app Android 3x6... (Ctrl+C para salir)\n")
        
        while True:
            data, addr = sock.recvfrom(1024)
            
            try:
                # Decodificar el texto enviado por el móvil
                trama_recibida = data.decode('utf-8').strip()
                print(f"[+] Trama recibida desde {addr}: {trama_recibida}")
                
                # Procesar y lanzar el comando correspondiente
                ejecutar_comando_windows(trama_recibida)
                print("-" * 60)
                
            except UnicodeDecodeError:
                print(f"[-] Recibida trama binaria no válida desde {addr}")

    except KeyboardInterrupt:
        print("\n[-] Deteniendo el receptor UDP...")
    finally:
        sock.close()
        print("[*] Socket cerrado de forma segura.")

if __name__ == "__main__":
    iniciar_cliente_udp()
