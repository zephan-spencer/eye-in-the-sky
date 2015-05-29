import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTML_Parser {

    static String input;
    static String cache;
    static Matcher matcher;
    static FileReader fileReader;
    static BufferedReader htmlScanner;
    static String line;
    final static String alreadyScanned = "alreadyScanned.txt";

    public static void initialize() throws FileNotFoundException {
        fileReader = new FileReader(alreadyScanned);
        htmlScanner = new BufferedReader(fileReader);
        line = null;
    }

    public static void setMatcher() {
        matcher = Pattern.compile("\\[(.*?)\\]").matcher(cache);
    }

////////////////////////////////////////////////////////////////////////////////
    public static void parseHTML() throws FileNotFoundException, IOException {
        input = "";
        try {
            
            while ((line = htmlScanner.readLine()) != null) {
                input = input + line;
                System.out.println(line);
            }

            setMatcher();
            while (matcher.find()) {
                System.out.println(matcher.group(1));
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
