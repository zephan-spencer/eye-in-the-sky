
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CSV {

    public void run() {
        String csvFile = "/Users/Zephan/Downloads/contact.csv";
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
                writer.println("Last Name: " + data[5]);
                writer.println("First name: " + data[4]);
                writer.println("Phone: " + data[13]);
                writer.println();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(XJH.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(XJH.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(XJH.class.getName()).log(Level.SEVERE, null, ex);
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
    }
}
