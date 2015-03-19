
import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class HTML_Parser {

    static Scanner htmlScanner;
    static String input;
    static String cache;
    static String arrayNum;
////////////////////////////////////////////////////////////////////////////////    
    static final String convArrayNumsDelim = "]";
////////////////////////////////////////////////////////////////////////////////
    static String[] convArrayNumbers;
////////////////////////////////////////////////////////////////////////////////

    public static void parseHTML() throws FileNotFoundException {
        input = "";
        htmlScanner = new Scanner(new File("dataTest.html"));
        try {
            htmlScanner.useDelimiter("\\[");
            htmlScanner.next();
            while (true) {
                cache = input + "[" + htmlScanner.next();
                if (!cache.contains(";")) {
                    input = cache;
                } else {
                    break;
                }
            }

        } catch (NoSuchElementException e) {
            htmlScanner.close();
            System.out.println("ERROR in HTML_Scanner");
        }

    }

    public static String getPreviousCords() {
        return input;
    }
}
