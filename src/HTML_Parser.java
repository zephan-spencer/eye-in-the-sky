
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTML_Parser {

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
////////////////////////////////////////////////////////////////////////////////
        GlobalVariables.previousBasicInquiries = "";
        GlobalVariables.previousPotentials = "";
        GlobalVariables.previousDoctors = "";
        GlobalVariables.previousTherapists = "";
        GlobalVariables.previousProsthetists = "";
        GlobalVariables.previousNurseCaseManagers = "";
        GlobalVariables.previousGoodProgressInquiries = "";
        GlobalVariables.previousWorkerComps = "";
        GlobalVariables.previousCompletedCustomers = "";
        GlobalVariables.previousCompletedWorkerComp = "";
////////////////////////////////////////////////////////////////////////////////
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
                        GlobalVariables.previousBasicInquiries = "[" + matcher.group(1);
                        break;
                    case 1:
                        GlobalVariables.previousPotentials = "[" + matcher.group(1);
                        break;
                    case 2:
                        GlobalVariables.previousDoctors = "[" + matcher.group(1);
                        break;
                    case 3:
                        GlobalVariables.previousTherapists = "[" + matcher.group(1);
                        break;
                    case 4:
                        GlobalVariables.previousProsthetists = "[" + matcher.group(1);
                        break;
                    case 5:
                        GlobalVariables.previousNurseCaseManagers = "[" + matcher.group(1);
                        break;
                    case 6:
                        GlobalVariables.previousGoodProgressInquiries = "[" + matcher.group(1);
                        break;
                    case 7:
                        GlobalVariables.previousWorkerComps = "[" + matcher.group(1);
                        break;
                    case 8:
                        GlobalVariables.previousCompletedCustomers = "[" + matcher.group(1);
                        break;
                    case 9:
                        GlobalVariables.previousCompletedWorkerComp = "[" + matcher.group(1);
                        break;
                }
                counter++;
            }
            System.out.println(GlobalVariables.previousBasicInquiries);
            System.out.println(GlobalVariables.previousPotentials);
            System.out.println(GlobalVariables.previousDoctors);
            System.out.println(GlobalVariables.previousTherapists);
            System.out.println(GlobalVariables.previousProsthetists);
            System.out.println(GlobalVariables.previousGoodProgressInquiries);
            System.out.println(GlobalVariables.previousWorkerComps);
            System.out.println(GlobalVariables.previousCompletedCustomers);
        } catch (NoSuchElementException e) {
            htmlScanner.close();
            System.out.println("ERROR in HTML_Scanner");
        }

    }

    public static String getPreviousCords() {
        return input;
    }
}
