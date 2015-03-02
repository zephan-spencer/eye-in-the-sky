
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import org.json.JSONObject;

public class XJH {

    static int counter;
    static Scanner scanner;
    static URL url;
    static PrintWriter writer;
    static String[] tokens;
    static String delims;
    static String location;

    XJH() {
//      clears all variables
        scanner = null;
        url = null;
        tokens = null;
        delims = null;
        location = null;
        counter = 0;
    }

    public static void main(String[] args) throws Exception {
        writer = new PrintWriter("dataTEST.txt", "UTF-8");
        run();
//      geocode();
    }

    public static void run() throws Exception {

        String csvFile = "/Users/Zephan/Downloads/contact.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                delims = "[ ]+";
                // use comma as separator
                String[] data = line.split(cvsSplitBy);
                // Skips the first line
                tokens = data[24].split(delims);
                location = "";
                if (counter > 0) {
                    writer.println();
                    writer.println("Last Name: " + data[5]);
                    writer.println("First name: " + data[4]);
                    writer.println("Phone: " + data[13]);
                    writer.println("Address: " + data[24]);
                    for (int i = 0; i < tokens.length; i++) {
                        if (i + 1 >= tokens.length) {
                            location = location + (tokens[i]);
                        } else {
                            location = location + (tokens[i] + "+");
                        }
                    }
                    System.out.println(location);
                    geocode(location);
                    writer.println();
                }
                counter++;
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            writer.close();
        }
        System.out.println("Done");

    }

    public static void geocode(String address) throws Exception {
////////clears the variables////////////////////////////////////////////////////
        scanner = null;
        url = null;
////////////////////////////////////////////////////////////////////////////////
////////builds a URL////////////////////////////////////////////////////////////
        String googleURL = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address;
        URLEncoder.encode(googleURL, "UTF-8");
        url = new URL(googleURL);
        scanner = new Scanner(url.openStream());
////////////////////////////////////////////////////////////////////////////////
////////read from the URL///////////////////////////////////////////////////////
        String str = new String();
        while (scanner.hasNext()) {
            str += scanner.nextLine();
        }
        scanner.close();
////////////////////////////////////////////////////////////////////////////////
////////build a JSON object/////////////////////////////////////////////////////
        JSONObject obj = new JSONObject(str);
        if (!obj.getString("status").equals("OK")) {
            return;
        }
////////////////////////////////////////////////////////////////////////////////
////////get the first result////////////////////////////////////////////////////
        JSONObject res = obj.getJSONArray("results").getJSONObject(0);
        System.out.println(res.getString("formatted_address"));
        JSONObject loc = res.getJSONObject("geometry").getJSONObject("location");
        System.out.println("lat: " + loc.getDouble("lat") + ", lng: " + loc.getDouble("lng"));
        Thread.sleep(1000);
    }
////////////////////////////////////////////////////////////////////////////////
}
