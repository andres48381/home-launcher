package com.example.homelauncher;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends Activity implements View.OnClickListener {

    // Configura la IP de tu PC y el puerto aquí
    private static final String DEST_IP = "192.168.1.39";
    private static final int DEST_PORT = 5005;

    // Matriz para referenciar los 18 botones
    private ImageButton[][] buttons = new ImageButton[3][6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 1. Forzar la carga del XML corregido
        setContentView(R.layout.activity_main);

        // 2. Mapeo explícito y seguro para KitKat (evita fallos de identificadores dinámicos)
        try {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 6; c++) {
                    String buttonIdStr = "btn_" + r + "_" + c;
                    int resId = getResources().getIdentifier(buttonIdStr, "id", getPackageName());
                    
                    if (resId != 0) {
                        buttons[r][c] = (ImageButton) findViewById(resId);
                        if (buttons[r][c] != null) {
                            buttons[r][c].setOnClickListener(this);
                            buttons[r][c].setTag(new int[]{r, c}); // Guardamos fila y columna
                        } else {
                            Log.e("UDP_APP", "No se pudo vincular el boton: " + buttonIdStr);
                        }
                    } else {
                        Log.e("UDP_APP", "No se encontro el ID de recurso para: " + buttonIdStr);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("UDP_APP", "Error inicializando botones: " + e.getMessage());
        }
    }

    // REEMPLAZA ESTE MÉTODO EN TU MAINACTIVITY.JAVA
    public void onClick(View v) {
        int[] coords = (int[]) v.getTag();
        if (coords != null) {
            int row = coords[0];
            int col = coords[1];
            
            // ASIGNAMOS EL ÚLTIMO BOTÓN (Fila 2, Columna 5) PARA EL WAKE-ON-LAN
            if (row == 2 && col == 5) {
                // PON AQUÍ LA DIRECCIÓN MAC DE TU ORDENADOR (separada por dos puntos o guiones)
                String macTarget = "64:31:50:68:48:55"; 
                
                // Enviamos un mensaje especial que la clase AsyncTask detectará para crear los bytes
                new SendUdpTask().execute("WOL:" + macTarget);
            } else {
                // El resto de los botones siguen funcionando enviando su texto normal al script de Python
                String mensaje = "BOTON_F" + row + "_C" + col;
                new SendUdpTask().execute(mensaje);
            }
        }
    }

    // Tarea en segundo plano para no bloquear la interfaz gráfica
    // REEMPLAZA ESTA CLASE INTERNA EN TU MAINACTIVITY.JAVA
    private class SendUdpTask extends AsyncTask<String, Void, Boolean> {
        private String msgEnviado;

        @Override
        protected Boolean doInBackground(String... params) {
            msgEnviado = params[0];
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                
                byte[] buf;
                InetAddress serverAddr;
                int port;

                // Si el mensaje empieza por "WOL:", calculamos el Magic Packet
                if (msgEnviado.startsWith("WOL:")) {
                    String macStr = msgEnviado.substring(4);
                    
                    // Limpiar la MAC de separadores (: o -) y convertir a bytes
                    String cleanMac = macStr.replaceAll("[:\\-]", "");
                    byte[] macBytes = new byte[6];
                    for (int i = 0; i < 6; i++) {
                        macBytes[i] = (byte) Integer.parseInt(cleanMac.substring(i * 2, (i * 2) + 2), 16);
                    }

                    // Estructura WoL: 6 bytes de 0xFF + 16 veces la MAC (6 * 16 = 96 bytes) = 102 bytes en total
                    buf = new byte[6 + 16 * 6];
                    for (int i = 0; i < 6; i++) {
                        buf[i] = (byte) 0xff;
                    }
                    for (int i = 6; i < buf.length; i += 6) {
                        System.arraycopy(macBytes, 0, buf, i, 6);
                    }

                    // El Magic Packet de WoL se envía obligatoriamente por Broadcast global (255.255.255.255)
                    // O a la IP de broadcast de tu subred (ej. 192.168.1.255) en el puerto 9
                    serverAddr = InetAddress.getByName("255.255.255.255");
                    port = 9; 
                    socket.setBroadcast(true); // Permitir el envío broadcast en la red local
                } else {
                    // Comportamiento normal para el resto de botones hacia el script de Python
                    buf = msgEnviado.getBytes("UTF-8");
                    serverAddr = InetAddress.getByName(DEST_IP); // Usa la IP de tu PC estática
                    port = DEST_PORT; // Usa tu puerto 5005
                }

                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, port);
                socket.send(packet);
                return true;
                
            } catch (Exception e) {
                Log.e("UDP_NET", "Error enviando: " + e.getMessage());
                return false;
            } finally {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                if (msgEnviado.startsWith("WOL:")) {
                    Toast.makeText(MainActivity.this, "¡Magic Packet enviado!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Enviado: " + msgEnviado, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Error al transmitir paquete", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
