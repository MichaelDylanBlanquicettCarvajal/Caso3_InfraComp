package seguridad20222_servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import seguridad20222_servidor.Client;
import seguridad20222_servidor.SrvThread;

public class Main {

    public static final int PUERTO = 4030;
    public static final String SERVIDOR = "localhost";
    
    public static void main(String[] args) throws IOException {
        Socket socket = null;
        PrintWriter escritor = null;
        BufferedReader lector = null;
        System.out.println("Cliente....");
        try {
            socket = new Socket(SERVIDOR, PUERTO);
            escritor = new PrintWriter(socket.getOutputStream(), true);
            lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        Client cliente = new Client(socket);
        try {
            cliente.procesar(stdIn, lector, escritor);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        stdIn.close();
        lector.close();
        escritor.close();
        socket.close();

    }
}
