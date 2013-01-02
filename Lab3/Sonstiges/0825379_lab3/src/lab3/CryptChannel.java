package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

/**
 *
 * @author ternekma
 */
public class CryptChannel extends ChannelDecorator{
    private Key key = null;
    private String algorithm = "";
    byte[] iv=null;
    

    public void setKey(Key key) {
        this.key=key;
    }

    public void setalgorithm(String algorithm) {
        this.algorithm=algorithm;
    }

    public void setInitVector(byte[] iv) {
        this.iv=iv;
    }


    public CryptChannel(Channel decoratedChannel) {
        super(decoratedChannel);
    }

    @Override
    public byte[] receive() {
        try {
            byte[] receivedStr = super.receive();
            if (receivedStr!=null) {
                Cipher crypt = Cipher.getInstance(algorithm);
                if (iv!=null) {
                  AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
                  crypt.init(Cipher.DECRYPT_MODE, key, paramSpec);
                } else {
                  crypt.init(Cipher.DECRYPT_MODE, key);
                }

                byte[] decryptedText = crypt.doFinal(receivedStr);
                return decryptedText;
            } else {
                return null;
            }
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(CryptChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(CryptChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(CryptChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(CryptChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CryptChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(CryptChannel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void send (byte[] sendBytes) {
        try {
            Cipher crypt = Cipher.getInstance(algorithm);
             if (iv!=null) {
              AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
              crypt.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            } else {
              crypt.init(Cipher.ENCRYPT_MODE, key);
            }
            byte[] encryptedText = crypt.doFinal(sendBytes);
            super.send(encryptedText);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(CryptChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(CryptChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(CryptChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(CryptChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CryptChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(CryptChannel.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }


}
