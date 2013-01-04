package security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import debug.Debug;

public class CipherChannel extends Decorator{

		private Key key = null;
		private String algorithm = "";
		private String regExPattern = null;
		byte[] iv=null;
		
		private boolean sendEncrypted = true;
		
	 	public CipherChannel(Channel decoratedChannel) {
	 		super(decoratedChannel);
	 		
	 		//Pattern to match "Standard Commands": !command
	 		regExPattern = "^\\!(?=[a-zA-Z]+).*";
	 	}
		
	    public void setKey(Key key) {
	        this.key=key;
	    }

	    public void setalgorithm(String algorithm) {
	        this.algorithm=algorithm;
	    }

	    public void setInitVector(byte[] iv) {
	        this.iv=iv;
	    }
	    
	    public void setPattern(String pattern) {
	    	this.regExPattern = pattern;
	    }
	    
	    public void setSendEncrypted() {
	    	this.sendEncrypted = true;
	    }
	    
	    public void unsetSendEncrypted() {
	    	this.sendEncrypted = false;
	    }

	    @Override
	    public byte[] receive() {
	    	
	    	try {
	            byte[] receivedStr = super.receive();
	            
	            if (receivedStr!=null) {
	            	
	            	String temp = new String(receivedStr, "UTF8");
		            Debug.printDebug("cipherChannel Receive: "+temp);
	            	if(temp.startsWith("!")) {
	            		return receivedStr;
	            	}
	            	
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
	            
	        } catch (InvalidAlgorithmParameterException invAlgo) {
	        	invAlgo.printStackTrace();
	        } catch (IllegalBlockSizeException ill) {
	            ill.printStackTrace();
	        } catch (BadPaddingException bad) {
	        	bad.printStackTrace();
	        } catch (InvalidKeyException indKey) {
	        	indKey.printStackTrace();
	        } catch (NoSuchAlgorithmException noAlgo) {
	        	noAlgo.printStackTrace();
	        } catch (NoSuchPaddingException noPadd) {
	        	noPadd.printStackTrace();
	        } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
	        return null;
	    	
	    }
	    
	    @Override
	    public void send (byte[] sendBytes) {
	    	
	    	try {
	    		if(!this.sendEncrypted) {
		    		super.send(sendBytes);
		    	}
		    	else {
		    		Cipher crypt = Cipher.getInstance(algorithm);
		              if (iv!=null) {
		               AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
		               crypt.init(Cipher.ENCRYPT_MODE, key, paramSpec);
		             } else {
		               crypt.init(Cipher.ENCRYPT_MODE, key);
		             }
		             byte[] encryptedText = crypt.doFinal(sendBytes);
		             super.send(encryptedText);
		    	}
	         } catch (InvalidAlgorithmParameterException invAlgo) {
		        	invAlgo.printStackTrace();
		        } catch (IllegalBlockSizeException ill) {
		            ill.printStackTrace();
		        } catch (BadPaddingException bad) {
		        	bad.printStackTrace();
		        } catch (InvalidKeyException indKey) {
		        	indKey.printStackTrace();
		        } catch (NoSuchAlgorithmException noAlgo) {
		        	noAlgo.printStackTrace();
		        } catch (NoSuchPaddingException noPadd) {
		        	noPadd.printStackTrace();
		        }    	
	    }
}
