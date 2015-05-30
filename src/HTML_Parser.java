
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTML_Parser {

    public static String previousBasicInquiries;
    public static String previousPotentials;
    public static String previousDoctors;
    public static String previousTherapists;
    public static String previousProsthetists;
    public static String previousGoodProgressInquiries;
    public static String previousWorkerComps;
    public static String previousCompletedCustomers;

////////////////////////////////////////////////////////////////////////////////
    final static String alreadyScanned = "dataTest.html";
////////////////////////////////////////////////////////////////////////////////
    static String input;
    static String cache;
    static Matcher matcher;
    static FileReader fileReader;
    static BufferedReader htmlScanner;
    static String line;
////////////////////////////////////////////////////////////////////////////////
    private static int counter;

    public static void initialize() throws FileNotFoundException {
        fileReader = new FileReader(alreadyScanned);
        htmlScanner = new BufferedReader(fileReader);
        line = null;

        previousBasicInquiries = "";
        previousPotentials = "";
        previousDoctors = "";
        previousTherapists = "";
        previousProsthetists = "";
        previousGoodProgressInquiries = "";
        previousWorkerComps = "";
        previousCompletedCustomers = "";

        counter = 0;
    }

    public static void setMatcher() {
        matcher = Pattern.compile("\\[\\[(.*?)\\];").matcher(input);
    }

////////////////////////////////////////////////////////////////////////////////
    public static void parseHTML() throws FileNotFoundException, IOException {
        input = "";
        initialize();
        try {
            while ((line = htmlScanner.readLine()) != null) {
                input = input + line;
            }
            setMatcher();
            while (matcher.find()) {
                switch (counter) {
                    case 0:
                        previousBasicInquiries = "[" + matcher.group(1);
                        break;
                    case 1:
                        previousPotentials = "[" + matcher.group(1);
                        break;
                    case 2:
                        previousDoctors = "[" + matcher.group(1);
                        break;
                    case 3:
                        previousTherapists = "[" + matcher.group(1);
                        break;
                    case 4:
                        previousProsthetists = "[" + matcher.group(1);
                        break;
                    case 5:
                        previousGoodProgressInquiries = "[" + matcher.group(1);
                        break;
                    case 6:
                        previousWorkerComps = "[" + matcher.group(1);
                        break;
                    case 7:
                        previousCompletedCustomers = "[" + matcher.group(1);
                        break;
                }
                counter++;
            }
            System.out.println(previousBasicInquiries);
            System.out.println(previousPotentials);
            System.out.println(previousDoctors);
            System.out.println(previousTherapists);
            System.out.println(previousProsthetists);
            System.out.println(previousGoodProgressInquiries);
            System.out.println(previousWorkerComps);
            System.out.println(previousCompletedCustomers);
        } catch (NoSuchElementException e) {
            htmlScanner.close();
            System.out.println("ERROR in HTML_Scanner");
        }

    }

    public static String getPreviousCords() {
        return input;
    }
}
