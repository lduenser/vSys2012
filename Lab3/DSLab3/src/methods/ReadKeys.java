package methods;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

public class ReadKeys {

	 public static PublicKey publickey = null;
	 public static PrivateKey privatekey = null;
	 private static String pathToPublicKey = "keys/alice.pub.pem";
	 private static String pathToPrivateKey ="keys/alice.pem";
	 
	 
	//Read private und public key for secure channel
    @SuppressWarnings("finally")
	public static boolean getKeys() {
        boolean result=false;
        PEMReader inPrivat=null,inPublic = null;
        try {
            //public key 
            try {
              inPublic = new PEMReader(new FileReader(pathToPublicKey));
            } catch (Exception e) {
                 System.out.println("Can't read file for public key!");
                 return false;
            }
            publickey= (PublicKey) inPublic.readObject();

            //private key       
            FileReader privateKeyFile=null;
            try {
               privateKeyFile=new FileReader(pathToPrivateKey);
            } catch (Exception e) {
                 System.out.println("Can't read file for private key!");
                 return false;
            }
            
            inPrivat = new PEMReader(privateKeyFile, new PasswordFinder() {
                @Override
                 public char[] getPassword() {
                    // reads the password from standard input for decrypting the private key
                    System.out.println("Enter pass phrase:");
                    try {
                        return (new BufferedReader(new InputStreamReader(System.in))).readLine().toCharArray();
                    } catch (IOException ex) {
                        return "".toCharArray();
                    }
                 }
            });

           KeyPair keyPair = (KeyPair) inPrivat.readObject();
           privatekey = keyPair.getPrivate();
           result=true;
           System.out.println("Keys successfully initialized!");
        } catch (IOException ex) {
            System.out.println("Wrong password!");
            result=getKeys();
        } finally {
            try {
                if (inPublic!=null) {
                  inPublic.close();
                }
                if (inPrivat!=null) {
                  inPrivat.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return result;
        }
    }
	
	
}
