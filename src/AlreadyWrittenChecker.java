
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class AlreadyWrittenChecker {

    private final static String parsedLeads = "alreadyScannedLeads.txt";
    private final static String parsedContacts = "alreadyScannedContacts.txt";
////////////////////////////////////////////////////////////////////////////////
    private static FileWriter alreadyWrittenLeadsWriter;
    private static FileWriter alreadyWrittenContactsWriter;
////////////////////////////////////////////////////////////////////////////////
    private static Scanner alreadyWrittenReader;
////////////////////////////////////////////////////////////////////////////////

    public static void alreadyWrittenLeadsInit() throws IOException {

        if (GlobalVariables.newFile) {
            alreadyWrittenLeadsWriter = new FileWriter(parsedLeads, false);
        } else {
            alreadyWrittenLeadsWriter = new FileWriter(parsedLeads, true);
            initScanner(parsedLeads);
        }
    }

    public static void alreadyWrittenContactsInit() throws IOException {

        if (GlobalVariables.newFile) {
            alreadyWrittenContactsWriter = new FileWriter(parsedContacts, false);
        } else {
            alreadyWrittenContactsWriter = new FileWriter(parsedContacts, true);
            initScanner(parsedContacts);
        }
    }

    public static void addLeadToList(String user) throws IOException {
        alreadyWrittenLeadsWriter.write(user + ":");
    }

    public static void addContactToList(String user) throws IOException {
        alreadyWrittenContactsWriter.write(user + ":");
    }

    public static boolean checkLeads(String data) throws FileNotFoundException {
        try {
            alreadyWrittenReader = new Scanner(new File(parsedLeads));
            alreadyWrittenReader.useDelimiter(":");
            while (true) {
                String temp = "";
                temp = alreadyWrittenReader.next();
                if (!data.equals(temp)) {
                } else {
                    return true;
                }
            }

        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public static boolean checkContacts(String data) throws FileNotFoundException {
        try {
            alreadyWrittenReader = new Scanner(new File(parsedContacts));
            alreadyWrittenReader.useDelimiter(":");
            while (true) {
                String temp = "";
                temp = alreadyWrittenReader.next();
                if (!data.equals(temp)) {
                } else {
                    return true;
                }
            }

        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public static void closeWrittenCheckers() throws IOException {
        alreadyWrittenLeadsWriter.close();
        alreadyWrittenContactsWriter.close();
    }

    public static void initScanner(String fileName) throws FileNotFoundException {
        alreadyWrittenReader = new Scanner(new File(fileName));
        alreadyWrittenReader.useDelimiter(":");
    }
}
