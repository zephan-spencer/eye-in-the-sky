
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class XJH {

    public static void main(String[] args) {

        run();
    }

    public static void run() {

        String csvFile = "/Users/Emil/Downloads/contact.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("dataTEST.txt", "UTF-8");

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] data = line.split(cvsSplitBy);

                writer.println();
                writer.println("Last Name: " + data[8]);
//                writer.println("First name: " + data[4]);
//                writer.println("Phone: " + data[13]);
                writer.println();
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            writer.close();
        }
        System.out.println("Done");

    }
}
