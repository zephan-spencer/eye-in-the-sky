
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
    static final String arrayNumsDelim = "[,]";
    static final String convArrayNumsDelim = "]";
////////////////////////////////////////////////////////////////////////////////
    static String[] arrayNumbers;
    static String[] convArrayNumbers;
////////////////////////////////////////////////////////////////////////////////
    static int numberLength;
    static int maxArrayNum;
    static int[] finalArrayConv;

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
                    arrayNumbers = input.split(arrayNumsDelim);
                    for (double i = 0; i < arrayNumbers.length; i++) {
                        if (i == 4) {
                            arrayNum = (arrayNumbers[(int) i]);
                        } else if ((i + 1) / 5 == Math.round((i + 1) / 5)) {
                            arrayNum = arrayNum + arrayNumbers[(int) i];
                        } else {
                        }
                        numberLength = (int) i;
                    }
                    finalArrayConv = new int[numberLength];
                    convArrayNumbers = arrayNum.split(convArrayNumsDelim);
                    for (int i = 0; i < convArrayNumbers.length; i++) {
                        convArrayNumbers[i] = convArrayNumbers[i].trim();
                        finalArrayConv[i] = Integer.parseInt(convArrayNumbers[i]);
                    }
                    for (int i = 1; i < finalArrayConv.length; i++) {
                        if (finalArrayConv[i] > maxArrayNum) {
                            maxArrayNum = finalArrayConv[i];
                        }
                    }
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

    public static int getMaxArrayNum() {
        return maxArrayNum;
    }
}
