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
import javax.crypto.spec.IvParameterSpec;

public class Client extends Thread {


    private String p;
    private String g;
    private String gx;
    private BigInteger gy;

    private SecurityFunctions sc  = new SecurityFunctions();
    private String signature;
    private PublicKey publicaServer = null;

    private SecretKey sk_srv;
    private SecretKey sk_mac;
    private BigInteger llave_maestra;


    private Socket socket;

    private Integer secure_Init;
    private Integer numConsulta;

    private static long tiempoCifrar = 0;
    private static long tiempoConsulta = 0;
    private static long tiempoAutentic = 0;
    private static long tiempoverificarFirma = 0;
    private static long tiempocalcularGy=0;


    
    public Client(Socket socket,Integer secure_Init, Integer numConsulta) throws IOException {

        this.socket = socket;
        this.secure_Init = secure_Init;
        this.numConsulta = numConsulta;



    }

    public void run ()
    {
        try {
            PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            long start_1 = System.nanoTime();

            Integer ret_=procesar (  lector,  escritor,  this.secure_Init,  this.numConsulta);
            long end_1 = System.nanoTime();   
            this.tiempoConsulta+= (end_1-start_1);
            this.tiempoConsulta= tiempoConsulta/secure_Init;
            System.out.println("Tiempo en completar acumulado: " + (tiempoConsulta));   


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    public synchronized Integer procesar( BufferedReader pIn, PrintWriter pout, Integer secure_Init, Integer numConsulta ) throws Exception
    {
        boolean ck = false;




        pout.println(secure_Init);// * envío secure Int
        String dlg = pIn.readLine();

        this.publicaServer = sc.read_kplus("datos_asim_srv.pub",dlg);


        this.g= pIn.readLine();// * llegada de G
       this.p = pIn.readLine();// * llegada de P
        this.gx = pIn.readLine();// * llegada de G^x



        String expected = this.g +","+this.p+"," +this.gx;// el mensaje correcto
       this.signature = pIn.readLine();//Firma digital
        try {
            long start_3 = System.nanoTime();

            ck = sc.checkSignature(this.publicaServer, str2byte(this.signature), expected);
            
            long end_3 = System.nanoTime();
            this.tiempoverificarFirma+=end_3-start_3;
            this.tiempoverificarFirma= this.tiempoverificarFirma/secure_Init;
            System.out.println("Tiempo en verificar firma: "+this.tiempoverificarFirma);

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
    		BigInteger Bx = BigInteger.valueOf(1);


            long start_4 = System.nanoTime();
            this.gy = G2Y(gB, Bx, pB);// generate Gy
            long end_4 = System.nanoTime();
            this.tiempocalcularGy+=end_4-start_4;
            this.tiempocalcularGy= tiempocalcularGy/secure_Init;
            System.out.println("Tiempo generar Gy : "+this.tiempocalcularGy);

            pout.println(this.gy.toString());//  * Envio 6b Gy

            BigInteger gxB = new BigInteger(this.gx);
            this.llave_maestra = calcular_llave_maestra(gxB, Bx, pB);



            this.sk_srv = sc.csk1( this.llave_maestra.toString());// * calculo K_AB1
            this.sk_mac = sc.csk2(this.llave_maestra.toString());// *calculo K_AB2
            byte[] iv1=generateIvBytes();// * genera iv1


            /**
             * Parte 8
             */
        
           IvParameterSpec iv1_ = new IvParameterSpec(iv1);
            long start = System.nanoTime();
            byte[] cconsulta = sc.senc(str2byte(numConsulta.toString()), sk_srv, iv1_, "Cliente");
            long end = System.nanoTime();   
            this.tiempoCifrar+=end-start;
            this.tiempoCifrar = this.tiempoCifrar/secure_Init;
            System.out.println("Tiempo en cifrar: " + tiempoCifrar);   
            long start_2 = System.nanoTime();
            byte[] chmac = sc.hmac(str2byte(numConsulta.toString()),sk_mac);
            long end_2 = System.nanoTime();   
            this.tiempoAutentic+=end_2-start_2;
            this.tiempoAutentic= this.tiempoAutentic/secure_Init;
            System.out.println("Tiempo en autentic: " + tiempoAutentic);   


            String m1 = byte2str(cconsulta);
            String m2 = byte2str(chmac);
            String iv1_str = byte2str(iv1);


            pout.println(m1);//* envío consulta cifrada
            pout.println(m2);//* envío hmac 
            pout.println(iv1_str);//* envío iv1

            /**
             * parte 10, 11
             */
            String response=pIn.readLine();
           
            if (response.compareTo("OK")==0)
            {
                String cRes = pIn.readLine();
                String hmacResp=pIn.readLine();
                String iv2 = pIn.readLine();
                /**
                 * parte 12
                 */
                IvParameterSpec iv2_ = new IvParameterSpec(str2byte(iv2));
                byte[]  cResB = str2byte(cRes);
                byte[]  hmacB = str2byte(hmacResp);
                byte[] decifradoResp = sc.sdec(cResB, sk_srv, iv2_);
                boolean verificar =sc.checkInt( decifradoResp, sk_mac, hmacB);
                if (verificar)
                {
                    pout.println("OK");
                    String respuesta = byte2str(decifradoResp);
                    int numResponse = (Integer.parseInt(respuesta));
                    System.out.println("El resultado a su consulta es :"+ numResponse);
                    return 1;

                }
                else
                {
                    pout.println("ERROR");
                    System.out.println(dlg+" Server did not send matching query and MAC ");
                }

            }
            else if (response.compareTo("ERROR")==0)
            {
                System.out.println(dlg+" Client did not send matching query and MAC ");
            }





            

        }
        else 
        {
            pout.println("ERROR");//*  envío por le canal cuando está mal

        


        }


        return 1;


       

 

        
    }

    private byte[] generateIvBytes() {
	    byte[] iv = new byte[16];
	    new SecureRandom().nextBytes(iv);
	    return iv;
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
