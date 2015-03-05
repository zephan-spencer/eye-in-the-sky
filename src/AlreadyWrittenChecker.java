
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class AlreadyWrittenChecker {

    private static String parsedData = "alreadyScanned.csv";
////////////////////////////////////////////////////////////////////////////////
    private static FileWriter alreadyWrittenWriter;
////////////////////////////////////////////////////////////////////////////////
    private static Scanner alreadyWrittenReader;
    private static Scanner scanner;

    private static String[] test;

    public static boolean newFile;

    private static String addressCache;
    
    private static int counter;

    public static void alreadyWrittenCheckerInit() throws IOException {
        scanner = new Scanner(System.in);

        System.out.println("Would you like to create a new file? Yes/No");
        if (scanner.hasNext("Yes") || scanner.hasNext("yes")) {
            alreadyWrittenWriter = new FileWriter(parsedData, false);
            newFile = true;
        } else {
            alreadyWrittenWriter = new FileWriter(parsedData, true);
            newFile = false;
            initScanner();
        }
        counter = 0;
    }

    public static void addToList(String address) throws IOException {
        alreadyWrittenWriter.write(address + ",");
    }

    public static String getAddress() throws IOException {
        addressCache = "";
        
        addressCache = alreadyWrittenReader.next();
        
        return addressCache;
    }

    public static void closeWrittenChecker() throws IOException {
        alreadyWrittenWriter.close();
//        alreadyWrittenReader.close();
        scanner.close();
    }

    public static void initScanner() throws FileNotFoundException {
        alreadyWrittenReader = new Scanner(new File(parsedData));
        alreadyWrittenReader.useDelimiter(",");
    }
}
