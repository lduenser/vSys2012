package lab2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author ternekma
 */
public class UtilityClass {
    public static enum TaskStatus {
        statePrepared, stateAssigned, stateExecuting, stateFinished
    }


    public static Properties GetRegistryProps() {
      //read properties file
       InputStream in = ClassLoader.getSystemResourceAsStream("registry.properties");
       if (in != null) {
            try {
                Properties registry = new java.util.Properties();
                registry.load(in);
                return registry;
            } catch (IOException ex) {
                Logger.getLogger(UtilityClass.class.getName()).log(Level.SEVERE, null, ex);
            }
       }

       return null;
    }


    public static boolean FileExists (String taskDir, String AFilename) {
        try {
            File myFile = new File(taskDir+"/"+AFilename);
            return myFile.exists();
        } catch (Exception ex) {
            return false;
        }

    }

    // Returns the contents of the file in a byte array.
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
}
