/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MAPPLET;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class will connect to server where our BIN file for each database is
 * stored. It then get the required file and store/save on the user computer
 *
 * @author mahendra
 */
public class getBinFileFromServer {

    String saveBinFile(String fileName, String outFileName) throws MalformedURLException, IOException {

        if (outFileName.contains("null")) {
            return null;
        } else {
            logWindow.updateLog("Please Wait! saving bin file..");
            URL url = new URL("http://130.92.134.166:8080/MAPPLET/runMAPPLET.v8/" + fileName);
            URLConnection servletConnection = url.openConnection();
            servletConnection.setDoInput(true); // true, if we get data back;           
            InputStream inputStream = servletConnection.getInputStream();
            BufferedInputStream in = new BufferedInputStream(servletConnection.getInputStream());
            FileOutputStream out = new FileOutputStream(outFileName);

            int i = 0;
            byte[] bytesIn = new byte[3000000];
            while ((i = in.read(bytesIn)) >= 0) {
                out.write(bytesIn, 0, i);
            }
            out.close();
            in.close();

            logWindow.updateLog("..Completed\n");
            return null;
        }
    }
    /**
     * ***************************************************************************************
     */
}
