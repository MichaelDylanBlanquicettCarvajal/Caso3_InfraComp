package seguridad20222_servidor;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.SecretKey;

public class Client {
    public static final int PUERTO =4030;
    public static final String SERVIDOR = "localhost";

    private String p;
    private String g;
    private String gx;
    private BigInteger gy;
    private SecurityFunctions sc;
    private String signature;
    private PublicKey publicaServer;


    private SecretKey sk_srv;
    private SecretKey sk_mac;
    private BigInteger llave_maestra;

    public void Client() {
        
    }

    public void procesar(BufferedReader stdln, BufferedReader pIn, PrintWriter pout) throws Exception
    {
        boolean ck = false;




        System.out.println("Escriba un numero ");
        String secure_int =  stdln.readLine();
        pout.println(secure_int);// * envío secure Int

        this.publicaServer = sc.read_kplus("datos_asim_srv.pub",pIn.readLine());


        this.g= pIn.readLine();// * llegada de G
        System.out.println("Esta es tu G:"+this.g);
        this.p = pIn.readLine();// * llegada de P
        System.out.println("Esta es tu P: "+this.p);

        this.gx = pIn.readLine();// * llegada de G^x
        System.out.println("Esta es tu gx: "+this.gx);




        String expected = this.g +","+this.p+"," +this.gx;// el mensaje correcto
        this.signature = pIn.readLine();//Firma digital
        byte[] bsing = str2byte(this.signature);
        System.out.println("Esta es la firma electronica: "+ signature);
         try {
            ck =sc.checkSignature(publicaServer, bsing,expected);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("No se ha podido revisar la firma");// cuando no se puede hacer el check
        }
        if (ck)
        {
            pout.println("OK");//*  envío por le canal cuando está bien
            BigInteger gB = new BigInteger(this.g);// g
            BigInteger pB = new BigInteger(this.p);// p
            SecureRandom r = new SecureRandom();// X
            int x = Math.abs(r.nextInt());
            Long longx = Long.valueOf(x);
    		BigInteger Bx = BigInteger.valueOf(longx);


            this.gy = G2Y(gB, Bx, pB);// generate Gy
            pout.println(this.gx.toString());//  * Envio 6b Gy

            BigInteger gxB = new BigInteger(this.gx);
            this.llave_maestra = calcular_llave_maestra(gxB, Bx, pB);
    		System.out.println(" llave maestra: " + this.llave_maestra);



            

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

    public byte[] str2byte( String ss)
	{	
		// Encapsulamiento con hexadecimales
		byte[] ret = new byte[ss.length()/2];
		for (int i = 0 ; i < ret.length ; i++) {
			ret[i] = (byte) Integer.parseInt(ss.substring(i*2,(i+1)*2), 16);
		}
		return ret;
	}
	
	public String byte2str( byte[] b )
	{	
		// Encapsulamiento con hexadecimales
		String ret = "";
		for (int i = 0 ; i < b.length ; i++) {
			String g = Integer.toHexString(((char)b[i])&0x00ff);
			ret += (g.length()==1?"0":"") + g;
		}
		return ret;
	}

    public BigInteger G2Y (BigInteger base, BigInteger expoenent, BigInteger modulo)
    {
        return base.modPow(expoenent, modulo);

    }
    private BigInteger calcular_llave_maestra(BigInteger base, BigInteger exponente, BigInteger modulo) {
		return base.modPow(exponente, modulo);
	}
  
 

}
