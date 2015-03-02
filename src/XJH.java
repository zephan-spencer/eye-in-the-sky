
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XJH {

    AddressConverter json;

    public XJH() {
        json = new AddressConverter();
    }

    public void main(String[] args) throws IOException {
        XJH obj = new XJH();
        obj.run();
        json.convertToLatLong("1320 Edwards Dr. Downingtown, PA 19335");
    }

    public void run() {

        System.out.println("Done");

    }
}
