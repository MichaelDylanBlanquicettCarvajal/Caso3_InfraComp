package seguridad20222_servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.PublicKey;

public class Client extends Thread {


    private String p;
    private String g;
    private String gx;
    private BigInteger gy;
    private SecurityFunctions sf;
    private String signature;
    private PublicKey publicaServer;
    private BufferedReader lector;
    private PrintWriter escritor;

    public Client(Socket skt) throws IOException {
        this.escritor = new PrintWriter(skt.getOutputStream() , true);
		this.lector = new BufferedReader(new InputStreamReader(skt.getInputStream()));
    }

    public void run() {
        System.out.println("Escriba un numero ");
        String secure_int;
        try {
            secure_int = lector.readLine();
            escritor.println(secure_int);// envío secure Int
            this.publicaServer = sf.read_kplus("datos_asim_srv.pub", lector.readLine());

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            this.g = lector.readLine();// llegada de G
            System.out.println("Esta es tu G:" + this.g);
            this.p = lector.readLine();// llegada de P
            System.out.println("Esta es tu P: " + this.p);
            this.gx = lector.readLine();// llegada de G^x
            System.out.println("Esta es tu gx: " + this.gx);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String expected = this.g + "," + this.p + "," + this.gx;// el mensaje correcto

        try {
            this.signature = lector.readLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // Firma digital

        System.out.println("Esta es la firma electronica: " + signature);

        boolean ck = false;

        try {
            ck = sf.checkSignature(publicaServer, str2byte(this.signature), expected);

            if (ck) {
                escritor.println("OK");// envío por le canal
                escritor.println();
            } else {
                escritor.println("ERROR");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        
    }

    public byte[] str2byte(String ss) {
        // Encapsulamiento con hexadecimales
        byte[] ret = new byte[ss.length() / 2];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (byte) Integer.parseInt(ss.substring(i * 2, (i + 1) * 2), 16);
        }
        return ret;
    }

    public String byte2str(byte[] b) {
        // Encapsulamiento con hexadecimales
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String g = Integer.toHexString(((char) b[i]) & 0x00ff);
            ret += (g.length() == 1 ? "0" : "") + g;
        }
        return ret;
    }

}
