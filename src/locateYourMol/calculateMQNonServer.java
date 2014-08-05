/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package locateYourMol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class will take the string(SMI) as input and then Connect to MQN browser
 * server(130.92.134.166) and calculate MQN and return the MQN as result.
 *
 * Steps involved are:
 *
 * 1: Open the connection to server
 *
 * 2: Send the "SMI" as input to the JSP page on server:
 *
 * root@130.92.134.166:/home/bowser/tomcat6/webapps/MAPPLET/calculateMQN.jsp
 *
 * 3: "calculateMQN.jsp" runs the MQN calculation on server.
 *
 * 4: Get the result back from server.
 *
 * The class specially built to avoid the calculation of MQN on client side. As
 * the MAPPLET runs in client browser and as such it requires the JCHEM license
 * on client machine for calculation of MQN. Client may or may not have the
 * license on their machine.
 *
 * So to resolve the license problem its better to send request from MAPPLET
 * from client machine to MQN browser server and then do the calculation on
 * server.
 *
 *
 * @author mahendra
 */
public class calculateMQNonServer {

    public String calculateMQN(String smi) throws MalformedURLException, IOException {

        /*
         * replace the special charachters 1st: in URL they have to be encoded I
         * had problem with molecules having + charge
         */
        smi = smi.replace("+", "%2B");
        smi = smi.replace("=", "%3D");
        smi = smi.replace("#", "%23");

        /*
         * Open the connection to server with smile as argument
         */
        URL url = new URL("http://130.92.134.166:8080/MAPPLET/calculateMQN.jsp?InMol=" + smi);
        URLConnection servletConnection = url.openConnection();
        servletConnection.setDoInput(true); // true, if we get data back;

        /*
         * retrive the result from the server
         */
        InputStream in = servletConnection.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String inputLine = "";

        while ((inputLine = br.readLine()) != null) {

            if (inputLine.contains("MQN")) {
                break;
            }
        }
        br.close();

        /*
         * Now look wether MQN is Generated OR Not
         */
        if (inputLine.contains("ERROR")) {
            return "ERROR";
        } else {

            /*
             * Input line is in pattern: MQN:mqn1;mqn2.....mqn42 So removed the
             * MQN: from inputLine and return the
            result
             */
            return inputLine.split("MQN:")[1];
        }
    }
}