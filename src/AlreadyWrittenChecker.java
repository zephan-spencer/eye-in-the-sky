
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class AlreadyWrittenChecker {

    private final static String parsedData = "alreadyScanned.txt";
////////////////////////////////////////////////////////////////////////////////
    private static String name;
    private static String address;
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
        alreadyWrittenWriter.write(user + "]" + ":");
    }

    public static boolean checkPerson(String data) throws FileNotFoundException {
        try {
            alreadyWrittenReader = new Scanner(new File(parsedData));
            alreadyWrittenReader.useDelimiter(":");
            while (true) {
                String temp = "";
                temp = alreadyWrittenReader.next();
                if (!data.equals(temp)) {
                } else {
                    parsePerson(data);
                    return true;
                }
            }

        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public static void parsePerson(String data) throws FileNotFoundException {
        try {
            alreadyWrittenReader = new Scanner(new File(parsedData));
            alreadyWrittenReader.useDelimiter("]");
////////////////////////////////////////////////////////////////////////////////
            name = "";
            address = "";
////////////////////////////////////////////////////////////////////////////////            
            int counter = 0;

            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    name = alreadyWrittenReader.next();
                } else {
                    address = alreadyWrittenReader.next();
                }
            }
        } catch (NoSuchElementException e) {
        }
    }

    public static boolean checkName(String inputtedName) {
        return name.equals(inputtedName);
    }

    public static boolean checkAddress(String inputtedAddress) {
        return address.equals(inputtedAddress);
    }

    public static String getName() {
        return name;
    }

    public static String getAddress() {
        return address;
    }

    public static void closeWrittenChecker() throws IOException {
        alreadyWrittenWriter.close();
    }

    public static void initScanner() throws FileNotFoundException {
        alreadyWrittenReader = new Scanner(new File(parsedData));
        alreadyWrittenReader.useDelimiter(":");
    }
}
