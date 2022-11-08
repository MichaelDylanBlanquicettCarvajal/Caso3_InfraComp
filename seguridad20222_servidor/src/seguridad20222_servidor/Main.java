package seguridad20222_servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import seguridad20222_servidor.Client;
import seguridad20222_servidor.SrvThread;

public class Main {

    public static final int PUERTO = 4030;
    public static final String SERVIDOR = "localhost";
    
    public static void main(String[] args) throws IOException {
        Socket socket = null;
        PrintWriter escritor = null;
        BufferedReader lector = null;
        Scanner input = new Scanner(System.in);






        System.out.println("Cliente....");

        System.out.println("Escriba 1 si desea usar el usar concurrente ");
        System.out.println("Escriba 2 si desea usar el programa para un caso especifico ");
        System.out.println("Escriba 3 para salir  ");

        Integer response1 =input.nextInt();


        if (response1.equals(1))
        {
            System.out.println("Escriba el numero de clientes concurrentes: ");

            Integer numClientes =input.nextInt();
            for (int i = 0; i < numClientes; i++) {
                socket = new Socket(SERVIDOR, PUERTO);
                Client cliente = new Client(socket,i,10+i);
                cliente.start();

               
                
            }


        }
        else if (response1.equals(2))
        {
            System.out.println("Escriba su consulta: ");
            Integer consulta = input.nextInt();
            try {
                socket = new Socket(SERVIDOR, PUERTO);
                
                escritor = new PrintWriter(socket.getOutputStream(), true);
                lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Client cliente = new Client(socket, 1, consulta);
                cliente.start();
             

               
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
                
       


        }
        else 
        {
            lector.close();
            escritor.close();
            socket.close();
        }



       
      

  
    }
}
