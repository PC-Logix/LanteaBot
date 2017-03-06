package pcl.lc.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Implementation of the Hastebin API
 *
 * @author OliverVsCreeper
 */
public class PasteUtils {

	public enum Formats {
	    LENGTH,
	    INVENTORY,
	    NONE;
	}
	
    private static String pasteURL = "http://paste.pc-logix.com/";

	  public synchronized static String paste(String urlParameters) {
	      return paste(urlParameters, Formats.NONE);
    }

    /**
     * A simple implementation of the Hastebin Client API, allowing data to be pasted online for
     * players to access.
     *
     * @param urlParameters The string to be sent in the body of the POST request
     * @return A formatted URL which links to the pasted file
     */
    public synchronized static String paste(String urlParameters, Enum format) {

        StringBuilder sb = new StringBuilder(urlParameters);

        if (format.equals(Formats.LENGTH)) {
            int i = 0;
            while ((i = sb.indexOf(" ", i + 100)) != -1) {
                sb.replace(i, i + 1, "\n");
            }
        }

        HttpURLConnection connection = null;
        try {
            //Create connection
            URL url = new URL(pasteURL + "documents");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            //DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

            connection.setRequestProperty("content-type", "application/json;  charset=utf-8");
            Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            writer.write(sb.toString());
            writer.flush();
            writer.close();

            //Get Response
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            return pasteURL.replace("http", "https") + new JSONObject(rd.readLine()).getString("key");

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection == null) return null;
            connection.disconnect();
        }
    }

    /**
     * Returns the URL of the server being used.
     *
     * @return API to use for posting data
     */
    public static String getPasteURL() {
        return pasteURL;
    }

    /**
     * Sets the URL used by the paste method, allowing for the server logs are pasted to to be
     * dynamically changed.
     *
     * @param URL API URL of HasteBin instance
     */
    public static void setPasteURL(String URL) {
        pasteURL = URL;
    }

    /**
     * Grabs a HasteBin file from the internet and attempts to return the file with formatting
     * intact.
     *
     * @return String HasteBin Raw Text
     */
    public static synchronized String getPaste(String ID) {
        String URLString = pasteURL + "raw/" + ID + "/";
        try {
            URL URL = new URL(URLString);
            HttpURLConnection connection = (HttpURLConnection) URL.openConnection();
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String paste = "";
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.contains("package")) continue;
                if (paste.equals("")) paste = line;
                else paste = paste + "\n" + line;
            }
            return paste;
        } catch (IOException e) {
            return "";
        }
    }


}