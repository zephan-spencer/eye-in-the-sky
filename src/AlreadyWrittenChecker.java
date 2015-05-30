
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class AlreadyWrittenChecker {

    private final static String parsedData = "alreadyScanned.txt";
////////////////////////////////////////////////////////////////////////////////
    private static FileWriter alreadyWrittenWriter;
////////////////////////////////////////////////////////////////////////////////
    private static Scanner alreadyWrittenReader;
////////////////////////////////////////////////////////////////////////////////

    public static void alreadyWrittenCheckerInit() throws IOException {

        if (GlobalVariables.newFile) {
            alreadyWrittenWriter = new FileWriter(parsedData, false);
        } else {
            alreadyWrittenWriter = new FileWriter(parsedData, true);
            initScanner();
        }
    }

    public static void addToList(String user) throws IOException {
        alreadyWrittenWriter.write(user + ":");
    }

    public static boolean checkPerson(String data) throws FileNotFoundException {
        return false;
    }

    public static void closeWrittenChecker() throws IOException {
        alreadyWrittenWriter.close();
    }

    public static void initScanner() throws FileNotFoundException {
        alreadyWrittenReader = new Scanner(new File(parsedData));
        alreadyWrittenReader.useDelimiter(":");
    }
}
