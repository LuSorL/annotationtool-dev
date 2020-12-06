

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class Dezip {
    /**
     * GunZip it
     */
    public void gunzipIt(final String source,final String destination){
        byte[] buffer = new byte[1024];
        try{
            GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(source));
            FileOutputStream out = new FileOutputStream(destination);
            int len;

            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            gzis.close();
            out.close();

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
}
