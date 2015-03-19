
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import org.json.JSONObject;
import com.csvreader.CsvReader;
import static java.lang.Thread.sleep;

public class XJH {

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
    static String basicInquiryOutput;
    static String goodProgressInquiryOutput;
    static String workerCompOutput;
    static String potentialClientOutput;
    static String completedCustomerOutput;
    static String doctorOutput;
    static String therapistOutput;
    static String prosthetistOutput;
    static String nurseCaseManagerOutput;
    static String type;

////////////////////////////////////////////////////////////////////////////////
    static char quotes = '"';

    public static void main(String[] args) throws Exception {
        init();

        System.out.println("Would you like to create a new file? Yes/No");
        if (mainScanner.hasNext("yes") || mainScanner.hasNext("Yes")) {
            GlobalVariables.newFile = true;
            initXJHWriter();
            AlreadyWrittenChecker.alreadyWrittenCheckerInit();
            run();
            writeData();
            writer.close();
        } else {
//            GlobalVariables.newFile = false;
//            AlreadyWrittenChecker.alreadyWrittenCheckerInit();
//            HTML_Parser.parseHTML();
//            run();
//            initXJHWriter();
//            writer.write(HTML.getHeader());
//            writer.write(HTML.previousArrayOpen());
//            writer.write(HTML_Parser.getPreviousCords() + output);
//            writer.write(HTML.arrayClose());
//            writer.write(HTML.getFooter());
//            writer.close();
        }
        AlreadyWrittenChecker.closeWrittenChecker();
    }

    public static void init() throws IOException {
////////intializes all variables////////////////////////////////////////////////
        scanner = null;
        url = null;
        tokens = null;
////////////////////////////////////////////////////////////////////////////////
        delims = "";
        location = "";
        name = "";
        basicInquiryOutput = "";
        goodProgressInquiryOutput = "";
        workerCompOutput = "";
        potentialClientOutput = "";
        completedCustomerOutput = "";
        doctorOutput = "";
        therapistOutput = "";
        prosthetistOutput = "";
        nurseCaseManagerOutput = "";
////////////////////////////////////////////////////////////////////////////////        
        counter = 0;
////////////////////////////////////////////////////////////////////////////////        
        mainScanner = new Scanner(System.in);
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
                input = "";
                type = "";
                type = csvReader.get("ID/Status");
                input = csvReader.get("Mailing Street");
                if (counter > 0 && !type.equals("") && !input.equals("") && counter <= 200) {
                    name = csvReader.get("Last Name") + ", " + csvReader.get("First Name");

                    tokens = input.split(delims);
                    for (int i = 0; i < tokens.length; i++) {
                        if (i + 1 >= tokens.length) {
                            location = location + (tokens[i]);
                        } else {
                            location = location + (tokens[i] + "+");
                        }
                    }
                    if (!GlobalVariables.newFile) {
                        if (AlreadyWrittenChecker.getAddress(location)) {
                            System.out.println("Already have this address in: " + location);
                        } else {
//                            System.out.println("No match :( " + location);
                            AlreadyWrittenChecker.addToList(location);
                            geocode(location, name, input, type);
                        }
                    } else {
                        AlreadyWrittenChecker.addToList(location);
                        geocode(location, name, input, type);
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

    public static void geocode(String address, String name, String prettyAddress, String mode) throws Exception {
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

        if (mode.contains("Client") || mode.contains("Amputee")) {
            potentialClientOutput = potentialClientOutput + "[" + "\"" + name + ""
                    + "\", " + loc.getDouble("lat") + ", "
                    + loc.getDouble("lng") + ", " + "\"" + prettyAddress + "\"" + "],";
        } else if (mode.contains("Prosthetist") || mode.contains("Orthotist")
                || mode.contains("prosthetist") || mode.contains("orthotist")) {
            prosthetistOutput = prosthetistOutput + "[" + "\"" + name + ""
                    + "\", " + loc.getDouble("lat") + ", "
                    + loc.getDouble("lng") + ", " + "\"" + prettyAddress + "\"" + "],";
        } else if (mode.contains("Doctor") || mode.contains("doctor")) {
            doctorOutput = doctorOutput + "[" + "\"" + name + ""
                    + "\", " + loc.getDouble("lat") + ", "
                    + loc.getDouble("lng") + ", " + "\"" + prettyAddress + "\"" + "],";
        } else if (mode.contains("Therapist") || mode.contains("therapist")) {
            therapistOutput = therapistOutput + "[" + "\"" + name + ""
                    + "\", " + loc.getDouble("lat") + ", "
                    + loc.getDouble("lng") + ", " + "\"" + prettyAddress + "\"" + "],";
        } else if (mode.contains("Nurse") || mode.contains("nurse")) {
            nurseCaseManagerOutput = nurseCaseManagerOutput + "[" + "\"" + name + ""
                    + "\", " + loc.getDouble("lat") + ", "
                    + loc.getDouble("lng") + ", " + "\"" + prettyAddress + "\"" + "],";
        } else {
        }
//        Thread.sleep(100);
    }

    public static void initXJHWriter() throws IOException {
        writeRef = new FileWriter("dataTest.html", false);
        writer = new BufferedWriter(writeRef);
    }

    public static void writeData() throws IOException {
        writer.write(HTML.getHeader());
        writer.write(HTML.basicInquiryMarkersOpen());
        writer.write(basicInquiryOutput);
        writer.write(HTML.basicInquiryMarkersClose());
        writer.write(HTML.goodProgressInquiryMarkersOpen());
        writer.write(goodProgressInquiryOutput);
        writer.write(HTML.goodProgressInquiryMarkersClose());
        writer.write(HTML.workerCompMarkersOpen());
        writer.write(workerCompOutput);
        writer.write(HTML.workerCompMarkersClose());
        writer.write(HTML.potentialMarkersOpen());
        writer.write(potentialClientOutput);
        writer.write(HTML.potentialMarkersClose());
        writer.write(HTML.completedCustomerMarkersOpen());
        writer.write(completedCustomerOutput);
        writer.write(HTML.completedCustomerMarkersClose());
        writer.write(HTML.doctorOpen());
        writer.write(doctorOutput);
        writer.write(HTML.doctorClose());
        writer.write(HTML.therapistOpen());
        writer.write(therapistOutput);
        writer.write(HTML.therapistClose());
        writer.write(HTML.prosthetistOpen());
        writer.write(prosthetistOutput);
        writer.write(HTML.prosthetistClose());
        writer.write(HTML.nurseCaseManagerOpen());
        writer.write(nurseCaseManagerOutput);
        writer.write(HTML.nurseCaseManagerClose());
        writer.write(HTML.getFooter());
    }
}
