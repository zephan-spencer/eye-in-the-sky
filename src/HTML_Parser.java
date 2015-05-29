
import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class HTML_Parser {

    static Scanner htmlScanner;
    static String input;
    static String cache;
////////////////////////////////////////////////////////////////////////////////

    public static void parseHTML() throws FileNotFoundException {
        input = "";
        htmlScanner = new Scanner(new File("dataTEST.html"));
        try {
            htmlScanner.useDelimiter("");
            htmlScanner.next();
            while (true) {
                cache = htmlScanner.next();
                if (cache.contains("var basicInquiryMarkers")) {
                    System.out.println();
                } else if (cache.contains("var goodProgressInquiryMarkers")) {
                    System.out.println(cache);
                } else if (cache.contains("var workerCompMarkers")) {
                    System.out.println(cache);
                } else if (cache.contains("var potentialClientMarkers")) {
                    System.out.println(cache);
                } else if (cache.contains("var completedCustomerMarkers")) {
                    System.out.println(cache);
                } else if (cache.contains("var prosthetistMarkers")) {
                    System.out.println(cache);
                } else if (cache.contains("var doctorMarkers")) {
                    System.out.println(cache);
                } else if (cache.contains("var therapistMarkers")) {
                    System.out.println(cache);
                } else if (cache.contains("var nurseCaseManagerMarkers")) {
                    System.out.println(cache);
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
