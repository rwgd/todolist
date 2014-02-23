package de.thm.todoist.Helper;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class FktLib implements Constants {

    private static FileInputStream fis;
    private static FileOutputStream fos;


    public static String readFile(File file) throws IOException {
        StringBuilder fileContents = new StringBuilder((int) file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");
        try {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + lineSeparator);
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }


    public static boolean ping(String httpUrl) {
        boolean res = false;
        if (null != httpUrl) {
            if (null != httpUrl) {
                // If protocol is not attached with the passed URL
                if (!(httpUrl.startsWith("https") || httpUrl.startsWith("http"))) {
                    httpUrl = "http://" + httpUrl;
                }
            }
            HttpURLConnection con = null;
            try {
                // Open a http connection via the url
                con = (HttpURLConnection) new URL(httpUrl).openConnection();
                con.setRequestMethod("GET");
                // Putting connection timeout as 5 sec
                con.setConnectTimeout(5000);
                // Adding default user agent property as few websites won't
                // allow unknown user agent's
                con.addRequestProperty("User-Agent",
                        "Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)");
                // Response Status code
                int code = con.getResponseCode();
                // Check if it belongs to 2xx / 3xx
                char[] codeArray = Integer.valueOf(code).toString().toCharArray();
                if (codeArray[0] == '2' || codeArray[0] == '3') {
                    res = true;
                }
            } catch (Exception e) {
                System.out.println("Error in checkURLExistence: "
                        + e.toString());
            } finally {
                con.disconnect();
                con = null;
            }
        }
        return res;
    }

}
