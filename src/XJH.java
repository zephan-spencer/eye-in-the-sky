
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import org.json.JSONObject;

public class XJH {

    static int counter;
    static Scanner scanner;
    static URL url;
    static BufferedReader csvReader;
    static BufferedWriter writer;
    static FileWriter writeRef;
    static String[] tokens;
    static String delims;
    static String location;
    static String csvFile = "/Users/Emil/Downloads/contact.csv";
    static String line = "";
    static String cvsSplitBy = ",";

    XJH() {
//      intializes all variables

        scanner = null;
        url = null;
        tokens = null;
        delims = null;
        location = null;
        counter = 0;
    }

    public static void main(String[] args) throws Exception {
        writeRef = new FileWriter("dataTest.html");
        writer = new BufferedWriter(writeRef);

        writer.write(HTML.getHeader());
        writer.write(HTML.arrayOpen());
        run();
        writer.write(HTML.arrayClose());
        writer.write(HTML.getFooter());
//      geocode();
        writer.close();
    }

    public static void run() throws Exception {
        csvReader = null;
        try {
            csvReader = new BufferedReader(new FileReader(csvFile));
            while ((line = csvReader.readLine()) != null) {
                delims = "[ ]+";
                // use comma as separator
                String[] data = line.split(cvsSplitBy);
                // Skips the first line
                tokens = data[24].split(delims);
                location = "";
                if (counter > 0 && counter <= 30) {
//                    writer.println();
//                    writer.println("Last Name: " + data[5]);
//                    writer.println("First name: " + data[4]);
//                    writer.println("Phone: " + data[13]);
//                    writer.println("Address: " + data[24]);
                    for (int i = 0; i < tokens.length; i++) {
                        if (i + 1 >= tokens.length) {
                            location = location + (tokens[i]);
                        } else {
                            location = location + (tokens[i] + "+");
                        }
                    }
                    System.out.println(location);
                    geocode(location, counter);
                }
                counter++;
            }

        } catch (FileNotFoundException ex) {
        } catch (UnsupportedEncodingException ex) {
        } catch (IOException ex) {
        } catch (ArrayIndexOutOfBoundsException eg) {
        System.out.println("Taadaaah");
        } finally {
            if (csvReader != null) {
                try {
                    csvReader.close();
                } catch (IOException ex) {
                }
            }
        }
        System.out.println("Done");

    }

    public static void geocode(String address, int arrayNum) throws Exception {
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
        writer.write("[" + "'[name here]', " + loc.getDouble("lat") + ", "
                + loc.getDouble("lng") + ", " + arrayNum + "],");
//        Thread.sleep(100);
    }
////////////////////////////////////////////////////////////////////////////////
}
