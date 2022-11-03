package seguridad20222_servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;

public class ProtocoloCliente {
    private static String p ;
    private static String  g;
    private static String gx;
    private static BigInteger gy;
    private SecurityFunctions sc;
    private Byte[] singnature; //F(k_w-,(g,p,gx)

    public static void procesar(BufferedReader stdln, BufferedReader pIn, PrintWriter pout) throws IOException
    {
        System.out.println("Escriba un numero ");
        String secure_int =  stdln.readLine();
        pout.println(secure_int);// envío secure Int

        this.setG(pIn.readLine());// llegada de G
        this.p = pIn.readLine();// llegada de P
        this.gx = pIn.readLine();// llegada de G^x
        String expected = this.g +","+this.p+"," +this.gx;// el mensaje correcto
        this.singnature = pIn.readLine();
        boolean ck =sc.checkSignature(singnature,expected);
        if (ck)
        {
            pout.println("OK");
            pout.println();
        }
        else
        {
            pout.println("ERROR");
        }






    }


}
