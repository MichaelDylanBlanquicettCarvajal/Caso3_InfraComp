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
    private SecurityFunctions sc;
    private String signature;
    private PublicKey publicaServer;

    private SecretKey sk_srv;
    private SecretKey sk_mac;
    private BigInteger llave_maestra;

    private BufferedReader lector;
    private PrintWriter escritor;


    
    public Client(Socket skt) throws IOException {
        this.escritor = new PrintWriter(skt.getOutputStream() , true);
		this.lector = new BufferedReader(new InputStreamReader(skt.getInputStream()));
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
        System.out.println("Esta es la firma electronica: "+ signature);
         try {
            ck = sc.checkSignature(publicaServer, str2byte(this.signature), expected);
            
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
    		System.out.println(" llave maestra: " + this.llave_maestra.toString());//* calculo de llave maestra
            this.sk_srv = sc.csk1( this.llave_maestra.toString());// * calculo K_AB1
            this.sk_mac = sc.csk2(this.llave_maestra.toString());// *calculo K_AB2
            byte[] iv1=generateIvBytes();// * genera iv1


            /**
             * Parte 8
             */
            System.out.println("Escriba su consulta ");
            String consulta =  stdln.readLine();
            IvParameterSpec iv1_ = new IvParameterSpec(iv1);

            byte[] cconsulta = sc.senc(str2byte(consulta), sk_srv, iv1_, "Cliente");
            byte[] chmac = sc.hmac(str2byte(consulta),sk_mac);



            pout.println(byte2str(cconsulta));//* envío consulta cifrada
            pout.println(byte2str(chmac));//* envío hmac 
            pout.println(byte2str(iv1));//* envío iv1

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
                boolean verificar =sc.checkInt(decifradoResp, sk_mac, hmacB);
                if (verificar)
                {
                    pout.println("OK");

                }
                else
                {
                    pout.println("ERROR");
                }

            }
            else if (response.compareTo("ERROR")==0)
            {
                throw new Exception("Client did not send matching query and MAC");
            }





            

        }

       

 

        
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
