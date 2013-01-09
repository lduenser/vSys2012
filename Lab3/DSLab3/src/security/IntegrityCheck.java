package security;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import methods.Methods;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import debug.Debug;

public class IntegrityCheck {
	
	    public String output="";
	    public byte[] hash = null;
	    private String directory = "";
	    private String username = "";
	    private Mac hMac = null;

	    public IntegrityCheck(String directory, String username) {
	    	this.directory=directory;
	    	this.username=username;

	    	String filename=username+".key";
	    	if(Methods.exists(directory,filename)) {
	
	    		byte[] keyBytes = new byte[1024];
	    		FileInputStream fis;
	    		try {
	    			fis = new FileInputStream(directory + filename);
	    			Debug.printDebug("File: "+directory + filename);
	    			fis.read(keyBytes);
	    			fis.close();
	    			byte[] input = Hex.decode(keyBytes);
	    			Key secretkey = new SecretKeySpec(input, "HmacSHA256");
	
	    			try {
	    				hMac = Mac.getInstance("HmacSHA256");
	    				hMac.init(secretkey);
	    			} catch (InvalidKeyException inv) {
	    				inv.printStackTrace();
	    			} catch (NoSuchAlgorithmException no) {
	    				no.printStackTrace();
	    			}
	    		} catch (IOException io) {
		                io.printStackTrace();
	    		} 	        
	    	}
	    }

	    public void updateHMac() {
	       if (output.equals("")) {
	            output="kein erzeugter Output vorhanden!";
	       }
	       hMac.update(output.getBytes());
	       hash = hMac.doFinal();
	       
	       try {
	    	   Debug.printDebug("string hash update: "+new String(Base64.encode(hash), "UTF8"));
	       } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
	    	   e.printStackTrace();
	       }
	    }
	    
	    public String getAttachedHMac() {

	        try {                
	            return output+" "+new String(Base64.encode(hash), "UTF8");            
	            
	        } catch (UnsupportedEncodingException uns) {
	            uns.printStackTrace();
	        }
	        return "";
	    }
	    
}
