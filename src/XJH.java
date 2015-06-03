
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import org.json.JSONObject;
import com.csvreader.CsvReader;

public class XJH {

    static Scanner scanner;
////////////////////////////////////////////////////////////////////////////////
    static URL url;
////////////////////////////////////////////////////////////////////////////////
    static CsvReader contact_CSV_Reader;
    static CsvReader lead_CSV_Reader;
////////////////////////////////////////////////////////////////////////////////
    static BufferedWriter writer;
////////////////////////////////////////////////////////////////////////////////    
    static FileWriter writeRef;
////////////////////////////////////////////////////////////////////////////////
    static Scanner mainScanner;
////////////////////////////////////////////////////////////////////////////////
    static String[] tokens;
////////////////////////////////////////////////////////////////////////////////
    static int contactCounter;
    static int leadCounter;
////////////////////////////////////////////////////////////////////////////////
    static String delims;
    static String input;
    static String location;
    static String contactCSV = "/Users/Emil/Downloads/contact.csv";
    static String leadCSV = "/Users/Emil/Downloads/lead.csv";
    static String line = "";
    static String cvsSplitBy = ",";
    static String name;
    static String basicInquiryOutput;
    static String goodProgressInquiryOutput;
    static String workerCompOutput;
    static String completedWorkerCompOutput;
    static String potentialClientOutput;
    static String completedCustomerOutput;
    static String doctorOutput;
    static String therapistOutput;
    static String prosthetistOutput;
    static String nurseCaseManagerOutput;
    static String type;
    static String city;
    static String phoneNumber;
    static String personCache;
    static String newAddress;
    static String oldAddress;
    static String tag;
////////////////////////////////////////////////////////////////////////////////
    static char quotes = '"';
////////////////////////////////////////////////////////////////////////////////
    final static boolean NEW = true;
    final static boolean OLD = false;

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
            GlobalVariables.newFile = false;
//            AlreadyWrittenChecker.alreadyWrittenCheckerInit();
            HTML_Parser.parseHTML();
//            initXJHWriter();
//            run();
//            writeData();
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
        city = "";
        basicInquiryOutput = "";
        goodProgressInquiryOutput = "";
        workerCompOutput = "";
        completedWorkerCompOutput = "";
        potentialClientOutput = "";
        completedCustomerOutput = "";
        doctorOutput = "";
        therapistOutput = "";
        prosthetistOutput = "";
        nurseCaseManagerOutput = "";
        phoneNumber = "";
        personCache = "";
        newAddress = "";
        oldAddress = "";
        tag = "";
////////////////////////////////////////////////////////////////////////////////        
        contactCounter = 0;
        leadCounter = 0;
////////////////////////////////////////////////////////////////////////////////        
        mainScanner = new Scanner(System.in);
    }

    public static void run() throws Exception {
        contact_CSV_Reader = null;
        lead_CSV_Reader = null;
        try {
//          parseLead();
            parseContact();
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        } catch (ArrayIndexOutOfBoundsException eg) {
            System.out.println(eg);
        } finally {
            System.out.println("Made it to close!");
            contact_CSV_Reader.close();
//            lead_CSV_Reader.close();
        }
        System.out.println("Done");
    }

    public static void geocode(String address, String name, String prettyAddress, String type,
            String phone, Boolean mode, boolean age, boolean portion) throws Exception {
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
            System.out.println(str);
            return;
        }
////////get the first result////////////////////////////////////////////////////
        JSONObject res = obj.getJSONArray("results").getJSONObject(0);
        System.out.println(res.getString("formatted_address"));
        JSONObject loc = res.getJSONObject("geometry").getJSONObject("location");
        System.out.println("lat: " + loc.getDouble("lat") + ", lng: " + loc.getDouble("lng"));
////////NEED TO REWRITE THIS IN A PRETTIER WAY//////////////////////////////////
        if (age) {
            if (mode) {
                if (type.contains("Client") || type.contains("Amputee")) {
                    potentialClientOutput = potentialClientOutput + "[" + "\"" + name + ""
                            + "\", " + loc.getDouble("lat") + ", "
                            + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "<p>" + phone + "</p>" + "\"" + "],";
                } else if (type.contains("WC Client")) {
                    completedWorkerCompOutput = completedWorkerCompOutput + "[" + "\"" + name + ""
                            + "\", " + loc.getDouble("lat") + ", "
                            + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "<p>" + phone + "</p>" + "\"" + "],";
                } else if (type.contains("Prosthetist") || type.contains("Orthotist")) {
                    prosthetistOutput = prosthetistOutput + "[" + "\"" + name + ""
                            + "\", " + loc.getDouble("lat") + ", "
                            + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "<p>" + phone + "</p>" + "\"" + "],";
                } else if (type.contains("Doctor")) {
                    doctorOutput = doctorOutput + "[" + "\"" + name + ""
                            + "\", " + loc.getDouble("lat") + ", "
                            + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "<p>" + phone + "</p>" + "\"" + "],";
                } else if (type.contains("Therapist")) {
                    therapistOutput = therapistOutput + "[" + "\"" + name + ""
                            + "\", " + loc.getDouble("lat") + ", "
                            + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "<p>" + phone + "</p>" + "\"" + "],";
                } else if (type.contains("Nurse")) {
                    nurseCaseManagerOutput = nurseCaseManagerOutput + "[" + "\"" + name + ""
                            + "\", " + loc.getDouble("lat") + ", "
                            + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "<p>" + phone + "</p>" + "\"" + "],";
                }
            } else {
                if (type.contains("") || type.contains("Amputee")) {
                    basicInquiryOutput = basicInquiryOutput + "[" + "\"" + name + ""
                            + "\", " + loc.getDouble("lat") + ", "
                            + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "\"" + "],";
                } else if (type.contains("Potential")) {
                    goodProgressInquiryOutput = goodProgressInquiryOutput + "[" + "\"" + name + ""
                            + "\", " + loc.getDouble("lat") + ", "
                            + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "\"" + "],";
                } else if (type.contains("Workers Comp")) {
                    workerCompOutput = workerCompOutput + "[" + "\"" + name + ""
                            + "\", " + loc.getDouble("lat") + ", "
                            + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "\"" + "],";
                }
            }
        } else {
            if (portion == NEW) {
                if (type.contains("Client") || type.contains("Amputee")) {
                    tag = "potentialClientOutput";
                    newAddress = "[" + "\"" + name + ""
                            + "\", " + loc.getDouble("lat") + ", "
                            + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "<p>" + phone + "</p>" + "\"" + "],";
                } else if (type.contains("WC Client")) {
                    tag = "completedWorkerCompOutput";
                    newAddress = "[" + "\"" + name + ""
                            + "\", " + loc.getDouble("lat") + ", "
                            + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "<p>" + phone + "</p>" + "\"" + "],";
                } else if (type.contains("Prosthetist") || type.contains("Orthotist")) {
                    tag = "prosthetistOutput";
                    newAddress = "[" + "\"" + name + ""
                            + "\", " + loc.getDouble("lat") + ", "
                            + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "<p>" + phone + "</p>" + "\"" + "],";
                } else if (type.contains("Doctor")) {
                    tag = "doctorOutput";
                    newAddress = "[" + "\"" + name + ""
                            + "\", " + loc.getDouble("lat") + ", "
                            + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "<p>" + phone + "</p>" + "\"" + "],";
                } else if (type.contains("Therapist")) {
                    tag = "therapistOutput";
                    newAddress = "[" + "\"" + name + ""
                            + "\", " + loc.getDouble("lat") + ", "
                            + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "<p>" + phone + "</p>" + "\"" + "],";
                } else if (type.contains("Nurse")) {
                    tag = "nurseCaseManagerOutput";
                    newAddress = "[" + "\"" + name + ""
                            + "\", " + loc.getDouble("lat") + ", "
                            + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "<p>" + phone + "</p>" + "\"" + "],";
                }
                System.out.println("New Address is: " + newAddress);
            } else {
//              Need to double check the tag for conflicts somehow
                oldAddress = "[" + "\"" + name + ""
                        + "\", " + loc.getDouble("lat") + ", "
                        + loc.getDouble("lng") + ", " + "\"" + "<h1>" + name + "</h1>" + "<p>" + prettyAddress + "</p>" + "<p>" + phone + "</p>" + "\"" + "],";

                System.out.println("Old Address is: " + oldAddress);
                
                String temp = GlobalVariables.previousPotentials;
                
                AddressChanger.changeAddress(oldAddress, newAddress, GlobalVariables.previousPotentials, tag);
                
            }
        }
        System.out.println(type);
        Thread.sleep(200);
    }

    public static void initXJHWriter() throws IOException {
        writeRef = new FileWriter("dataTest.html", false);
        writer = new BufferedWriter(writeRef);
    }

    public static void parseContact() throws IOException, Exception {
        contact_CSV_Reader = new CsvReader(contactCSV);
        contact_CSV_Reader.readHeaders();

        while (contact_CSV_Reader.readRecord() && contactCounter < 10) {
            delims = "[ ]+";
            location = "";
            name = "";
            input = "";
            type = "";
            city = "";
            phoneNumber = "";
            personCache = "";

            city = contact_CSV_Reader.get("Mailing City");
            type = contact_CSV_Reader.get("ID/Status");
            input = contact_CSV_Reader.get("Mailing Street");
            phoneNumber = contact_CSV_Reader.get("Phone");
            if (contactCounter > 0 && !type.equals("") && !input.equals("")
                    && !input.contains("P.O.") && !input.contains("PO")) {
                if (city.equals("")) {
                    input = input + " " + contact_CSV_Reader.get("Mailing State");
                    name = contact_CSV_Reader.get("Last Name") + ", " + contact_CSV_Reader.get("First Name");
                } else {
                    input = input + " " + city + " " + contact_CSV_Reader.get("Mailing State");
                    name = contact_CSV_Reader.get("Last Name") + ", " + contact_CSV_Reader.get("First Name");
                }
                tokens = input.split(delims);
                for (int i = 0; i < tokens.length; i++) {
                    if (i + 1 >= tokens.length) {
                        location = location + (tokens[i]);
                    } else {
                        location = location + (tokens[i] + "+");
                    }
                }

                personCache = name + "]" + location;

                AlreadyWrittenChecker.parsePerson(personCache);

                if (!GlobalVariables.newFile) {
                    if (AlreadyWrittenChecker.checkPerson(personCache)) {
                        System.out.println("Already have this one in: " + personCache);
                    } else if (AlreadyWrittenChecker.checkName(name) && !AlreadyWrittenChecker.checkAddress(location)) {
                        System.out.println("ADDRESS CHANGE!!! " + personCache);
                        geocode(location, name, input, type, phoneNumber, true, OLD, NEW);
                        geocode(AlreadyWrittenChecker.getAddress(), name, input, type, phoneNumber, true, OLD, OLD);
                    } else {
//                            System.out.println("No match :( " + location);
                        AlreadyWrittenChecker.addToList(personCache);
                        geocode(location, name, input, type, phoneNumber, true, NEW, NEW);
                    }
                } else {
                    if (AlreadyWrittenChecker.checkPerson(personCache)) {
                        System.out.println("This person is already on the map.");
                    } else {
                        AlreadyWrittenChecker.addToList(personCache);
                        geocode(location, name, input, type, phoneNumber, true, NEW, NEW);
                    }
                }
            }
            contactCounter++;
        }
    }

    public static void parseLead() throws IOException, Exception {
        lead_CSV_Reader = new CsvReader(leadCSV);
        lead_CSV_Reader.readHeaders();
        leadCounter = 0;

        while (lead_CSV_Reader.readRecord() & leadCounter < 10) {
            delims = "[ ]+";
            location = "";
            name = "";
            input = "";
            type = "";
            city = "";

            city = lead_CSV_Reader.get("City");
            type = lead_CSV_Reader.get("ID/Status");
            input = lead_CSV_Reader.get("Street");
            if (!input.equals("") && !input.contains("P.O.") && !input.contains("\"A\"") && !input.contains("PO")) {
                if (city.equals("")) {
                    input = input + " " + lead_CSV_Reader.get("State");
                    name = lead_CSV_Reader.get("Last Name") + ", " + lead_CSV_Reader.get("First Name");
                } else {
                    input = input + " " + city + " " + lead_CSV_Reader.get("State");
                    name = lead_CSV_Reader.get("Last Name") + ", " + lead_CSV_Reader.get("First Name");
                }
                tokens = input.split(delims);
                for (int i = 0; i < tokens.length; i++) {
                    if (i + 1 >= tokens.length) {
                        location = location + (tokens[i]);
                    } else {
                        location = location + (tokens[i] + "+");
                    }
                }

//                personCache = name + " " + location;
//
//                if (!GlobalVariables.newFile) {
//                    if (AlreadyWrittenChecker.checkPerson(personCache)) {
//                        System.out.println("Already have this address in: " + location);
//                    } else {
////                            System.out.println("No match :( " + location);
//                        AlreadyWrittenChecker.addToList(personCache);
//                        geocode(location, name, input, false, type, null);
//                    }
//                } else {
//                    if (AlreadyWrittenChecker.checkPerson(personCache)) {
//                        System.out.println("This person is already on the map.");
//                    } else {
//                        AlreadyWrittenChecker.addToList(personCache);
//                        geocode(location, name, input, false, type, null);
//                    }
//                }
                leadCounter++;
            }
        }
    }

    public static void writeData() throws IOException {
        writer.write(HTML.getHeader());

        if (!GlobalVariables.newFile) {
            writer.write(HTML.basicInquiryMarkersOpen());
            writer.write(basicInquiryOutput);
            writer.write(GlobalVariables.previousBasicInquiries);
            writer.write(HTML.basicInquiryMarkersClose());

            writer.write(HTML.potentialMarkersOpen());
            writer.write(potentialClientOutput);
            writer.write(GlobalVariables.previousPotentials);
            writer.write(HTML.potentialMarkersClose());

            writer.write(HTML.doctorOpen());
            writer.write(doctorOutput);
            writer.write(GlobalVariables.previousDoctors);
            writer.write(HTML.doctorClose());

            writer.write(HTML.therapistOpen());
            writer.write(therapistOutput);
            writer.write(GlobalVariables.previousTherapists);
            writer.write(HTML.therapistClose());

            writer.write(HTML.prosthetistOpen());
            writer.write(prosthetistOutput);
            writer.write(GlobalVariables.previousProsthetists);
            writer.write(HTML.prosthetistClose());

            writer.write(HTML.nurseCaseManagerOpen());
            writer.write(nurseCaseManagerOutput);
            writer.write(GlobalVariables.previousNurseCaseManagers);
            writer.write(HTML.nurseCaseManagerClose());

            writer.write(HTML.goodProgressInquiryMarkersOpen());
            writer.write(goodProgressInquiryOutput);
            writer.write(GlobalVariables.previousGoodProgressInquiries);
            writer.write(HTML.goodProgressInquiryMarkersClose());

            writer.write(HTML.workerCompMarkersOpen());
            writer.write(workerCompOutput);
            writer.write(GlobalVariables.previousWorkerComps);
            writer.write(HTML.workerCompMarkersClose());

            writer.write(HTML.completedCustomerMarkersOpen());
            writer.write(completedCustomerOutput);
            writer.write(GlobalVariables.previousCompletedCustomers);
            writer.write(HTML.completedCustomerMarkersClose());

            writer.write(HTML.completedWorkerCompMarkersOpen());
            writer.write(completedWorkerCompOutput);
            writer.write(GlobalVariables.previousCompletedWorkerComp);
            writer.write(HTML.completedWorkerCompMarkersClose());
        } else {
            writer.write(HTML.basicInquiryMarkersOpen());
            writer.write(basicInquiryOutput);
            writer.write(HTML.basicInquiryMarkersClose());

            writer.write(HTML.potentialMarkersOpen());
            writer.write(potentialClientOutput);
            writer.write(HTML.potentialMarkersClose());

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

            writer.write(HTML.goodProgressInquiryMarkersOpen());
            writer.write(goodProgressInquiryOutput);
            writer.write(HTML.goodProgressInquiryMarkersClose());

            writer.write(HTML.workerCompMarkersOpen());
            writer.write(workerCompOutput);
            writer.write(HTML.workerCompMarkersClose());

            writer.write(HTML.completedCustomerMarkersOpen());
            writer.write(completedCustomerOutput);
            writer.write(HTML.completedCustomerMarkersClose());

            writer.write(HTML.completedWorkerCompMarkersOpen());
            writer.write(completedWorkerCompOutput);
            writer.write(HTML.completedWorkerCompMarkersClose());
        }
        writer.write(HTML.getFooter());
    }
}
