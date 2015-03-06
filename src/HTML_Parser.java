
import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class HTML_Parser {

    static Scanner scanner;
    static String input;
    static String cache;

    public static String parseHTML() throws FileNotFoundException {
        input = "";
        scanner = new Scanner(new File("dataTest.html"));
        try {
            scanner.useDelimiter("\\[");
            scanner.next();
            while (true) {
                cache = input + "[" + scanner.next();
                if (!cache.contains(";")) {
                    input = cache;
                } else {
                    return input;
                }
            }
        } catch (NoSuchElementException e) {
            scanner.close();
            System.out.println("ERROR in HTML_Scanner");
            return "";
        }

    }
}
