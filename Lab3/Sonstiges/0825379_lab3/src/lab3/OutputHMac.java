package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

/**
 *
 * @author ternekma
 */
public class OutputHMac {
    public String output="";
    public byte[] hash = null;

    private String directory;
    private String username;
    private Mac hMac = null;

    OutputHMac(String directory, String username) {
      this.directory=directory;
      this.username=username;

      String filename=username.toUpperCase()+".key";
      if ( UtilityClass.FileExists(directory,filename)) {

         byte[] keyBytes = new byte[1024];
         FileInputStream fis;
            try {
                fis = new FileInputStream(directory + "/" + filename);
                fis.read(keyBytes);
                fis.close();
                byte[] input = Hex.decode(keyBytes);
                Key secretkey = new SecretKeySpec(input, "HmacSHA256");

                try {
                    hMac = Mac.getInstance("HmacSHA256");
                    hMac.init(secretkey);
                } catch (InvalidKeyException ex) {
                    Logger.getLogger(OutputHMac.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(OutputHMac.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                Logger.getLogger(OutputHMac.class.getName()).log(Level.SEVERE, null, ex);
            } 
        
      }
    }

    public void updateHMac() {
       if (output.equals("")) {
            output="kein erzeugter Output vorhanden!";
       }
       hMac.update(output.getBytes());
       hash = hMac.doFinal();
    }
    
    public String getSerializedOutputHMac() {

        try {
            return "###"+output + "### " + new String( Base64.encode(hash), "UTF8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(OutputHMac.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }


}
