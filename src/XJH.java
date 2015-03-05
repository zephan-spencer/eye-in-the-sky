
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import org.json.JSONObject;
import com.csvreader.CsvReader;
import static java.lang.Thread.sleep;

public class XJH {

////////////////////////////////////////////////////////////////////////////////
    static Scanner scanner;
////////////////////////////////////////////////////////////////////////////////
    static URL url;
////////////////////////////////////////////////////////////////////////////////
    static CsvReader csvReader;
////////////////////////////////////////////////////////////////////////////////
    static BufferedWriter writer;
////////////////////////////////////////////////////////////////////////////////    
    static FileWriter writeRef;
////////////////////////////////////////////////////////////////////////////////
    static Scanner mainScanner;
////////////////////////////////////////////////////////////////////////////////
    static String[] tokens;
////////////////////////////////////////////////////////////////////////////////
    static int counter;
////////////////////////////////////////////////////////////////////////////////
    static String delims;
    static String input;
    static String location;
    static String csvFile = "/Users/Emil/Downloads/contact.csv";
    static String line = "";
    static String cvsSplitBy = ",";
    static String name;

    static char quotes = '"';

    public static void main(String[] args) throws Exception {
        init();

//        System.out.println("Run the program? yes or no?");
//        if (eInput.hasNext("Yes") || eInput.hasNext("yes")) {
//        } else {
//            return;
//        }
        writer.write(HTML.getHeader());
        writer.write(HTML.arrayOpen());
        run();
        writer.write(HTML.arrayClose());
        writer.write(HTML.getFooter());
        writer.close();
        AlreadyWrittenChecker.closeWrittenChecker();
    }

    public static void init() throws IOException {
////////intializes all variables////////////////////////////////////////////////
        name = null;
        scanner = null;
        url = null;
        tokens = null;
        delims = null;
        location = null;
////////////////////////////////////////////////////////////////////////////////        
        counter = 0;
////////////////////////////////////////////////////////////////////////////////        
        writeRef = new FileWriter("dataTest.html");
        writer = new BufferedWriter(writeRef);
        mainScanner = new Scanner(System.in);
        AlreadyWrittenChecker.alreadyWrittenCheckerInit();
    }

    public static void run() throws Exception {
        csvReader = null;
        try {
            csvReader = new CsvReader(csvFile);
            csvReader.readHeaders();
//            for (int i = 0; i < tokens.length; i++) {
//                csvReader.getDelimiter();
//            }
            while (csvReader.readRecord()) {
                delims = "[ ]+";
                location = "";
                name = "";
                input = csvReader.get("Mailing Street");
                if (counter > 0 && !input.equals("")) {
                    if (csvReader.get("Last Name").contains("'")) {
                        name = csvReader.get("First Name");
                    } else if (csvReader.get("First Name").contains("'")) {
                        name = csvReader.get("Last Name");
                    } else {
                        name = csvReader.get("First Name") + ", " + csvReader.get("Last Name");
                    }
                    tokens = input.split(delims);
                    for (int i = 0; i < tokens.length; i++) {
                        if (i + 1 >= tokens.length) {
                            location = location + (tokens[i]);
                        } else {
                            location = location + (tokens[i] + "+");
                        }
                    }
                    if (!AlreadyWrittenChecker.newFile) {
                        if (AlreadyWrittenChecker.getAddress().equals(location)) {
                            System.out.println("Already have this address in: " + location);
                            //geocode(location, name, counter);
                        } else {
                            System.out.println("No match :( " + location);
                            AlreadyWrittenChecker.addToList(location);
                        }
                    } else {
                        AlreadyWrittenChecker.addToList(location);
                    }
                }
                counter++;
            }

        } catch (FileNotFoundException ex) {
        } catch (UnsupportedEncodingException ex) {
        } catch (IOException ex) {
        } catch (ArrayIndexOutOfBoundsException eg) {
        } finally {
            System.out.println("Made it to close!");
            csvReader.close();
        }
        System.out.println("Done");

    }

    public static void geocode(String address, String name, int arrayNum) throws Exception {
////////clears the variables////////////////////////////////////////////////////
        scanner = null;
        url = null;
////////builds a URL////////////////////////////////////////////////////////////
        String googleURL = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address;
        URLEncoder.encode(googleURL, "UTF-8");
        url = new URL(googleURL);
        scanner = new Scanner(url.openStream());
////////read from the URL///////////////////////////////////////////////////////
        String str = new String();
        while (scanner.hasNext()) {
            str += scanner.nextLine();
        }
        scanner.close();
////////build a JSON object/////////////////////////////////////////////////////
        JSONObject obj = new JSONObject(str);
        if (!obj.getString("status").equals("OK")) {
            return;
        }
////////get the first result////////////////////////////////////////////////////
        JSONObject res = obj.getJSONArray("results").getJSONObject(0);
        System.out.println(res.getString("formatted_address"));
        JSONObject loc = res.getJSONObject("geometry").getJSONObject("location");
        System.out.println("lat: " + loc.getDouble("lat") + ", lng: " + loc.getDouble("lng"));
        writer.write("[" + "'" + name + "', " + loc.getDouble("lat") + ", "
                + loc.getDouble("lng") + ", " + arrayNum + "],");
//        Thread.sleep(100);
    }
}
