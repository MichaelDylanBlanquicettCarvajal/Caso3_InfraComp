package seguridad20222_servidor;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.PublicKey;

public class Client {
    public static final int PUERTO =3040;
    public static final String SERVIDOR = "localhost";

    private String p;
    private String g;
    private String gx;
    private BigInteger gy;
    private SecurityFunctions sc;
    private String signature;

    public void Client() {
        
    }

    public   void procesar(BufferedReader stdln, BufferedReader pIn, PrintWriter pout) throws IOException
    {
        System.out.println("Escriba un numero ");
        String secure_int =  stdln.readLine();
        pout.println(secure_int);// envío secure Int

        this.g= pIn.readLine();// llegada de G
        this.p = pIn.readLine();// llegada de P
        this.gx = pIn.readLine();// llegada de G^x
        String expected = this.g +","+this.p+"," +this.gx;// el mensaje correcto
        this.signature = pIn.readLine();
        // no tengo la llave publica 
        //boolean ck =sc.checkSignature(,this.signature,expected);
        boolean ck = false;
        if (ck)
        {
            pout.println("OK");// envío por le canal
            pout.println();
        }
        else
        {
            pout.println("ERROR");
        }






    }

    public static void main(String[] args) throws IOException {
        Client cliente = new Client();
        
        Socket socket = null;
        PrintWriter escritor = null;
        BufferedReader lector = null;
        System.out.println("Cliente....");
        try{
            socket = new Socket(SERVIDOR, PUERTO);
            escritor = new PrintWriter(socket.getOutputStream(),true);
            lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (IOException e )
        {
            e.printStackTrace();
            System.exit(-1);
        }
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        cliente.procesar(stdIn, stdIn, escritor); 
        stdIn.close();
        lector.close();
        escritor.close();
        socket.close();

    }

  
 

}