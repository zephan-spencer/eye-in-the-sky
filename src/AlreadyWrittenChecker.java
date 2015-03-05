
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class AlreadyWrittenChecker {

    private static String parsedData = "alreadyScanned.txt";
////////////////////////////////////////////////////////////////////////////////
    private static FileWriter alreadyWrittenWriter;
////////////////////////////////////////////////////////////////////////////////
    private static Scanner alreadyWrittenReader;
////////////////////////////////////////////////////////////////////////////////
    private static String[] test;
////////////////////////////////////////////////////////////////////////////////

    public static void alreadyWrittenCheckerInit() throws IOException {

        if (GlobalVariables.newFile) {
            alreadyWrittenWriter = new FileWriter(parsedData, false);
        } else {
            alreadyWrittenWriter = new FileWriter(parsedData, true);
            initScanner();
        }
    }

    public static void addToList(String address) throws IOException {
        alreadyWrittenWriter.write(address + ":");
    }

    public static String getAddress() throws IOException {
        return alreadyWrittenReader.next();
    }

    public static void closeWrittenChecker() throws IOException {
        alreadyWrittenWriter.close();
    }

    public static void initScanner() throws FileNotFoundException {
        alreadyWrittenReader = new Scanner(new File(parsedData));
        alreadyWrittenReader.useDelimiter(":");
    }
}
